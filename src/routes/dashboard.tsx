import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  BookOpen,
  DoorOpen,
  Calendar,
  Users,
  Clock,
  AlertTriangle,
  GraduationCap,
} from "lucide-react";
import {
  Courses,
  Rooms,
  TimeTables,
  Instructors,
  TimeSlots,
  Lectures,
  useConflicts,
} from "@/lib/api";
import { ExcelImportButton } from "@/components/excel-import-button";

export const Route = createFileRoute("/dashboard")({ component: Dashboard });

function Dashboard() {
  const courses = Courses.useList();
  const rooms = Rooms.useList();
  const timetables = TimeTables.useList();
  const instructors = Instructors.useList();
  const timeSlots = TimeSlots.useList();
  const lectures = Lectures.useList();
  const conflicts = useConflicts();

  const setupStats = [
    { label: "Rooms", icon: DoorOpen, count: rooms.data?.length, href: "/rooms" },
    { label: "Instructors", icon: Users, count: instructors.data?.length, href: "/instructors" },
    { label: "Time Slots", icon: Clock, count: timeSlots.data?.length, href: "/time-slots" },
    { label: "Lectures", icon: GraduationCap, count: lectures.data?.length, href: "/lectures" },
  ];

  const overviewStats = [
    { label: "Courses", icon: BookOpen, count: courses.data?.length, href: "/courses" },
    { label: "Timetables", icon: Calendar, count: timetables.data?.length, href: "/schedule" },
    {
      label: "Conflicts",
      icon: AlertTriangle,
      count: conflicts.data?.length,
      href: "/conflicts",
      danger: (conflicts.data?.length ?? 0) > 0,
    },
  ];

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">KAYA Scheduler</h1>
          <p className="mt-1 max-w-xl text-sm text-muted-foreground">
            Genetic-algorithm timetable generation. Set up your data first, then add courses and generate a schedule.
          </p>
        </div>
        <ExcelImportButton />
      </div>

      <div>
        <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          Setup &amp; Data
        </p>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {setupStats.map(({ label, icon: Icon, count, href }) => (
            <Link key={href} to={href}>
              <Card className="cursor-pointer transition-shadow hover:shadow-md">
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium">{label}</CardTitle>
                  <Icon className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-semibold">{count ?? "—"}</div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>

      <div>
        <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          Overview
        </p>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          {overviewStats.map(({ label, icon: Icon, count, href, danger }) => (
            <Link key={href} to={href}>
              <Card
                className={`cursor-pointer transition-shadow hover:shadow-md ${
                  danger ? "border-destructive/60" : ""
                }`}
              >
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium">{label}</CardTitle>
                  <Icon
                    className={`h-4 w-4 ${
                      danger ? "text-destructive" : "text-muted-foreground"
                    }`}
                  />
                </CardHeader>
                <CardContent>
                  <div className={`text-2xl font-semibold ${danger ? "text-destructive" : ""}`}>
                    {count ?? "—"}
                  </div>
                  {danger && (
                    <p className="mt-0.5 text-xs text-destructive">Needs attention</p>
                  )}
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}