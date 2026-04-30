import { useConflicts } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, CheckCircle2, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useQueryClient } from "@tanstack/react-query";

const TYPE_LABELS: Record<string, { label: string; color: "destructive" | "secondary" | "outline" }> = {
  ROOM:    { label: "Room",    color: "destructive" },
  TEACHER: { label: "Doctor",  color: "secondary"   },
  STUDENT: { label: "Student", color: "outline"     },
};

export default function ConflictsPage() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useConflicts();

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Conflicts</h1>
          <p className="text-muted-foreground text-sm">
            Room, doctor, and student-group scheduling conflicts detected across all lectures.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={() => qc.invalidateQueries({ queryKey: ["conflicts"] })}>
          <RefreshCw className="h-4 w-4 mr-2" /> Refresh
        </Button>
      </div>

      {isLoading && (
        <div className="text-sm text-muted-foreground">Checking for conflicts…</div>
      )}

      {error && (
        <Card className="border-destructive">
          <CardContent className="pt-4 text-sm text-destructive">{(error as Error).message}</CardContent>
        </Card>
      )}

      {!isLoading && !error && data?.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center gap-3 py-12">
            <CheckCircle2 className="h-10 w-10 text-green-500" />
            <p className="font-medium">No conflicts detected</p>
            <p className="text-sm text-muted-foreground">All lectures are free of room, doctor, and student-group overlaps.</p>
          </CardContent>
        </Card>
      )}

      {!isLoading && (data?.length ?? 0) > 0 && (
        <>
          <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
            <span className="flex items-center gap-1">
              <AlertTriangle className="h-4 w-4 text-destructive" />
              {data!.filter(c => c.type === "ROOM").length} room conflict(s)
            </span>
            <span className="flex items-center gap-1">
              <AlertTriangle className="h-4 w-4 text-yellow-500" />
              {data!.filter(c => c.type === "TEACHER").length} doctor conflict(s)
            </span>
            <span className="flex items-center gap-1">
              <AlertTriangle className="h-4 w-4 text-blue-500" />
              {data!.filter(c => c.type === "STUDENT").length} student-group conflict(s)
            </span>
          </div>

          <div className="space-y-3">
            {data!.map((conflict, i) => {
              const meta = TYPE_LABELS[conflict.type] ?? { label: conflict.type, color: "outline" as const };
              return (
                <Card key={i}>
                  <CardHeader className="pb-2">
                    <div className="flex items-start gap-3">
                      <AlertTriangle className="h-4 w-4 mt-0.5 text-destructive shrink-0" />
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <Badge variant={meta.color}>{meta.label}</Badge>
                          <span className="text-sm font-medium">{conflict.message}</span>
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs text-muted-foreground">
                      <div><span className="font-medium text-foreground">Course A</span><br />{conflict.courseA}</div>
                      <div><span className="font-medium text-foreground">Course B</span><br />{conflict.courseB}</div>
                      {(conflict.instructorA || conflict.instructorB) && (
                        <>
                          <div><span className="font-medium text-foreground">Doctor A</span><br />{conflict.instructorA || "—"}</div>
                          <div><span className="font-medium text-foreground">Doctor B</span><br />{conflict.instructorB || "—"}</div>
                        </>
                      )}
                      <div className="sm:col-span-2">
                        <span className="font-medium text-foreground">Overlapping Time</span><br />{conflict.timeSlot}
                      </div>
                      <div className="text-[10px]">
                        Lecture IDs: #{conflict.lectureAId} &amp; #{conflict.lectureBId}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}
