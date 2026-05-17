import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { TimeTables, exportTimetableUrl } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ArrowLeft, Download } from "lucide-react";

export const Route = createFileRoute("/schedule/$id")({ component: TimetableView });

function TimetableView() {
  const { id } = useParams({ from: "/schedule/$id" });
  const list = TimeTables.useList();
  const ttId = Number(id);
  const tt = list.data?.find((t) => t.id === ttId) ?? null;

  if (list.isLoading) {
    return (
      <div className="flex h-48 items-center justify-center text-sm text-muted-foreground">
        Loading timetable…
      </div>
    );
  }

  if (!tt) {
    return (
      <div className="space-y-4">
        <Link to="/schedule">
          <Button variant="outline" size="sm" className="gap-2">
            <ArrowLeft className="h-4 w-4" /> Back to Schedule
          </Button>
        </Link>
        <p className="text-sm text-muted-foreground">Timetable not found.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link to="/schedule">
            <Button variant="outline" size="sm" className="gap-2">
              <ArrowLeft className="h-4 w-4" /> Schedule
            </Button>
          </Link>
          <h1 className="text-xl font-semibold">Timetable #{tt.id}</h1>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {tt.fitnessReport && (
            <>
              <Badge
                variant={
                  tt.fitnessReport.roomConflicts > 0 ? "destructive" : "outline"
                }
              >
                Room: {tt.fitnessReport.roomConflicts}
              </Badge>
              <Badge
                variant={
                  tt.fitnessReport.instructorConflicts > 0
                    ? "destructive"
                    : "outline"
                }
              >
                Instructor: {tt.fitnessReport.instructorConflicts}
              </Badge>
              <Badge
                variant={
                  tt.fitnessReport.studentConflicts > 0 ? "destructive" : "outline"
                }
              >
                Student: {tt.fitnessReport.studentConflicts}
              </Badge>
              <Badge>Penalty: {tt.fitnessReport.totalPenalty}</Badge>
            </>
          )}
          <a href={exportTimetableUrl(tt.id)} download>
            <Button variant="outline" size="sm" className="gap-2">
              <Download className="h-4 w-4" /> Export
            </Button>
          </a>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            Lectures ({tt.lectures?.length ?? 0})
          </CardTitle>
        </CardHeader>
        <CardContent>
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
              {tt.lectures?.map((l) => (
                <TableRow key={l.id}>
                  <TableCell className="font-medium">
                    {l.course
                      ? `${l.course.courseSymbol} ${l.course.courseNumber}`
                      : "—"}
                  </TableCell>
                  <TableCell>{l.number ?? "—"}</TableCell>
                  <TableCell>{l.instructor?.instructorName ?? "—"}</TableCell>
                  <TableCell>
                    {l.timeSlot?.days?.map((d) => d.slice(0, 3)).join(", ") ?? "—"}
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
        </CardContent>
      </Card>
    </div>
  );
}