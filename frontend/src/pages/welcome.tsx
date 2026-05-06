import { Link } from "wouter";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Sparkles, DoorOpen, Users, Building2, Clock,
  BookOpen, Calendar, AlertTriangle, ArrowRight,
} from "lucide-react";

const STEPS = [
  {
    num: "0",
    text: <>Click <strong>Import from Excel</strong> on the Dashboard to bulk-import an existing schedule.</>,
  },
  {
    num: "1",
    text: (
      <>
        Add{" "}
        <Link href="/rooms" className="underline hover:text-foreground transition-colors">Room</Link>,{" "}
        <Link href="/teachers" className="underline hover:text-foreground transition-colors">Instructor</Link>,{" "}
        <Link href="/departments" className="underline hover:text-foreground transition-colors">Department</Link>, and{" "}
        <Link href="/time-slots" className="underline hover:text-foreground transition-colors">Time Slots</Link>{" "}
        as your setup data.
      </>
    ),
  },
  {
    num: "2",
    text: (
      <>
        Create{" "}
        <Link href="/courses" className="underline hover:text-foreground transition-colors">Courses</Link>
        {" "}— link each to a doctor, room, and time slot via dropdown.
      </>
    ),
  },
  {
    num: "3",
    text: (
      <>
        Run the genetic algorithm on the{" "}
        <Link href="/schedule" className="underline hover:text-foreground transition-colors">Schedule</Link>
        {" "}page for an optimized timetable.
      </>
    ),
  },
  {
    num: "4",
    text: (
      <>
        Check the{" "}
        <Link href="/conflicts" className="underline hover:text-foreground transition-colors">Conflicts</Link>
        {" "}page to review any detected overlaps.
      </>
    ),
  },
];

const FEATURES = [
  {
    icon: Sparkles,
    title: "Genetic Algorithm",
    description: "Automatically evolves thousands of schedule combinations to minimise instructor, room, and student conflicts.",
    color: "text-[#B8860B]",
    bg: "bg-amber-50 border-amber-100",
  },
  {
    icon: BookOpen,
    title: "Course Management",
    description: "Define courses with their assigned instructors, rooms, and preferred time slots — all in one place.",
    color: "text-[#1A4D35]",
    bg: "bg-green-50 border-green-100",
  },
  {
    icon: Calendar,
    title: "Visual Timetable",
    description: "Browse generated timetables in a clean calendar grid or as a structured list with full conflict details.",
    color: "text-blue-600",
    bg: "bg-blue-50 border-blue-100",
  },
  {
    icon: AlertTriangle,
    title: "Conflict Detection",
    description: "Instantly surfaces scheduling conflicts — double-booked rooms, instructors, or overlapping student groups.",
    color: "text-rose-500",
    bg: "bg-rose-50 border-rose-100",
  },
];

const QUICK_LINKS = [
  { href: "/rooms",       label: "Rooms",       icon: DoorOpen   },
  { href: "/teachers",    label: "Instructors",  icon: Users      },
  { href: "/departments", label: "Departments",  icon: Building2  },
  { href: "/time-slots",  label: "Time Slots",   icon: Clock      },
  { href: "/courses",     label: "Courses",      icon: BookOpen   },
  { href: "/schedule",    label: "Schedule",     icon: Calendar   },
];

const TEAM = [
  "Ahmad Al-Quraan",
  "Younis Majdalawi",
  "Ahmad Obaidat",
  "Kanan Lafi",
];

export default function WelcomePage() {
  return (
    <div className="space-y-12">

      {/* ── Hero ── */}
      <section className="relative overflow-hidden rounded-2xl border border-[#1A4D35]/20 bg-gradient-to-br from-[#1A4D35] via-[#1f5c3f] to-[#153d2a] px-8 py-14 text-white shadow-lg">
        {/* decorative circles */}
        <div className="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-white/5" />
        <div className="pointer-events-none absolute -bottom-12 -left-12 h-48 w-48 rounded-full bg-white/5" />

        <div className="relative flex flex-col items-start gap-6 sm:flex-row sm:items-center">
          <img
            src="/yarmouk-logo.png"
            alt="Yarmouk University"
            className="h-24 w-24 shrink-0 rounded-xl object-contain bg-white/10 p-2 shadow"
          />
          <div className="flex-1">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-white/60 mb-1">
              Yarmouk University
            </p>
            <h1 className="text-3xl sm:text-4xl font-bold tracking-tight leading-tight">
              KAYA
              <span className="ml-3 text-[#B8860B]">·</span>
              <span className="ml-3 text-2xl sm:text-3xl font-semibold text-white/90">
                University Course Timetable System
              </span>
            </h1>
            <p className="mt-3 max-w-2xl text-white/75 text-sm sm:text-base leading-relaxed">
              KAYA is an intelligent scheduling platform that uses a genetic algorithm to
              automatically generate conflict-free university timetables. Manage rooms,
              instructors, departments, time slots, and courses — then let the algorithm do the rest.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link href="/dashboard">
                <Button className="bg-[#B8860B] hover:bg-[#9a7009] text-white border-0 gap-2">
                  <ArrowRight className="h-4 w-4" /> Go to Dashboard
                </Button>
              </Link>
              <Link href="/schedule">
                <Button variant="outline" className="border-white/30 text-white bg-white/10 hover:bg-white/20 gap-2">
                  <Sparkles className="h-4 w-4" /> Generate Schedule
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ── Features ── */}
      <section>
        <h2 className="text-lg font-semibold mb-4">What KAYA does</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {FEATURES.map(({ icon: Icon, title, description, color, bg }) => (
            <Card key={title} className={`border ${bg} shadow-none`}>
              <CardContent className="flex gap-4 pt-5 pb-5">
                <div className={`mt-0.5 shrink-0 ${color}`}>
                  <Icon className="h-5 w-5" />
                </div>
                <div>
                  <p className="font-semibold text-sm">{title}</p>
                  <p className="text-sm text-muted-foreground mt-1 leading-relaxed">{description}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* ── Getting Started ── */}
      <section>
        <Card className="border shadow-sm">
          <CardHeader>
            <CardTitle className="text-base">Getting started</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {STEPS.map(({ num, text }) => (
              <div key={num} className="flex gap-3 text-sm text-muted-foreground">
                <span className="shrink-0 flex h-6 w-6 items-center justify-center rounded-full bg-[#1A4D35]/10 text-[#1A4D35] text-xs font-bold">
                  {num}
                </span>
                <p className="leading-6">{text}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      </section>

      {/* ── Quick Links ── */}
      <section>
        <h2 className="text-lg font-semibold mb-4">Quick access</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {QUICK_LINKS.map(({ href, label, icon: Icon }) => (
            <Link key={href} href={href}>
              <Card className="cursor-pointer hover:shadow-md transition-shadow border group">
                <CardContent className="flex items-center gap-3 py-4 px-4">
                  <Icon className="h-4 w-4 text-[#1A4D35] group-hover:text-[#B8860B] transition-colors" />
                  <span className="text-sm font-medium">{label}</span>
                  <ArrowRight className="h-3 w-3 ml-auto text-muted-foreground group-hover:text-[#B8860B] transition-colors" />
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </section>

      {/* ── Team Credits ── */}
      <footer className="border-t pt-8 pb-2">
        <p className="text-xs uppercase tracking-widest text-muted-foreground font-semibold mb-3">Made by</p>
        <div className="flex flex-wrap gap-3">
          {TEAM.map(name => (
            <span
              key={name}
              className="rounded-full border border-[#1A4D35]/20 bg-[#1A4D35]/5 px-4 py-1.5 text-sm font-medium text-[#1A4D35]"
            >
              {name}
            </span>
          ))}
        </div>
        <p className="mt-4 text-xs text-muted-foreground">
          Yarmouk University · KAYA University Course Timetable System
        </p>
      </footer>

    </div>
  );
}
