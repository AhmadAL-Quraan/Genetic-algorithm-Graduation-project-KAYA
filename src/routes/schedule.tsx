import { createFileRoute, Link } from "@tanstack/react-router";
import {
  TimeTables,
  useGenerateTimetable,
  exportTimetableUrl,
  exportLatestUrl,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Trash2, Sparkles, Loader2, Download, Eye } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export const Route = createFileRoute("/schedule")({ component: SchedulePage });

function SchedulePage() {
  const { toast } = useToast();
  const list = TimeTables.useList();
  const remove = TimeTables.useDelete();
  const generate = useGenerateTimetable();

  const sorted = list.data
    ? [...list.data].sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
    : [];

  const onGenerate = async () => {
    try {
      const tt = await generate.mutateAsync();
      toast({
        title: "Timetable generated",
        description: `Penalty: ${tt.fitnessReport?.totalPenalty ?? "?"}`,
      });
    } catch (err) {
      toast({
        title: "Generation failed",
        description: (err as Error).message,
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Schedule</h1>
          <p className="text-sm text-muted-foreground">
            Run the genetic algorithm to produce a conflict-minimized timetable.
          </p>
        </div>
        {sorted[0] && (
          <a href={exportLatestUrl()} download>
            <Button variant="outline" size="sm" className="gap-2">
              <Download className="h-4 w-4" /> Export Latest
            </Button>
          </a>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Sparkles className="h-4 w-4" /> Generate
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Make sure you have added rooms, instructors, time slots, courses and
            lecture sections first. Then click Generate to run the genetic algorithm
            on the backend.
          </p>
          <Button
            onClick={onGenerate}
            disabled={generate.isPending}
            className="gap-2"
          >
            {generate.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" /> Running…
              </>
            ) : (
              <>
                <Sparkles className="h-4 w-4" /> Generate Timetable
              </>
            )}
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            Saved timetables ({list.data?.length ?? 0})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {list.isLoading ? (
            <div className="text-sm text-muted-foreground">Loading…</div>
          ) : !sorted.length ? (
            <div className="text-sm text-muted-foreground">
              No timetables yet — generate one above.
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Lectures</TableHead>
                  <TableHead>Penalty</TableHead>
                  <TableHead>Conflicts</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {sorted.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell>#{t.id}</TableCell>
                    <TableCell>{t.lectures?.length ?? 0}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          (t.fitnessReport?.totalPenalty ?? 0) === 0
                            ? "outline"
                            : "secondary"
                        }
                      >
                        {t.fitnessReport?.totalPenalty ?? "—"}
                      </Badge>
                    </TableCell>
                    <TableCell className="space-x-1 text-xs">
                      <Badge variant="outline">
                        R: {t.fitnessReport?.roomConflicts ?? 0}
                      </Badge>
                      <Badge variant="outline">
                        I: {t.fitnessReport?.instructorConflicts ?? 0}
                      </Badge>
                      <Badge variant="outline">
                        S: {t.fitnessReport?.studentConflicts ?? 0}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" asChild>
                        <Link
                          to="/schedule/$id"
                          params={{ id: String(t.id) }}
                        >
                          <Eye className="h-4 w-4" />
                        </Link>
                      </Button>
                      <a href={exportTimetableUrl(t.id)} download>
                        <Button variant="ghost" size="icon">
                          <Download className="h-4 w-4" />
                        </Button>
                      </a>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => remove.mutate(t.id)}
                      >
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
    </div>
  );
}