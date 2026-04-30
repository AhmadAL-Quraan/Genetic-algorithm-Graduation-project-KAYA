import { Link, useLocation } from "wouter";
import {
  LayoutDashboard, BookOpen, DoorOpen, Calendar,
  Users, Building2, Clock, AlertTriangle, Menu,
} from "lucide-react";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { useConflicts } from "@/lib/api";

const SETUP_NAV = [
  { href: "/rooms",       label: "Rooms",       icon: DoorOpen   },
  { href: "/teachers",    label: "Doctors",     icon: Users      },
  { href: "/departments", label: "Departments", icon: Building2  },
  { href: "/time-slots",  label: "Timeslots",   icon: Clock      },
];

const OVERVIEW_NAV = [
  { href: "/",            label: "Dashboard",   icon: LayoutDashboard },
  { href: "/courses",     label: "Courses",     icon: BookOpen        },
  { href: "/schedule",    label: "Schedule",    icon: Calendar        },
  { href: "/conflicts",   label: "Conflicts",   icon: AlertTriangle   },
];

function NavSection({ title, items, onNavigate }: {
  title: string;
  items: { href: string; label: string; icon: React.ComponentType<{ className?: string }> }[];
  onNavigate?: () => void;
}) {
  const [location] = useLocation();
  return (
    <div className="mb-4">
      <p className="px-3 mb-1 text-[10px] font-semibold uppercase tracking-widest text-sidebar-foreground/40">
        {title}
      </p>
      {items.map(({ href, label, icon: Icon }) => {
        const active = location === href || (href !== "/" && location.startsWith(href));
        return (
          <Link key={href} href={href} onClick={onNavigate}
            className={cn(
              "flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors",
              active
                ? "bg-sidebar-primary text-sidebar-primary-foreground font-medium"
                : "text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
            )}>
            <Icon className="h-4 w-4" />
            {label}
          </Link>
        );
      })}
    </div>
  );
}

function ConflictBadge() {
  const { data } = useConflicts();
  if (!data?.length) return null;
  return (
    <span className="ml-auto flex h-5 min-w-5 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-bold text-destructive-foreground">
      {data.length}
    </span>
  );
}

function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const [location] = useLocation();
  return (
    <nav className="flex flex-col">
      <NavSection title="Setup & Data" items={SETUP_NAV} onNavigate={onNavigate} />
      <div className="mb-4">
        <p className="px-3 mb-1 text-[10px] font-semibold uppercase tracking-widest text-sidebar-foreground/40">
          Overview
        </p>
        {OVERVIEW_NAV.map(({ href, label, icon: Icon }) => {
          const active = location === href || (href !== "/" && location.startsWith(href));
          return (
            <Link key={href} href={href} onClick={onNavigate}
              className={cn(
                "flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors",
                active
                  ? "bg-sidebar-primary text-sidebar-primary-foreground font-medium"
                  : "text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
              )}>
              <Icon className="h-4 w-4" />
              {label}
              {href === "/conflicts" && <ConflictBadge />}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

function SidebarLogo() {
  return (
    <div className="flex h-[80px] items-center gap-3 border-b border-sidebar-border px-3 bg-black/10">
      <img
        src="/yarmouk-logo.png"
        alt="Yarmouk University"
        className="h-14 w-14 object-contain rounded-sm flex-shrink-0"
      />
      <div className="min-w-0">
        <div className="text-[11px] font-bold tracking-wide text-sidebar-foreground leading-tight truncate">Yarmouk University</div>
        <div className="text-base font-bold tracking-wide" style={{ color: "hsl(43 88% 58%)" }}>KAYA</div>
        <div className="text-[8px] uppercase tracking-[0.15em] text-sidebar-foreground/45 leading-tight">UCTT System</div>
      </div>
    </div>
  );
}

export function AppLayout({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  return (
    <div className="flex h-screen w-full bg-background text-foreground">
      <aside className="hidden md:flex w-56 flex-col border-r bg-sidebar">
        <SidebarLogo />
        <div className="flex-1 overflow-y-auto p-3 pt-4">
          <NavLinks />
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="md:hidden flex h-14 items-center justify-between border-b bg-sidebar px-4">
          <div className="flex items-center gap-2">
            <img
              src="/yarmouk-logo.png"
              alt="Yarmouk University"
              className="h-8 w-8 object-contain rounded-sm"
            />
            <div>
              <div className="text-[10px] text-sidebar-foreground/60 leading-none">Yarmouk University</div>
              <div className="font-bold leading-tight" style={{ color: "hsl(43 88% 58%)" }}>KAYA</div>
            </div>
          </div>
          <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="text-sidebar-foreground hover:bg-sidebar-accent">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-56 p-0 bg-sidebar border-sidebar-border">
              <SidebarLogo />
              <div className="p-3 pt-4">
                <NavLinks onNavigate={() => setOpen(false)} />
              </div>
            </SheetContent>
          </Sheet>
        </header>
        <main className="flex-1 overflow-y-auto">
          <div className="mx-auto max-w-6xl p-6">{children}</div>
        </main>
      </div>
    </div>
  );
}
