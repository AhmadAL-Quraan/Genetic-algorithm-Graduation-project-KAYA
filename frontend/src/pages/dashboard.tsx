import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  BookOpen, DoorOpen, Calendar, Users, Building2, Clock, AlertTriangle,
} from "lucide-react";
import {
  Courses, Rooms, TimeTables, Teachers, Departments, TimeSlots,
  useConflicts, exportScheduleUrl,
} from "@/lib/api";
import { Link } from "wouter";
import { ExcelImportButton } from "@/components/excel-import-button";
import { Button } from "@/components/ui/button";
import { Download } from "lucide-react";

export default function Dashboard() {
  const courses     = Courses.useList();
  const rooms       = Rooms.useList();
  const timetables  = TimeTables.useList();
  const teachers    = Teachers.useList();
  const departments = Departments.useList();
  const timeSlots   = TimeSlots.useList();
  const conflicts   = useConflicts();

  const setupStats = [
    { label: "Rooms",       icon: DoorOpen,   count: rooms.data?.length,       href: "/rooms"       },
    { label: "Doctors",     icon: Users,       count: teachers.data?.length,    href: "/teachers"    },
    { label: "Departments", icon: Building2,   count: departments.data?.length, href: "/departments" },
    { label: "Time Slots",  icon: Clock,       count: timeSlots.data?.length,   href: "/time-slots"  },
  ];

  const overviewStats = [
    { label: "Courses",     icon: BookOpen,    count: courses.data?.length,     href: "/courses"     },
    { label: "Timetables",  icon: Calendar,    count: timetables.data?.length,  href: "/schedule"    },
    { label: "Conflicts",   icon: AlertTriangle, count: conflicts.data?.length, href: "/conflicts",
      danger: (conflicts.data?.length ?? 0) > 0 },
  ];

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">KAYA Scheduler</h1>
          <p className="text-muted-foreground mt-1 text-sm max-w-xl">
            Genetic-algorithm timetable generation. Set up your data first, then add courses and generate a schedule.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <ExcelImportButton />
          <a href={exportScheduleUrl()} download>
            <Button variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" /> Export
            </Button>
          </a>
        </div>
      </div>

      <div>
        <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">Setup &amp; Data</p>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {setupStats.map(({ label, icon: Icon, count, href }) => (
            <Link key={href} href={href}>
              <Card className="cursor-pointer hover:shadow-md transition-shadow">
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
        <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">Overview</p>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {overviewStats.map(({ label, icon: Icon, count, href, danger }) => (
            <Link key={href} href={href}>
              <Card className={`cursor-pointer hover:shadow-md transition-shadow ${danger ? "border-destructive/60" : ""}`}>
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium">{label}</CardTitle>
                  <Icon className={`h-4 w-4 ${danger ? "text-destructive" : "text-muted-foreground"}`} />
                </CardHeader>
                <CardContent>
                  <div className={`text-2xl font-semibold ${danger ? "text-destructive" : ""}`}>
                    {count ?? "—"}
                  </div>
                  {danger && (
                    <p className="text-xs text-destructive mt-0.5">Needs attention</p>
                  )}
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>

      <Card>
        <CardHeader><CardTitle>Getting started</CardTitle></CardHeader>
        <CardContent className="space-y-2 text-sm text-muted-foreground">
          <p>0. Click <strong>Import from Excel</strong> above to bulk-import an existing schedule.</p>
          <p>1. Add <Link href="/rooms" className="underline">Rooms</Link>,{" "}
            <Link href="/teachers" className="underline">Doctors</Link>,{" "}
            <Link href="/departments" className="underline">Departments</Link>, and{" "}
            <Link href="/time-slots" className="underline">Time Slots</Link> as your setup data.</p>
          <p>2. Create <Link href="/courses" className="underline">Courses</Link> — link each to a doctor, room, and time slot via dropdown.</p>
          <p>3. Run the genetic algorithm on the <Link href="/schedule" className="underline">Schedule</Link> page for an optimized timetable.</p>
          <p>4. Check the <Link href="/conflicts" className="underline">Conflicts</Link> page to review any detected overlaps.</p>
        </CardContent>
      </Card>
    </div>
  );
}
