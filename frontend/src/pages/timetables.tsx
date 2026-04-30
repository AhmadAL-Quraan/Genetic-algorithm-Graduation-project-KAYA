import { useEffect, useRef, useState } from "react";
import { TimeTables, type TimeTable, type GAConfig } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Trash2, Sparkles, CalendarDays, List, Info, ChevronDown, ChevronUp, TrendingUp, TrendingDown, Minus, Zap, CheckCircle2, Loader2, Activity } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { TimetableCalendar } from "@/components/timetable-calendar";
import { useQueryClient } from "@tanstack/react-query";

const BASE = import.meta.env.BASE_URL?.replace(/\/$/, "") ?? "";

const PARAM_GUIDE = [
  {
    name: "Generations",
    field: "maxGenerations" as keyof GAConfig,
    default: 200,
    step: 1,
    description: "How many evolution cycles the algorithm runs.",
    higher: { effect: "Better schedules, finds fewer conflicts", cost: "Slower — takes more time to complete" },
    lower: { effect: "Faster results", cost: "May miss a good solution, higher conflict count" },
    recommended: "100–500 for most datasets. Use 200+ for large numbers of courses.",
  },
  {
    name: "Population",
    field: "populationSize" as keyof GAConfig,
    default: 60,
    step: 1,
    description: "Number of candidate timetables evaluated per generation.",
    higher: { effect: "More diverse solutions, less likely to get stuck", cost: "Much slower per generation" },
    lower: { effect: "Very fast", cost: "Low diversity — often converges to a bad local optimum" },
    recommended: "40–100. Raise above 80 only for very large or hard problems.",
  },
  {
    name: "Elitism",
    field: "elitismCount" as keyof GAConfig,
    default: 2,
    step: 1,
    description: "Number of best timetables copied unchanged into the next generation.",
    higher: { effect: "Best solution is always preserved", cost: "Reduces diversity — can cause premature convergence" },
    lower: { effect: "More exploration, diverse population", cost: "May lose good solutions between generations" },
    recommended: "1–3. Rarely set above 5.",
  },
  {
    name: "Tournament",
    field: "tournamentSize" as keyof GAConfig,
    default: 5,
    step: 1,
    description: "How many candidates compete in each selection round; the winner becomes a parent.",
    higher: { effect: "Strong selection pressure — best candidates dominate", cost: "Reduces diversity, premature convergence risk" },
    lower: { effect: "Weak pressure — more diverse parents", cost: "Slower improvement per generation" },
    recommended: "3–7. Keep below population ÷ 10.",
  },
  {
    name: "Mutation Rate",
    field: "initialMutationRate" as keyof GAConfig,
    default: 0.15,
    step: 0.01,
    description: "Probability (0–1) that a given lecture's room or time slot is randomly changed.",
    higher: { effect: "More exploration, escapes local minima", cost: "Too high → random walk, no learning" },
    lower: { effect: "Stable improvement of good solutions", cost: "Too low → gets stuck, slow to recover from bad start" },
    recommended: "0.05–0.25. Start at 0.15; raise if results plateau early.",
  },
  {
    name: "Mutation Impact",
    field: "mutationImpactRatio" as keyof GAConfig,
    default: 0.1,
    step: 0.01,
    description: "Fraction of lectures affected when a mutation is applied.",
    higher: { effect: "Large structural changes per mutation", cost: "Can destroy good partial solutions" },
    lower: { effect: "Fine-grained adjustments", cost: "Very slow improvement on hard problems" },
    recommended: "0.05–0.15. Keep low (0.1) for most cases.",
  },
];

function ParamGuide() {
  const [open, setOpen] = useState(false);
  return (
    <div className="border rounded-lg overflow-hidden">
      <button
        onClick={() => setOpen(v => !v)}
        className="w-full flex items-center justify-between px-4 py-3 bg-muted/40 hover:bg-muted/70 transition-colors text-sm font-medium text-left">
        <span className="flex items-center gap-2">
          <Info className="h-4 w-4 text-muted-foreground" />
          Parameter guide — what do these settings do?
        </span>
        {open ? <ChevronUp className="h-4 w-4 text-muted-foreground" /> : <ChevronDown className="h-4 w-4 text-muted-foreground" />}
      </button>

      {open && (
        <div className="p-4 space-y-4 border-t bg-background">
          <p className="text-sm text-muted-foreground">
            KAYA uses a <strong>genetic algorithm (GA)</strong> — it evolves a population of candidate timetables
            over many generations, selecting and combining the best ones. The parameters below control how that
            search behaves. A lower <strong>fitness penalty = fewer conflicts</strong>.
          </p>
          <div className="space-y-3">
            {PARAM_GUIDE.map(p => (
              <div key={p.field} className="rounded-md border p-3 text-sm space-y-2">
                <div className="flex items-start justify-between gap-2 flex-wrap">
                  <span className="font-semibold">{p.name}</span>
                  <Badge variant="outline" className="text-xs font-mono">default: {p.default}</Badge>
                </div>
                <p className="text-muted-foreground">{p.description}</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                  <div className="flex items-start gap-2 rounded bg-green-50 dark:bg-green-950/30 p-2">
                    <TrendingUp className="h-3.5 w-3.5 text-green-600 mt-0.5 shrink-0" />
                    <div>
                      <span className="font-medium text-green-700 dark:text-green-400">Higher: </span>
                      <span className="text-muted-foreground">{p.higher.effect}</span>
                      <span className="text-red-500"> — but: </span>
                      <span className="text-muted-foreground">{p.higher.cost}</span>
                    </div>
                  </div>
                  <div className="flex items-start gap-2 rounded bg-blue-50 dark:bg-blue-950/30 p-2">
                    <TrendingDown className="h-3.5 w-3.5 text-blue-600 mt-0.5 shrink-0" />
                    <div>
                      <span className="font-medium text-blue-700 dark:text-blue-400">Lower: </span>
                      <span className="text-muted-foreground">{p.lower.effect}</span>
                      <span className="text-red-500"> — but: </span>
                      <span className="text-muted-foreground">{p.lower.cost}</span>
                    </div>
                  </div>
                </div>
                <div className="flex items-start gap-2 text-xs text-muted-foreground">
                  <Minus className="h-3 w-3 mt-0.5 shrink-0" />
                  <span><strong>Recommended:</strong> {p.recommended}</span>
                </div>
              </div>
            ))}
          </div>
          <div className="rounded-md bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 p-3 text-xs text-amber-800 dark:text-amber-300">
            <strong>General tip:</strong> Start with the defaults. If the fitness penalty is still high after
            generation, first increase <em>Generations</em> (e.g. 400), then <em>Population</em> (e.g. 80).
            Only tune mutation once those are exhausted.
          </div>
        </div>
      )}
    </div>
  );
}

interface ProgressState {
  phase: "initializing" | "evolving" | "perfect" | "saving";
  generation: number;
  maxGenerations: number;
  bestFitness: number;
  roomConflicts: number;
  instructorConflicts: number;
  studentConflicts: number;
  mutationRate: number;
}

function FitnessSparkline({ history }: { history: number[] }) {
  if (history.length < 2) return null;
  const W = 320, H = 64, pad = 4;
  const min = Math.min(...history);
  const max = Math.max(...history);
  const range = max - min || 1;
  const pts = history.map((v, i) => {
    const x = pad + (i / (history.length - 1)) * (W - pad * 2);
    const y = H - pad - ((v - min) / range) * (H - pad * 2);
    return `${x},${y}`;
  });
  const latest = history[history.length - 1];
  const lx = pad + ((history.length - 1) / (history.length - 1)) * (W - pad * 2);
  const ly = H - pad - ((latest - min) / range) * (H - pad * 2);
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full h-16" preserveAspectRatio="none">
      <polyline
        points={pts.join(" ")}
        fill="none"
        stroke="hsl(var(--primary))"
        strokeWidth="2"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
      <circle cx={lx} cy={ly} r="4" fill="hsl(var(--primary))" />
    </svg>
  );
}

function LiveProgressPanel({ progress, history }: { progress: ProgressState; history: number[] }) {
  const pct = progress.maxGenerations > 0
    ? Math.round((progress.generation / progress.maxGenerations) * 100)
    : 0;
  const isPerfect = progress.phase === "perfect";
  const isSaving = progress.phase === "saving";
  const isInit = progress.phase === "initializing";

  return (
    <Card className="border-primary/30 bg-gradient-to-br from-background to-muted/20">
      <CardHeader className="pb-2">
        <CardTitle className="text-base flex items-center gap-2">
          {isPerfect ? (
            <><CheckCircle2 className="h-4 w-4 text-green-500" /> Perfect schedule found!</>
          ) : isSaving ? (
            <><Loader2 className="h-4 w-4 animate-spin text-primary" /> Saving timetable…</>
          ) : isInit ? (
            <><Loader2 className="h-4 w-4 animate-spin text-primary" /> Initializing population…</>
          ) : (
            <><Activity className="h-4 w-4 text-primary animate-pulse" /> Evolving chromosomes…</>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {!isInit && !isSaving && (
          <>
            <div className="flex items-end justify-between gap-4">
              <div>
                <p className="text-xs text-muted-foreground mb-1">Generation</p>
                <p className="text-2xl font-bold tabular-nums">
                  {progress.generation}
                  <span className="text-sm font-normal text-muted-foreground"> / {progress.maxGenerations}</span>
                </p>
              </div>
              <div className="text-right">
                <p className="text-xs text-muted-foreground mb-1">Best fitness penalty</p>
                <p className={`text-2xl font-bold tabular-nums ${isPerfect ? "text-green-500" : progress.bestFitness < 0 ? "text-orange-500" : "text-green-500"}`}>
                  {progress.bestFitness}
                </p>
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>Progress</span>
                <span>{pct}%</span>
              </div>
              <div className="h-2 rounded-full bg-muted overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-300 ${isPerfect ? "bg-green-500" : "bg-primary"}`}
                  style={{ width: `${isPerfect ? 100 : pct}%` }}
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-2 text-center">
              <div className="rounded-lg bg-red-50 dark:bg-red-950/30 border border-red-100 dark:border-red-900 p-2">
                <p className="text-xs text-muted-foreground">Room</p>
                <p className="text-lg font-bold tabular-nums text-red-600 dark:text-red-400">{progress.roomConflicts}</p>
                <p className="text-xs text-muted-foreground">conflicts</p>
              </div>
              <div className="rounded-lg bg-amber-50 dark:bg-amber-950/30 border border-amber-100 dark:border-amber-900 p-2">
                <p className="text-xs text-muted-foreground">Instructor</p>
                <p className="text-lg font-bold tabular-nums text-amber-600 dark:text-amber-400">{progress.instructorConflicts}</p>
                <p className="text-xs text-muted-foreground">conflicts</p>
              </div>
              <div className="rounded-lg bg-blue-50 dark:bg-blue-950/30 border border-blue-100 dark:border-blue-900 p-2">
                <p className="text-xs text-muted-foreground">Student</p>
                <p className="text-lg font-bold tabular-nums text-blue-600 dark:text-blue-400">{progress.studentConflicts}</p>
                <p className="text-xs text-muted-foreground">conflicts</p>
              </div>
            </div>

            {progress.mutationRate > 0.16 && (
              <div className="flex items-center gap-2 rounded-md bg-purple-50 dark:bg-purple-950/30 border border-purple-100 dark:border-purple-900 px-3 py-2 text-xs text-purple-700 dark:text-purple-300">
                <Zap className="h-3.5 w-3.5 shrink-0" />
                Adaptive mutation active — rate boosted to {(progress.mutationRate * 100).toFixed(0)}% to escape local optima
              </div>
            )}

            <div>
              <p className="text-xs text-muted-foreground mb-1">Fitness over time</p>
              <FitnessSparkline history={history} />
            </div>
          </>
        )}

        {isInit && (
          <p className="text-sm text-muted-foreground">Building the initial population of candidate timetables…</p>
        )}
        {isSaving && (
          <p className="text-sm text-muted-foreground">GA finished. Persisting the best timetable to the database…</p>
        )}
      </CardContent>
    </Card>
  );
}

export default function TimetablesPage() {
  const { toast } = useToast();
  const qc = useQueryClient();
  const list = TimeTables.useList();
  const remove = TimeTables.useDelete();

  const [cfg, setCfg] = useState<GAConfig>({
    maxGenerations: 200, populationSize: 60, elitismCount: 2, tournamentSize: 5,
    initialMutationRate: 0.15, mutationImpactRatio: 0.1,
  });

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [generating, setGenerating] = useState(false);
  const [progress, setProgress] = useState<ProgressState | null>(null);
  const [fitnessHistory, setFitnessHistory] = useState<number[]>([]);
  const abortRef = useRef<AbortController | null>(null);

  const sortedTimetables = list.data ? [...list.data].sort((a, b) => (a.id ?? 0) - (b.id ?? 0)) : [];

  useEffect(() => {
    if (selectedId == null && list.data && list.data.length > 0) {
      const latest = [...list.data].sort((a, b) => (b.id ?? 0) - (a.id ?? 0))[0];
      setSelectedId(latest.id);
    }
  }, [list.data, selectedId]);

  const selected: TimeTable | null = list.data?.find(t => t.id === selectedId) ?? null;
  const selectedDisplayNum = selected ? sortedTimetables.findIndex(t => t.id === selected.id) + 1 : null;

  const onGenerate = async () => {
    abortRef.current?.abort();
    const ac = new AbortController();
    abortRef.current = ac;

    setGenerating(true);
    setProgress({ phase: "initializing", generation: 0, maxGenerations: cfg.maxGenerations, bestFitness: 0, roomConflicts: 0, instructorConflicts: 0, studentConflicts: 0, mutationRate: cfg.initialMutationRate });
    setFitnessHistory([]);

    try {
      const res = await fetch(`${BASE}/api/time-table/generate-stream`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "text/event-stream" },
        body: JSON.stringify(cfg),
        signal: ac.signal,
      });

      if (!res.ok || !res.body) throw new Error(`Server error: ${res.status}`);

      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buf = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buf += decoder.decode(value, { stream: true });

        const lines = buf.split("\n");
        buf = lines.pop() ?? "";

        let eventName = "";
        for (const line of lines) {
          if (line.startsWith("event:")) {
            eventName = line.slice(6).trim();
          } else if (line.startsWith("data:")) {
            const payload = line.slice(5).trim();
            if (!payload) continue;

            if (eventName === "progress") {
              const p = JSON.parse(payload) as ProgressState;
              setProgress(p);
              if (p.phase === "evolving" || p.phase === "perfect") {
                setFitnessHistory(h => [...h, p.bestFitness]);
              }
            } else if (eventName === "complete") {
              const tt = JSON.parse(payload) as TimeTable;
              qc.invalidateQueries({ queryKey: ["time-table"] });
              setSelectedId(tt.id);
              toast({ title: "Timetable generated", description: `Fitness penalty: ${tt.fitness}` });
            } else if (eventName === "error") {
              throw new Error(payload);
            }
            eventName = "";
          }
        }
      }
    } catch (err: any) {
      if (err.name !== "AbortError") {
        toast({ title: "Generation failed", description: err.message, variant: "destructive" });
      }
    } finally {
      setGenerating(false);
      setProgress(null);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Schedule</h1>
        <p className="text-muted-foreground text-sm">Run the genetic algorithm to produce a conflict-minimized timetable.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2"><Sparkles className="h-4 w-4" /> Generate</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {PARAM_GUIDE.map(p => (
              <div key={p.field}>
                <Label className="text-xs">{p.name}</Label>
                <Input
                  type="number"
                  step={p.step}
                  value={cfg[p.field] ?? p.default}
                  onChange={e => setCfg({ ...cfg, [p.field]: +e.target.value })}
                  disabled={generating}
                />
              </div>
            ))}
          </div>

          <ParamGuide />

          <Button onClick={onGenerate} disabled={generating}>
            {generating ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Running…</> : "Generate timetable"}
          </Button>
        </CardContent>
      </Card>

      {generating && progress && (
        <LiveProgressPanel progress={progress} history={fitnessHistory} />
      )}

      <Card>
        <CardHeader><CardTitle className="text-base">Saved timetables ({list.data?.length ?? 0})</CardTitle></CardHeader>
        <CardContent>
          {list.isLoading ? <div className="text-sm text-muted-foreground">Loading…</div> :
           !list.data?.length ? <div className="text-sm text-muted-foreground">No timetables yet — generate one above.</div> :
            <Table>
              <TableHeader><TableRow>
                <TableHead>ID</TableHead><TableHead>Generated</TableHead>
                <TableHead>Lectures</TableHead><TableHead>Fitness penalty</TableHead><TableHead /></TableRow></TableHeader>
              <TableBody>
                {sortedTimetables.map((t, idx) => (
                  <TableRow key={t.id} className={`cursor-pointer ${selectedId === t.id ? "bg-muted/60" : ""}`} onClick={() => setSelectedId(t.id)}>
                    <TableCell>#{idx + 1}</TableCell>
                    <TableCell>{t.generatedAt ? new Date(t.generatedAt).toLocaleString() : "—"}</TableCell>
                    <TableCell>{t.lectures?.length ?? 0}</TableCell>
                    <TableCell><Badge variant={t.fitness === 0 ? "default" : "secondary"}>{t.fitness}</Badge></TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={(e) => { e.stopPropagation(); remove.mutate(t.id); if (selectedId === t.id) setSelectedId(null); }}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          }
        </CardContent>
      </Card>

      {selected && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Timetable #{selectedDisplayNum}</CardTitle>
            {selected.fitnessReport && (
              <div className="flex flex-wrap gap-2 pt-1 text-xs">
                <Badge variant="outline">Room conflicts: {selected.fitnessReport.roomConflicts}</Badge>
                <Badge variant="outline">Instructor conflicts: {selected.fitnessReport.instructorConflicts}</Badge>
                <Badge variant="outline">Student conflicts: {selected.fitnessReport.studentConflicts}</Badge>
                <Badge>Total penalty: {selected.fitnessReport.totalPenalty}</Badge>
              </div>
            )}
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="calendar">
              <TabsList>
                <TabsTrigger value="calendar"><CalendarDays className="h-4 w-4 mr-1" /> Calendar</TabsTrigger>
                <TabsTrigger value="list"><List className="h-4 w-4 mr-1" /> List</TabsTrigger>
              </TabsList>
              <TabsContent value="calendar" className="pt-3">
                <TimetableCalendar timetable={selected} onMutated={() => qc.invalidateQueries({ queryKey: ["time-table"] })} />
              </TabsContent>
              <TabsContent value="list" className="pt-3">
                <Table>
                  <TableHeader><TableRow>
                    <TableHead>Course</TableHead><TableHead>Sec</TableHead><TableHead>Instructor</TableHead>
                    <TableHead>Days</TableHead><TableHead>Time</TableHead><TableHead>Room</TableHead>
                  </TableRow></TableHeader>
                  <TableBody>
                    {selected.lectures?.map(l => (
                      <TableRow key={l.id}>
                        <TableCell className="font-medium">{l.course ? `${l.course.courseSymbol} ${l.course.courseNumber}` : "—"}</TableCell>
                        <TableCell>{l.number}</TableCell>
                        <TableCell>{l.instructor}</TableCell>
                        <TableCell>{l.timeSlot?.days?.map(d => d.slice(0, 3)).join(", ") ?? "—"}</TableCell>
                        <TableCell>{l.timeSlot ? `${l.timeSlot.startTime?.slice(0,5)}–${l.timeSlot.endTime?.slice(0,5)}` : "—"}</TableCell>
                        <TableCell>{l.room ? `${l.room.building} ${l.room.roomNumber}` : "—"}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
