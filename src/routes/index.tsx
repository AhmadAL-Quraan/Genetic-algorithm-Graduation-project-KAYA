import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Sparkles,
  DoorOpen,
  Users,
  Clock,
  BookOpen,
  Calendar,
  AlertTriangle,
  ArrowRight,
} from "lucide-react";

export const Route = createFileRoute("/")({ component: Welcome });

const FEATURES = [
  {
    icon: Sparkles,
    title: "Genetic Algorithm",
    description:
      "Automatically evolves thousands of schedule combinations to minimise instructor, room, and student conflicts.",
    color: "text-[#B8860B]",
    bg: "bg-amber-50 border-amber-100",
  },
  {
    icon: BookOpen,
    title: "Course Management",
    description:
      "Define courses with their teaching method and required room type — the algorithm assigns time slots and rooms.",
    color: "text-[#1A4D35]",
    bg: "bg-green-50 border-green-100",
  },
  {
    icon: Calendar,
    title: "Generated Timetables",
    description:
      "Browse generated timetables as a structured list with full lecture, room and time details.",
    color: "text-blue-600",
    bg: "bg-blue-50 border-blue-100",
  },
  {
    icon: AlertTriangle,
    title: "Conflict Detection",
    description:
      "Instantly surfaces scheduling conflicts — double-booked rooms, instructors, or overlapping student groups.",
    color: "text-rose-500",
    bg: "bg-rose-50 border-rose-100",
  },
];

const QUICK_LINKS = [
  { href: "/rooms", label: "Rooms", icon: DoorOpen },
  { href: "/instructors", label: "Instructors", icon: Users },
  { href: "/time-slots", label: "Time Slots", icon: Clock },
  { href: "/courses", label: "Courses", icon: BookOpen },
  { href: "/lectures", label: "Lectures", icon: GraduationCapPlaceholder },
  { href: "/schedule", label: "Schedule", icon: Calendar },
];

// (avoid an extra import — alias a known icon for the lectures link)
function GraduationCapPlaceholder(props: React.SVGProps<SVGSVGElement>) {
  return <BookOpen {...props} />;
}

const TEAM = ["Ahmad Al-Quraan", "Younis Majdalawi", "Ahmad Obaidat", "Kanan Lafi"];

const STEPS: { num: string; text: React.ReactNode }[] = [
  {
    num: "0",
    text: (
      <>
        Click <strong>Import from Excel</strong> on the Dashboard to bulk-import an existing schedule.
      </>
    ),
  },
  {
    num: "1",
    text: (
      <>
        Add{" "}
        <Link to="/rooms" className="underline">Rooms</Link>,{" "}
        <Link to="/instructors" className="underline">Instructors</Link>, and{" "}
        <Link to="/time-slots" className="underline">Time Slots</Link>.
      </>
    ),
  },
  {
    num: "2",
    text: (
      <>
        Create <Link to="/courses" className="underline">Courses</Link> and add{" "}
        <Link to="/lectures" className="underline">Lecture sections</Link>.
      </>
    ),
  },
  {
    num: "3",
    text: (
      <>
        Run the genetic algorithm from the{" "}
        <Link to="/schedule" className="underline">Schedule</Link> page.
      </>
    ),
  },
  {
    num: "4",
    text: (
      <>
        Review detected overlaps on the{" "}
        <Link to="/conflicts" className="underline">Conflicts</Link> page.
      </>
    ),
  },
];

function Welcome() {
  return (
    <div className="space-y-12">
      <section className="relative overflow-hidden rounded-2xl border border-[#1A4D35]/20 bg-gradient-to-br from-[#1A4D35] via-[#1f5c3f] to-[#153d2a] px-8 py-14 text-white shadow-lg">
        <div className="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-white/5" />
        <div className="pointer-events-none absolute -bottom-12 -left-12 h-48 w-48 rounded-full bg-white/5" />
        <div className="relative flex flex-col items-start gap-6 sm:flex-row sm:items-center">
          <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-xl bg-white/10 p-2 shadow">
            <Sparkles className="h-12 w-12 text-[#B8860B]" />
          </div>
          <div className="flex-1">
            <p className="mb-1 text-xs font-semibold uppercase tracking-[0.2em] text-white/60">
              Yarmouk University
            </p>
            <h1 className="text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
              KAYA
              <span className="ml-3 text-[#B8860B]">·</span>
              <span className="ml-3 text-2xl font-semibold text-white/90 sm:text-3xl">
                University Course Timetable System
              </span>
            </h1>
            <p className="mt-3 max-w-2xl text-sm leading-relaxed text-white/75 sm:text-base">
              KAYA is an intelligent scheduling platform that uses a genetic algorithm to
              automatically generate conflict-free university timetables. Manage rooms,
              instructors, time slots, and courses — then let the algorithm do the rest.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link to="/dashboard">
                <Button className="gap-2 border-0 bg-[#B8860B] text-white hover:bg-[#9a7009]">
                  <ArrowRight className="h-4 w-4" /> Go to Dashboard
                </Button>
              </Link>
              <Link to="/schedule">
                <Button
                  variant="outline"
                  className="gap-2 border-white/30 bg-white/10 text-white hover:bg-white/20"
                >
                  <Sparkles className="h-4 w-4" /> Generate Schedule
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section>
        <h2 className="mb-4 text-lg font-semibold">What KAYA does</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {FEATURES.map(({ icon: Icon, title, description, color, bg }) => (
            <Card key={title} className={`border ${bg} shadow-none`}>
              <CardContent className="flex gap-4 pt-5 pb-5">
                <div className={`mt-0.5 shrink-0 ${color}`}>
                  <Icon className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-sm font-semibold">{title}</p>
                  <p className="mt-1 text-sm leading-relaxed text-muted-foreground">
                    {description}
                  </p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      <section>
        <Card className="border shadow-sm">
          <CardHeader>
            <CardTitle className="text-base">Getting started</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {STEPS.map(({ num, text }) => (
              <div key={num} className="flex gap-3 text-sm text-muted-foreground">
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[#1A4D35]/10 text-xs font-bold text-[#1A4D35]">
                  {num}
                </span>
                <p className="leading-6">{text}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      </section>

      <section>
        <h2 className="mb-4 text-lg font-semibold">Quick access</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {QUICK_LINKS.map(({ href, label, icon: Icon }) => (
            <Link key={href} to={href}>
              <Card className="group cursor-pointer border transition-shadow hover:shadow-md">
                <CardContent className="flex items-center gap-3 py-4 px-4">
                  <Icon className="h-4 w-4 text-[#1A4D35] transition-colors group-hover:text-[#B8860B]" />
                  <span className="text-sm font-medium">{label}</span>
                  <ArrowRight className="ml-auto h-3 w-3 text-muted-foreground transition-colors group-hover:text-[#B8860B]" />
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </section>

      <footer className="border-t pt-8 pb-2">
        <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          Made by
        </p>
        <div className="flex flex-wrap gap-3">
          {TEAM.map((name) => (
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
