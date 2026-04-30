import { useMemo, useRef, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin, { type EventResizeDoneArg } from "@fullcalendar/interaction";
import type { EventInput, EventDropArg, DatesSetArg } from "@fullcalendar/core";
import {
  type Lecture,
  type TimeTable,
  type DayOfWeek,
  findOrCreateTimeSlot,
  updateLectureAssignment,
} from "@/lib/api";
import { useToast } from "@/hooks/use-toast";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Maximize2, Minimize2 } from "lucide-react";

const DAY_TO_INDEX: Record<DayOfWeek, number> = {
  SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3,
  THURSDAY: 4, FRIDAY: 5, SATURDAY: 6,
};
const INDEX_TO_DAY: DayOfWeek[] = [
  "SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY",
];
const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: "Mon", TUESDAY: "Tue", WEDNESDAY: "Wed", THURSDAY: "Thu",
  FRIDAY: "Fri", SATURDAY: "Sat", SUNDAY: "Sun",
};
const ALL_DAYS: DayOfWeek[] = ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"];

const REF_MONDAY = new Date("2024-01-01T00:00:00");

function dayDateFor(day: DayOfWeek): Date {
  const d = new Date(REF_MONDAY);
  const idx = DAY_TO_INDEX[day];
  const offset = (idx + 6) % 7;
  d.setDate(REF_MONDAY.getDate() + offset);
  return d;
}

function withTime(d: Date, hhmmss: string): Date {
  const [h, m, s] = hhmmss.split(":").map(Number);
  const out = new Date(d);
  out.setHours(h ?? 0, m ?? 0, s ?? 0, 0);
  return out;
}

function pad(n: number) { return n.toString().padStart(2, "0"); }
function toHHMMSS(d: Date) { return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`; }

const COLORS = [
  "#2563eb", "#16a34a", "#dc2626", "#ea580c", "#9333ea",
  "#0891b2", "#ca8a04", "#db2777", "#4f46e5", "#0d9488",
];
function colorFor(id: number) { return COLORS[id % COLORS.length]; }

interface Props {
  timetable: TimeTable;
  onMutated: () => void;
  isFullscreen?: boolean;
}

function CalendarInner({ timetable, onMutated, isFullscreen = false }: Props) {
  const { toast } = useToast();
  const qc = useQueryClient();
  const calRef = useRef<FullCalendar | null>(null);
  const [viewType, setViewType] = useState("timeGridWeek");
  const [activeDayDate, setActiveDayDate] = useState<Date>(dayDateFor("MONDAY"));

  const lecturesById = useMemo(() => {
    const m = new Map<number, Lecture>();
    for (const l of timetable.lectures ?? []) m.set(l.id, l);
    return m;
  }, [timetable]);

  const events = useMemo<EventInput[]>(() => {
    const out: EventInput[] = [];
    for (const l of timetable.lectures ?? []) {
      if (!l.timeSlot || !l.timeSlot.days?.length) continue;
      for (const day of l.timeSlot.days) {
        const base = dayDateFor(day);
        const start = withTime(base, l.timeSlot.startTime);
        const end = withTime(base, l.timeSlot.endTime);
        const title = (l.course ? `${l.course.courseSymbol} ${l.course.courseNumber}` : "Lecture") +
          (l.room ? ` · ${l.room.building} ${l.room.roomNumber}` : "");
        out.push({
          id: `${l.id}:${day}`,
          title,
          start,
          end,
          backgroundColor: colorFor(l.id),
          borderColor: colorFor(l.id),
          extendedProps: { lectureId: l.id, day, instructor: l.instructor },
        });
      }
    }
    return out;
  }, [timetable]);

  const handleDatesSet = (arg: DatesSetArg) => {
    setViewType(arg.view.type);
    setActiveDayDate(arg.view.activeStart);
  };

  async function applyMove(arg: EventDropArg | EventResizeDoneArg) {
    const lectureId = arg.event.extendedProps.lectureId as number;
    const draggedDay = arg.event.extendedProps.day as DayOfWeek;
    const lecture = lecturesById.get(lectureId);
    if (!lecture || !lecture.timeSlot || !arg.event.start || !arg.event.end) {
      arg.revert(); return;
    }
    const newStart = arg.event.start;
    const newEnd = arg.event.end;
    const newDayIdx = newStart.getDay();
    const oldDayIdx = DAY_TO_INDEX[draggedDay];
    const dayDelta = ((newDayIdx - oldDayIdx) % 7 + 7) % 7;
    const oldDays = lecture.timeSlot.days;
    const newDays: DayOfWeek[] = oldDays.map(d => INDEX_TO_DAY[(DAY_TO_INDEX[d] + dayDelta) % 7]);
    const uniqueDays = Array.from(new Set(newDays));
    const newDay = INDEX_TO_DAY[newDayIdx];
    try {
      const ts = await findOrCreateTimeSlot({
        startTime: toHHMMSS(newStart),
        endTime: toHHMMSS(newEnd),
        days: uniqueDays,
        teachingMethod: lecture.timeSlot.teachingMethod,
      });
      await updateLectureAssignment(lecture, { timeSlotId: ts.id });
      await qc.invalidateQueries({ queryKey: ["time-table"] });
      await qc.invalidateQueries({ queryKey: ["lectures"] });
      await qc.invalidateQueries({ queryKey: ["time-slots"] });
      onMutated();
      toast({ title: "Lecture moved", description: `${arg.event.title} → ${newDay} ${toHHMMSS(newStart).slice(0,5)}` });
    } catch (err: any) {
      arg.revert();
      toast({ title: "Could not move lecture", description: err.message, variant: "destructive" });
    }
  }

  const isDayView = viewType === "timeGridDay";

  const goToDay = (day: DayOfWeek) => {
    const api = calRef.current?.getApi();
    if (!api) return;
    api.changeView("timeGridDay", dayDateFor(day));
  };

  const activeDayName: DayOfWeek | null = isDayView
    ? (INDEX_TO_DAY[activeDayDate.getDay()] ?? null)
    : null;

  return (
    <div className="flex flex-col gap-2">
      {isDayView && (
        <div className="flex flex-wrap gap-1">
          {ALL_DAYS.map(day => (
            <Button
              key={day}
              size="sm"
              variant={activeDayName === day ? "default" : "outline"}
              className="text-xs px-3"
              onClick={() => goToDay(day)}
            >
              {DAY_LABELS[day]}
            </Button>
          ))}
        </div>
      )}

      <div className={`bg-background rounded-md border p-2 ${isFullscreen ? "flex-1" : ""}`}>
        <FullCalendar
          ref={calRef}
          plugins={[timeGridPlugin, dayGridPlugin, interactionPlugin]}
          initialView="timeGridWeek"
          headerToolbar={{ left: "", center: "", right: "timeGridWeek,timeGridDay,dayGridMonth" }}
          initialDate={REF_MONDAY}
          firstDay={1}
          allDaySlot={false}
          slotMinTime="07:00:00"
          slotMaxTime="22:00:00"
          slotDuration="00:30:00"
          height={isFullscreen ? "calc(100vh - 160px)" : "auto"}
          expandRows
          nowIndicator={false}
          dayHeaderFormat={{ weekday: "long" }}
          editable
          eventStartEditable
          eventDurationEditable
          eventResizableFromStart
          events={events}
          eventDrop={applyMove}
          eventResize={applyMove}
          datesSet={handleDatesSet}
        />
        <p className="text-xs text-muted-foreground pt-2">
          Drag a lecture to a new day or time, or resize its edges to change duration. Changes are saved automatically.
        </p>
      </div>
    </div>
  );
}

export function TimetableCalendar({ timetable, onMutated }: Omit<Props, "isFullscreen">) {
  const [expanded, setExpanded] = useState(false);

  return (
    <>
      <div className="relative">
        <Button
          variant="outline"
          size="sm"
          className="absolute top-0 right-0 z-10 gap-1.5 text-xs"
          onClick={() => setExpanded(true)}
        >
          <Maximize2 className="h-3.5 w-3.5" />
          Expand
        </Button>
        <CalendarInner timetable={timetable} onMutated={onMutated} />
      </div>

      <Dialog open={expanded} onOpenChange={setExpanded}>
        <DialogContent className="max-w-[98vw] w-[98vw] h-[96vh] flex flex-col p-4 gap-2">
          <div className="flex items-center justify-between shrink-0">
            <h2 className="text-base font-semibold">Timetable — Full View</h2>
            <Button variant="ghost" size="sm" className="gap-1.5" onClick={() => setExpanded(false)}>
              <Minimize2 className="h-4 w-4" /> Close
            </Button>
          </div>
          <div className="flex-1 overflow-auto">
            <CalendarInner timetable={timetable} onMutated={() => { onMutated(); }} isFullscreen />
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
