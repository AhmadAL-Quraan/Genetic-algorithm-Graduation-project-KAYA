import { Link, Outlet, useLocation } from "@tanstack/react-router";
import {
  Sparkles,
  DoorOpen,
  Users,
  Clock,
  BookOpen,
  Calendar,
  AlertTriangle,
  Home,
  LayoutDashboard,
  GraduationCap,
} from "lucide-react";
import { cn } from "@/lib/utils";

const NAV = [
  { href: "/", label: "Welcome", icon: Home },
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/rooms", label: "Rooms", icon: DoorOpen },
  { href: "/instructors", label: "Instructors", icon: Users },
  { href: "/time-slots", label: "Time Slots", icon: Clock },
  { href: "/courses", label: "Courses", icon: BookOpen },
  { href: "/lectures", label: "Lectures", icon: GraduationCap },
  { href: "/schedule", label: "Schedule", icon: Calendar },
  { href: "/conflicts", label: "Conflicts", icon: AlertTriangle },
];

export function AppLayout() {
  const loc = useLocation();
  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-30 border-b bg-background/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center gap-3 px-4 py-3">
          <Link to="/" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#1A4D35] text-white">
              <Sparkles className="h-4 w-4" />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-semibold">KAYA</p>
              <p className="text-[10px] uppercase tracking-widest text-muted-foreground">
                Yarmouk · Timetable
              </p>
            </div>
          </Link>
          <nav className="ml-6 hidden flex-wrap gap-1 md:flex">
            {NAV.map(({ href, label, icon: Icon }) => {
              const active =
                href === "/"
                  ? loc.pathname === "/"
                  : loc.pathname === href || loc.pathname.startsWith(`${href}/`);
              return (
                <Link
                  key={href}
                  to={href}
                  className={cn(
                    "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm transition-colors",
                    active
                      ? "bg-[#1A4D35] text-white"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground",
                  )}
                >
                  <Icon className="h-3.5 w-3.5" />
                  {label}
                </Link>
              );
            })}
          </nav>
        </div>
        <nav className="flex gap-1 overflow-x-auto border-t px-3 py-2 md:hidden">
          {NAV.map(({ href, label, icon: Icon }) => {
            const active =
              href === "/"
                ? loc.pathname === "/"
                : loc.pathname === href || loc.pathname.startsWith(`${href}/`);
            return (
              <Link
                key={href}
                to={href}
                className={cn(
                  "flex shrink-0 items-center gap-1 rounded-md px-2.5 py-1.5 text-xs",
                  active
                    ? "bg-[#1A4D35] text-white"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground",
                )}
              >
                <Icon className="h-3 w-3" />
                {label}
              </Link>
            );
          })}
        </nav>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}