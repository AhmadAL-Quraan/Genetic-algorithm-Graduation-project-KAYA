import { useParams, useLocation } from "wouter";
import { TimeTables, exportTimetableUrl } from "@/lib/api";
import { CalendarInner } from "@/components/timetable-calendar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, Download } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";

export default function TimetableViewPage() {
  const { id } = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const qc = useQueryClient();
  const list = TimeTables.useList();

  const ttId = Number(id);
  const timetable = list.data?.find(t => t.id === ttId) ?? null;
  const sortedList = list.data ? [...list.data].sort((a, b) => (a.id ?? 0) - (b.id ?? 0)) : [];
  const displayNum = timetable ? sortedList.findIndex(t => t.id === ttId) + 1 : null;

  if (list.isLoading) {
    return (
      <div className="flex items-center justify-center h-48 text-sm text-muted-foreground">
        Loading timetable…
      </div>
    );
  }

  if (!timetable) {
    return (
      <div className="space-y-4">
        <Button variant="outline" size="sm" onClick={() => navigate("/schedule")} className="gap-2">
          <ArrowLeft className="h-4 w-4" /> Back to Schedule
        </Button>
        <p className="text-sm text-muted-foreground">Timetable not found.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <Button variant="outline" size="sm" onClick={() => navigate("/schedule")} className="gap-2 shrink-0">
            <ArrowLeft className="h-4 w-4" /> Schedule
          </Button>
          <div>
            <h1 className="text-xl font-semibold">Timetable #{displayNum}</h1>
            {timetable.generatedAt && (
              <p className="text-xs text-muted-foreground">
                Generated {new Date(timetable.generatedAt).toLocaleString()}
              </p>
            )}
          </div>
        </div>
        <div className="flex items-center gap-3 flex-wrap">
          {timetable.fitnessReport && (
            <div className="flex flex-wrap gap-2">
              <Badge variant={timetable.fitnessReport.roomConflicts > 0 ? "destructive" : "outline"}>
                Room: {timetable.fitnessReport.roomConflicts}
                {timetable.fitnessReport.roomConflicts > 0 && ` (−${timetable.fitnessReport.roomConflicts * 10})`}
              </Badge>
              <Badge variant={timetable.fitnessReport.instructorConflicts > 0 ? "destructive" : "outline"}>
                Instructor: {timetable.fitnessReport.instructorConflicts}
                {timetable.fitnessReport.instructorConflicts > 0 && ` (−${timetable.fitnessReport.instructorConflicts * 10})`}
              </Badge>
              <Badge variant={timetable.fitnessReport.studentConflicts > 0 ? "destructive" : "outline"}>
                Student: {timetable.fitnessReport.studentConflicts}
                {timetable.fitnessReport.studentConflicts > 0 && ` (−${timetable.fitnessReport.studentConflicts * 20})`}
              </Badge>
              <Badge variant={timetable.fitnessReport.totalPenalty === 0 ? "outline" : "default"}>
                Penalty: {timetable.fitnessReport.totalPenalty}
              </Badge>
            </div>
          )}
          <a href={exportTimetableUrl(timetable.id)} download>
            <Button variant="outline" size="sm" className="gap-2">
              <Download className="h-4 w-4" /> Export
            </Button>
          </a>
        </div>
      </div>

      {/* Full-height calendar */}
      <CalendarInner
        timetable={timetable}
        onMutated={() => qc.invalidateQueries({ queryKey: ["time-table"] })}
        isFullscreen
      />
    </div>
  );
}
