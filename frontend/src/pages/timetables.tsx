import { useEffect, useMemo, useRef, useState } from "react";
import { TimeTables, Courses, Rooms, TimeSlots, Lectures, type TimeTable, type GAConfig, exportTimetableUrl } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
  Trash2, Sparkles, CalendarDays, List, Info, ChevronDown, ChevronUp,
  TrendingUp, TrendingDown, Minus, Zap, CheckCircle2, XCircle, Loader2, Activity, X, Download,
} from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { TimetableCalendar } from "@/components/timetable-calendar";
import { useQueryClient } from "@tanstack/react-query";
import { Link } from "wouter";

const BASE = import.meta.env.BASE_URL?.replace(/\/$/, "") ?? "";

const METHOD_LABEL: Record<string, string> = {
  IN_PERSON: "In Person", ONLINE: "Online", BLENDED: "Blended",
};
const ROOM_LABEL: Record<string, string> = {
  LECTURE: "Lecture", LAB: "Lab", OTHER: "Other",
};

interface ReadinessCheck { label: string; ok: boolean; hint: string; href: string; }

function useReadinessChecks() {
  const courses   = Courses.useList();
  const rooms     = Rooms.useList();
  const timeSlots = TimeSlots.useList();
  const lectures  = Lectures.useList();

  const checks = useMemo<ReadinessCheck[] | null>(() => {
    if (!courses.data || !rooms.data || !timeSlots.data || !lectures.data) return null;

    const items: ReadinessCheck[] = [];

    items.push({
      label: "At least one course added",
      ok: courses.data.length > 0,
      hint: "Go to Courses and add at least one course.",
      href: "/courses",
    });

    items.push({
      label: "At least one lecture section defined",
      ok: lectures.data.length > 0,
      hint: "Go to Lectures and add sections for your courses. Each section is one row the algorithm will schedule.",
      href: "/lectures",
    });

    const neededRoomTypes = [...new Set(courses.data.flatMap(c => c.roomGroups))];
    for (const rt of neededRoomTypes) {
      const hasRoom = rooms.data.some(r => r.roomType === rt);
      items.push({
        label: `${ROOM_LABEL[rt] ?? rt} room exists`,
        ok: hasRoom,
        hint: `Add at least one "${ROOM_LABEL[rt] ?? rt}" room on the Rooms page.`,
        href: "/rooms",
      });
    }

    const neededMethods = [...new Set(courses.data.flatMap(c => c.timeGroups))];
    for (const method of neededMethods) {
      const hasSlot = timeSlots.data.some(ts => ts.teachingMethod === method);
      items.push({
        label: `Time window for "${METHOD_LABEL[method] ?? method}" defined`,
        ok: hasSlot,
        hint: `Add a time window for the "${METHOD_LABEL[method] ?? method}" section on the Time Slots page.`,
        href: "/time-slots",
      });
    }

    return items;
  }, [courses.data, rooms.data, timeSlots.data, lectures.data]);

  const loading = courses.isLoading || rooms.isLoading || timeSlots.isLoading || lectures.isLoading;
  const allOk   = checks != null && checks.every(c => c.ok);
  return { checks, allOk, loading };
}

function ReadinessPanel({ checks, loading }: { checks: ReadinessCheck[] | null; loading: boolean }) {
  const [collapsed, setCollapsed] = useState(false);

  if (loading || !checks) return null;

  const failing = checks.filter(c => !c.ok);
  const allOk   = failing.length === 0;

  return (
      <div className={`rounded-xl border p-4 space-y-3 ${
          allOk
              ? "border-green-200 bg-green-50"
              : "border-amber-200 bg-amber-50"
      }`}>
        <button
            type="button"
            onClick={() => setCollapsed(v => !v)}
            className="w-full flex items-center justify-between gap-2 text-left"
        >
          <p className={`text-sm font-semibold ${allOk ? "text-green-800" : "text-amber-800"}`}>
            {allOk
                ? "✓ All requirements met — ready to generate"
                : `${failing.length} requirement${failing.length > 1 ? "s" : ""} missing before you can generate`}
          </p>
          {allOk && (
              <span className={`text-xs ${collapsed ? "text-green-600" : "text-green-500"}`}>
            {collapsed ? "show ▾" : "hide ▴"}
          </span>
          )}
        </button>

        {(!collapsed || !allOk) && (
            <ul className="space-y-2">
              {checks.map((c, i) => (
                  <li key={i} className="flex items-start gap-2.5 text-sm">
                    {c.ok
                        ? <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 shrink-0" />
                        : <XCircle     className="h-4 w-4 text-red-500   mt-0.5 shrink-0" />}
                    <span className={c.ok ? "text-green-700" : "text-foreground"}>
                {c.ok ? c.label : (
                    <>
                      {c.hint}{" "}
                      <Link href={c.href} className="font-medium text-amber-700 underline underline-offset-2 hover:text-amber-900">
                        Go &rarr;
                      </Link>
                    </>
                )}
              </span>
                  </li>
              ))}
            </ul>
        )}
      </div>
  );
}

const PARAM_GUIDE = [
  {
    name: "Generations", field: "maxGenerations" as keyof GAConfig, default: 400, step: 1,
    description: "Maximum number of evolutionary cycles. (الحد الأقصى لعدد الأجيال)",
    higher: { effect: "Better schedules, finds fewer conflicts", cost: "Slower — takes more time" },
    lower:  { effect: "Faster results", cost: "May miss a good solution" },
    recommended: "400",
  },
  {
    name: "Population", field: "populationSize" as keyof GAConfig, default: 100, step: 1,
    description: "Number of candidate timetables evaluated per generation. (حجم الشعبة في الجيل الواحد)",
    higher: { effect: "More diverse solutions", cost: "Much slower per generation" },
    lower:  { effect: "Very fast", cost: "Low diversity — often converges early" },
    recommended: "100",
  },
  {
    name: "Tournament", field: "tournamentSize" as keyof GAConfig, default: 5, step: 1,
    description: "Subset size for the parent selection competition. (عدد الجداول المتنافسة في كل جولة تزاوج)",
    higher: { effect: "Strong selection pressure", cost: "Premature convergence risk" },
    lower:  { effect: "More diverse parents", cost: "Slower improvement" },
    recommended: "5",
  },
  {
    name: "Elitism Ratio", field: "elitismRatio" as keyof GAConfig, default: 0.015, step: 0.005,
    description: "Fraction of top schedules passed unchanged. (نسبة مئوية لأفضل جداول تنتقل للجيل القادم، 0.015 تعني 1.5%)",
    higher: { effect: "Best solution always preserved", cost: "Reduces diversity" },
    lower:  { effect: "More exploration", cost: "May lose good solutions" },
    recommended: "0.01 - 0.05",
  },
  {
    name: "Mutation Rate", field: "initialMutationRate" as keyof GAConfig, default: 0.15, step: 0.01,
    description: "Base probability of a chromosome undergoing mutation. (احتمالية حدوث طفرة للجدول 15%)",
    higher: { effect: "More exploration", cost: "Too high → random walk" },
    lower:  { effect: "Stable improvement", cost: "Gets stuck in local minima" },
    recommended: "0.15",
  },
  {
    name: "Mutation Impact", field: "mutationImpactRatio" as keyof GAConfig, default: 0.10, step: 0.01,
    description: "Mutate 10% of conflicting courses when a mutation triggers. (نسبة التعديل عند حدوث طفرة)",
    higher: { effect: "Large structural changes", cost: "Can destroy good partial solutions" },
    lower:  { effect: "Fine-grained adjustments", cost: "Very slow on hard problems" },
    recommended: "0.10",
  },
  {
    name: "Stagnation Tolerance", field: "stagnationToleranceRatio" as keyof GAConfig, default: 0.10, step: 0.01,
    description: "Trigger adaptive mutation after stagnating for 10% of maxGenerations. (تحفيز الطفرة عند الركود)",
    higher: { effect: "Wait longer before boosting mutation", cost: "Algorithm might get stuck" },
    lower:  { effect: "Quickly boosts mutation", cost: "Might disrupt stable progress" },
    recommended: "0.10",
  },
  {
    name: "Islands Count", field: "numIslands" as keyof GAConfig, default: 4, step: 1,
    description: "Divide schedules into islands for parallel processing. (عدد الجزر المعزولة لزيادة سرعة المعالجة)",
    higher: { effect: "More isolation & parallelism", cost: "Fewer schedules per island" },
    lower:  { effect: "Larger island populations", cost: "Less parallel processing speed" },
    recommended: "4",
  },
  {
    name: "Migration Interval", field: "migrationInterval" as keyof GAConfig, default: 20, step: 1,
    description: "Exchange genetic material every 20 generations. (عدد الأجيال قبل الهجرة بين الجزر)",
    higher: { effect: "More independent island evolution", cost: "Late sharing of good traits" },
    lower:  { effect: "Frequent sharing of traits", cost: "Islands become too similar (low diversity)" },
    recommended: "20",
  },
  {
    name: "Migration Rate", field: "migrationRate" as keyof GAConfig, default: 2, step: 1,
    description: "Transfer the top schedules during migration. (عدد الجداول المهاجرة بين الجزر)",
    higher: { effect: "Faster spread of elite schedules", cost: "Can overwrite local island diversity" },
    lower:  { effect: "Maintains high island diversity", cost: "Slow spread of good solutions" },
    recommended: "2",
  },
];

function ParamGuide() {
  const [open, setOpen] = useState(false);
  return (
      <div className="border rounded-lg overflow-hidden">
        <button onClick={() => setOpen(v => !v)}
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
                over many generations. A lower <strong>fitness penalty = fewer conflicts</strong>.
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
                            <span className="font-medium text-green-700">Higher: </span>
                            <span className="text-muted-foreground">{p.higher.effect}</span>
                            <span className="text-red-500"> — but: </span>
                            <span className="text-muted-foreground">{p.higher.cost}</span>
                          </div>
                        </div>
                        <div className="flex items-start gap-2 rounded bg-blue-50 dark:bg-blue-950/30 p-2">
                          <TrendingDown className="h-3.5 w-3.5 text-blue-600 mt-0.5 shrink-0" />
                          <div>
                            <span className="font-medium text-blue-700">Lower: </span>
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

function FitnessChart({ history }: { history: number[] }) {
  if (history.length < 2) return (
      <div className="h-24 flex items-center justify-center text-xs text-muted-foreground">
        Collecting data…
      </div>
  );

  const W = 600, H = 100, pad = 8;
  const min = Math.min(...history);
  const max = Math.max(...history);
  const range = max - min || 1;

  const pts = history.map((v, i) => {
    const x = pad + (i / Math.max(history.length - 1, 1)) * (W - pad * 2);
    const y = H - pad - ((v - min) / range) * (H - pad * 2);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  });

  const lastPt = pts[pts.length - 1].split(",");
  const lx = parseFloat(lastPt[0]);
  const ly = parseFloat(lastPt[1]);

  const areaPath = `M ${pts[0]} ${pts.slice(1).map(p => `L ${p}`).join(" ")} L ${lx.toFixed(1)},${H - pad} L ${pad},${H - pad} Z`;

  return (
      <svg viewBox={`0 0 ${W} ${H}`} className="w-full h-24" preserveAspectRatio="none">
        <defs>
          <linearGradient id="fitnessGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#B8860B" stopOpacity="0.25" />
            <stop offset="100%" stopColor="#B8860B" stopOpacity="0" />
          </linearGradient>
        </defs>
        <path d={areaPath} fill="url(#fitnessGrad)" />
        <polyline points={pts.join(" ")} fill="none" stroke="#B8860B"
                  strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
        <circle cx={lx} cy={ly} r="5" fill="#B8860B" />
      </svg>
  );
}

function LiveProgressPanel({ progress, history, onDismiss }: {
  progress: ProgressState;
  history: number[];
  onDismiss: () => void;
}) {
  const isPerfect  = progress.phase === "perfect";
  const isSaving   = progress.phase === "saving";
  const isInit     = progress.phase === "initializing";
  const isEvolving = progress.phase === "evolving";

  const pct = progress.maxGenerations > 0
      ? Math.min(100, Math.round((progress.generation / progress.maxGenerations) * 100))
      : 0;

  const phaseLabel =
      isPerfect  ? "Perfect schedule found!" :
          isSaving   ? "Saving timetable…"       :
              isInit     ? "Initializing population…" :
                  "Evolving chromosomes...";

  return (
      <Card className="border shadow-sm">
        <CardContent className="pt-5 pb-5 space-y-5">

          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              {isPerfect ? (
                  <CheckCircle2 className="h-5 w-5 text-green-500" />
              ) : isSaving ? (
                  <Loader2 className="h-5 w-5 text-primary animate-spin" />
              ) : isInit ? (
                  <Loader2 className="h-5 w-5 text-primary animate-spin" />
              ) : (
                  <Activity className="h-5 w-5 text-[#B8860B]" />
              )}
              <span className="text-lg font-semibold">{phaseLabel}</span>
            </div>
            <button
                onClick={onDismiss}
                className="rounded-md p-1 text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
                aria-label="Dismiss"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Initializing message */}
          {isInit && (
              <p className="text-sm text-muted-foreground">
                Building the initial population of candidate timetables…
              </p>
          )}

          {/* Saving message */}
          {isSaving && (
              <p className="text-sm text-muted-foreground">
                GA finished. Persisting the best timetable to the database…
              </p>
          )}

          {(isEvolving || isPerfect) && (
              <>
                {/* Generation + Fitness row */}
                <div className="flex items-end justify-between">
                  <div>
                    <p className="text-xs text-muted-foreground mb-0.5">Generation</p>
                    <p className="text-3xl font-bold tabular-nums leading-none">
                      {progress.generation}
                      <span className="text-base font-normal text-muted-foreground"> / {progress.maxGenerations}</span>
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-muted-foreground mb-0.5">Best fitness penalty</p>
                    <p className={`text-3xl font-bold tabular-nums leading-none ${
                        isPerfect || progress.bestFitness === 0
                            ? "text-green-500"
                            : "text-orange-500"
                    }`}>
                      {progress.bestFitness}
                    </p>
                  </div>
                </div>

                {/* Progress bar */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Progress</span>
                    <span>{isPerfect ? 100 : pct}%</span>
                  </div>
                  <div className="h-2.5 rounded-full bg-muted overflow-hidden">
                    <div
                        className="h-full rounded-full transition-all duration-300"
                        style={{
                          width: `${isPerfect ? 100 : pct}%`,
                          backgroundColor: isPerfect ? "#22c55e" : "#B8860B",
                        }}
                    />
                  </div>
                </div>

                {/* Conflict cards */}
                <div className="grid grid-cols-3 gap-3">
                  <div className="rounded-xl border border-red-100 bg-red-50 p-4 text-center">
                    <p className="text-xs text-muted-foreground mb-1">Room</p>
                    <p className={`text-2xl font-bold tabular-nums ${progress.roomConflicts === 0 ? "text-green-500" : "text-red-500"}`}>
                      {progress.roomConflicts}
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">conflicts</p>
                  </div>
                  <div className="rounded-xl border border-amber-100 bg-amber-50 p-4 text-center">
                    <p className="text-xs text-muted-foreground mb-1">Instructor</p>
                    <p className={`text-2xl font-bold tabular-nums ${progress.instructorConflicts === 0 ? "text-green-500" : "text-amber-500"}`}>
                      {progress.instructorConflicts}
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">conflicts</p>
                  </div>
                  <div className="rounded-xl border border-blue-100 bg-blue-50 p-4 text-center">
                    <p className="text-xs text-muted-foreground mb-1">Student</p>
                    <p className={`text-2xl font-bold tabular-nums ${progress.studentConflicts === 0 ? "text-green-500" : "text-blue-500"}`}>
                      {progress.studentConflicts}
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">conflicts</p>
                  </div>
                </div>

                {/* Adaptive mutation banner */}
                {progress.mutationRate > 0.16 && (
                    <div className="flex items-center gap-2 rounded-lg border border-purple-200 bg-purple-50 px-4 py-2.5 text-sm text-purple-700">
                      <Zap className="h-4 w-4 shrink-0 text-purple-500" />
                      Adaptive mutation active — rate boosted to {(progress.mutationRate * 100).toFixed(0)}% to escape local optima
                    </div>
                )}

                {/* Perfect banner */}
                {isPerfect && (
                    <div className="flex items-center gap-2 rounded-lg border border-green-200 bg-green-50 px-4 py-2.5 text-sm text-green-700">
                      <CheckCircle2 className="h-4 w-4 shrink-0" />
                      Zero-conflict schedule achieved at generation {progress.generation}!
                    </div>
                )}

                {/* Fitness chart */}
                <div>
                  <p className="text-xs text-muted-foreground mb-2">Fitness over time</p>
                  <FitnessChart history={history} />
                </div>
              </>
          )}
        </CardContent>
      </Card>
  );
}

export default function TimetablesPage() {
  const { toast } = useToast();
  const qc     = useQueryClient();
  const list   = TimeTables.useList();
  const remove = TimeTables.useDelete();
  const { checks, allOk, loading: readinessLoading } = useReadinessChecks();

  const [cfg, setCfg] = useState<GAConfig & { useIslandModel: boolean }>({
    maxGenerations: 400,
    populationSize: 100,
    tournamentSize: 5,
    elitismRatio: 0.015,
    initialMutationRate: 0.15,
    mutationImpactRatio: 0.10,
    stagnationToleranceRatio: 0.10,
    numIslands: 4,
    migrationInterval: 20,
    migrationRate: 2,
    useIslandModel: false,
  });

  const [selectedId, setSelectedId]         = useState<number | null>(null);
  const [generating, setGenerating]         = useState(false);
  const [progress, setProgress]             = useState<ProgressState | null>(null);
  const [fitnessHistory, setFitnessHistory] = useState<number[]>([]);
  const abortRef = useRef<AbortController | null>(null);

  const sortedTimetables = list.data
      ? [...list.data].sort((a, b) => (a.id ?? 0) - (b.id ?? 0))
      : [];

  useEffect(() => {
    if (selectedId == null && list.data?.length) {
      const latest = [...list.data].sort((a, b) => (b.id ?? 0) - (a.id ?? 0))[0];
      setSelectedId(latest.id);
    }
  }, [list.data, selectedId]);

  const selected: TimeTable | null = list.data?.find(t => t.id === selectedId) ?? null;
  const selectedDisplayNum = selected
      ? sortedTimetables.findIndex(t => t.id === selected.id) + 1
      : null;

  const onGenerate = async () => {
    // Stop any previous poll loop
    abortRef.current?.abort();
    const ac = new AbortController();
    abortRef.current = ac;

    setGenerating(true);
    setProgress(null);
    setFitnessHistory([]);

    // Start polling /progress every 400 ms in the background
    const pollInterval = setInterval(async () => {
      if (ac.signal.aborted) { clearInterval(pollInterval); return; }
      try {
        const r = await fetch(`${BASE}/api/generator/progress`, { signal: ac.signal });
        if (r.status === 204) return; // no progress yet
        if (!r.ok) return;
        const p = (await r.json()) as ProgressState;
        setProgress(p);
        if (p.phase === "evolving" || p.phase === "perfect") {
          setFitnessHistory(h => {
            // Only append if fitness changed or first entry
            if (h.length === 0 || h[h.length - 1] !== p.bestFitness) {
              return [...h, p.bestFitness];
            }
            return h;
          });
        }
      } catch {
        // AbortError or network glitch — ignore, generation may still be running
      }
    }, 400);

    try {
      // Fire the long-running POST (blocks until GA completes)
      const res = await fetch(`${BASE}/api/generator/generate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cfg),
        signal: ac.signal,
      });

      clearInterval(pollInterval);

      if (!res.ok) {
        const text = await res.text().catch(() => "");
        let msg = res.statusText;
        try { const j = JSON.parse(text); msg = j.message || j.error || text; }
        catch { msg = text || res.statusText; }
        throw new Error(msg);
      }

      const tt = (await res.json()) as TimeTable;
      qc.invalidateQueries({ queryKey: ["time-table"] });
      setSelectedId(tt.id);
      toast({ title: "Timetable generated", description: `Fitness penalty: ${tt.fitness}` });

      // Replace the "saving" spinner with a done state, then auto-dismiss after 3s
      setProgress(p => p ? { ...p, phase: "evolving" } : p);
      setTimeout(() => setProgress(null), 3000);
    } catch (err: any) {
      clearInterval(pollInterval);
      if (err.name !== "AbortError") {
        toast({ title: "Generation failed", description: err.message, variant: "destructive" });
        setProgress(null);
      } else {
        setProgress(null);
      }
    } finally {
      setGenerating(false);
    }
  };

  return (
      <div className="space-y-6">
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-2xl font-semibold">Algorithm Setting</h1>
            <p className="text-muted-foreground text-sm">
              Run the genetic algorithm to produce a conflict-minimized timetable.
            </p>
          </div>
          {selectedId != null && (
              <a href={exportTimetableUrl(selectedId)} download>
                <Button variant="outline" size="sm" className="gap-2">
                  <Download className="h-4 w-4" /> Export Latest
                </Button>
              </a>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Sparkles className="h-4 w-4" /> Algorithm Parameters
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">

            {/* زر تفعيل معمارية الجزر */}
            <div className="flex items-center gap-3 p-3 mb-4 rounded-lg border bg-muted/20">
              <input
                  type="checkbox"
                  id="useIslandModel"
                  checked={cfg.useIslandModel}
                  onChange={(e) => setCfg({ ...cfg, useIslandModel: e.target.checked })}
                  disabled={generating}
                  className="h-5 w-5 accent-primary cursor-pointer"
              />
              <div className="flex flex-col">
                <Label htmlFor="useIslandModel" className="font-semibold text-base cursor-pointer">
                  Enable Island Model Architecture
                </Label>
                <p className="text-xs text-muted-foreground">
                  Uses Parallel Streams to evolve multiple isolated populations for faster and more diverse results.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              {PARAM_GUIDE.map(p => (
                  <div key={p.field}>
                    <Label className="text-xs">{p.name}</Label>
                    <Input
                        type="number"
                        step={p.step}
                        value={cfg[p.field] ?? p.default}
                        onChange={e => setCfg({ ...cfg, [p.field]: +e.target.value })}
                        disabled={generating}
                        className="mt-1"
                    />
                  </div>
              ))}
            </div>

            <ParamGuide />

            <ReadinessPanel checks={checks} loading={readinessLoading} />

            <div className="flex items-center gap-3">
              <Button onClick={onGenerate} disabled={generating || !allOk} className="gap-2">
                {generating
                    ? <><Loader2 className="h-4 w-4 animate-spin" />Running…</>
                    : <><Sparkles className="h-4 w-4" />Generate Timetable</>}
              </Button>
              {generating && (
                  <Button
                      variant="outline"
                      className="gap-2 border-red-300 text-red-600 hover:bg-red-50 hover:text-red-700"
                      onClick={async () => {
                        try { await fetch(`${BASE}/api/generator/cancel`, { method: "POST" }); } catch {}                        abortRef.current?.abort();
                      }}
                  >
                    <span className="h-2 w-2 rounded-full bg-red-500 inline-block" />
                    Cancel
                  </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Inline live progress panel */}
        {progress && (
            <LiveProgressPanel
                progress={progress}
                history={fitnessHistory}
                onDismiss={() => setProgress(null)}
            />
        )}

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              Saved timetables ({list.data?.length ?? 0})
            </CardTitle>
          </CardHeader>
          <CardContent>
            {list.isLoading ? (
                <div className="text-sm text-muted-foreground">Loading…</div>
            ) : !list.data?.length ? (
                <div className="text-sm text-muted-foreground">
                  No timetables yet — generate one above.
                </div>
            ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Generated</TableHead>
                      <TableHead>Lectures</TableHead>
                      <TableHead>Fitness penalty</TableHead>
                      <TableHead />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sortedTimetables.map((t, idx) => (
                        <TableRow
                            key={t.id}
                            className={`cursor-pointer ${selectedId === t.id ? "bg-muted/60" : ""}`}
                            onClick={() => setSelectedId(t.id)}>
                          <TableCell>#{idx + 1}</TableCell>
                          <TableCell>
                            {t.generatedAt ? new Date(t.generatedAt).toLocaleString() : "—"}
                          </TableCell>
                          <TableCell>{t.lectures?.length ?? 0}</TableCell>
                          <TableCell>
                            <Badge variant={t.fitness === 0 ? "default" : "secondary"}>
                              {t.fitness}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button variant="ghost" size="icon"
                                    onClick={e => {
                                      e.stopPropagation();
                                      remove.mutate(t.id);
                                      if (selectedId === t.id) setSelectedId(null);
                                      // Immediately clear the progress panel when any timetable is deleted
                                      setProgress(null);
                                    }}>
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                    ))}
                  </TableBody>
                </Table>
            )}
          </CardContent>
        </Card>

        {selected && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Timetable #{selectedDisplayNum}</CardTitle>
                {selected.fitnessReport && (
                    <div className="flex flex-wrap gap-2 pt-1 text-xs">
                      <Badge variant={selected.fitnessReport.roomConflicts > 0 ? "destructive" : "outline"}>
                        Room conflicts: {selected.fitnessReport.roomConflicts}
                        {selected.fitnessReport.roomConflicts > 0 && ` (−${selected.fitnessReport.roomConflicts * 10})`}
                      </Badge>
                      <Badge variant={selected.fitnessReport.instructorConflicts > 0 ? "destructive" : "outline"}>
                        Instructor conflicts: {selected.fitnessReport.instructorConflicts}
                        {selected.fitnessReport.instructorConflicts > 0 && ` (−${selected.fitnessReport.instructorConflicts * 10})`}
                      </Badge>
                      <Badge variant={selected.fitnessReport.studentConflicts > 0 ? "destructive" : "outline"}>
                        Student conflicts: {selected.fitnessReport.studentConflicts}
                        {selected.fitnessReport.studentConflicts > 0 && ` (−${selected.fitnessReport.studentConflicts * 20})`}
                      </Badge>
                      <Badge variant={selected.fitnessReport.totalPenalty === 0 ? "outline" : "default"}>
                        Total penalty: {selected.fitnessReport.totalPenalty}
                      </Badge>
                    </div>
                )}
              </CardHeader>
              <CardContent>
                <Tabs defaultValue="calendar">
                  <TabsList>
                    <TabsTrigger value="calendar">
                      <CalendarDays className="h-4 w-4 mr-1" /> Calendar
                    </TabsTrigger>
                    <TabsTrigger value="list">
                      <List className="h-4 w-4 mr-1" /> List
                    </TabsTrigger>
                  </TabsList>
                  <TabsContent value="calendar" className="pt-3">
                    <TimetableCalendar
                        timetable={selected}
                        onMutated={() => qc.invalidateQueries({ queryKey: ["time-table"] })}
                    />
                  </TabsContent>
                  <TabsContent value="list" className="pt-3">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Course</TableHead>
                          <TableHead>Sec</TableHead>
                          <TableHead>Instructor</TableHead>
                          <TableHead>Days</TableHead>
                          <TableHead>Time</TableHead>
                          <TableHead>Room</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {selected.lectures?.map(l => (
                            <TableRow key={l.id}>
                              <TableCell className="font-medium">
                                {l.course ? `${l.course.courseSymbol} ${l.course.courseNumber}` : "—"}
                              </TableCell>
                              {/* التعديل: قراءة رقم الشعبة الصحيح من الباك إند */}
                              <TableCell>{(l as any).sectionNumber ?? "—"}</TableCell>

                              {/* التعديل الأهم: قراءة الاسم من الأوبجيكت لمنع الكراش */}
                              <TableCell>{l.instructor?.name ?? "—"}</TableCell>

                              <TableCell>
                                {l.timeSlot?.days?.map(d => d.slice(0, 3)).join(", ") ?? "—"}
                              </TableCell>
                              <TableCell>
                                {l.timeSlot
                                    ? `${l.timeSlot.startTime?.slice(0, 5)}–${l.timeSlot.endTime?.slice(0, 5)}`
                                    : "—"}
                              </TableCell>
                              <TableCell>
                                {l.room ? `${l.room.building} ${l.room.roomNumber}` : "—"}
                              </TableCell>
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