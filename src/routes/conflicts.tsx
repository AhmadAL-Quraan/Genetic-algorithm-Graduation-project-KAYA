import { createFileRoute } from "@tanstack/react-router";
import { useConflicts } from "@/lib/api";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, CheckCircle2, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useQueryClient } from "@tanstack/react-query";

export const Route = createFileRoute("/conflicts")({ component: ConflictsPage });

const TYPE_LABELS: Record<
  string,
  { label: string; color: "destructive" | "secondary" | "outline" }
> = {
  ROOM: { label: "Room", color: "destructive" },
  TEACHER: { label: "Instructor", color: "secondary" },
  INSTRUCTOR: { label: "Instructor", color: "secondary" },
  STUDENT: { label: "Student", color: "outline" },
};

function ConflictsPage() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useConflicts();

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Conflicts</h1>
          <p className="text-sm text-muted-foreground">
            Room, instructor, and student-group scheduling conflicts detected across all lectures.
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => qc.invalidateQueries({ queryKey: ["conflicts"] })}
        >
          <RefreshCw className="mr-2 h-4 w-4" /> Refresh
        </Button>
      </div>

      {isLoading && (
        <div className="text-sm text-muted-foreground">Checking for conflicts…</div>
      )}

      {error && (
        <Card className="border-destructive">
          <CardContent className="pt-4 text-sm text-destructive">
            {(error as Error).message}
          </CardContent>
        </Card>
      )}

      {!isLoading && !error && data?.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center gap-3 py-12">
            <CheckCircle2 className="h-10 w-10 text-green-500" />
            <p className="font-medium">No conflicts detected</p>
            <p className="text-sm text-muted-foreground">
              All lectures are free of room, instructor, and student-group overlaps.
            </p>
          </CardContent>
        </Card>
      )}

      {!isLoading && (data?.length ?? 0) > 0 && (
        <div className="space-y-3">
          {data!.map((c, i) => {
            const meta = TYPE_LABELS[c.type] ?? { label: c.type, color: "outline" as const };
            return (
              <Card key={i}>
                <CardHeader className="pb-2">
                  <div className="flex items-start gap-3">
                    <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-destructive" />
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <Badge variant={meta.color}>{meta.label}</Badge>
                        <span className="text-sm font-medium">{c.message}</span>
                      </div>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pt-0">
                  <div className="grid grid-cols-2 gap-2 text-xs text-muted-foreground sm:grid-cols-4">
                    <div>
                      <span className="font-medium text-foreground">Course A</span>
                      <br />
                      {c.courseA}
                    </div>
                    <div>
                      <span className="font-medium text-foreground">Course B</span>
                      <br />
                      {c.courseB}
                    </div>
                    <div>
                      <span className="font-medium text-foreground">Instructor A</span>
                      <br />
                      {c.instructorA || "—"}
                    </div>
                    <div>
                      <span className="font-medium text-foreground">Instructor B</span>
                      <br />
                      {c.instructorB || "—"}
                    </div>
                    <div className="sm:col-span-2">
                      <span className="font-medium text-foreground">Overlapping Time</span>
                      <br />
                      {c.timeSlot}
                    </div>
                    <div className="text-[10px]">
                      Lecture IDs: #{c.lectureAId} &amp; #{c.lectureBId}
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}