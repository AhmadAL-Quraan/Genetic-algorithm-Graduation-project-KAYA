import { useState } from "react";
import {
  TimeSlots, DAYS_OF_WEEK, DURATION_OPTIONS, useDeleteAllTimeSlots,
  type TimeSlot, type TimeSlotInput, type TeachingMethod, type DayOfWeek,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Trash2, Clock, CalendarDays, Shuffle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

const METHOD_META: { method: TeachingMethod; label: string; color: string; bg: string }[] = [
  { method: "IN_PERSON", label: "In Person",  color: "text-green-700",  bg: "bg-green-50 border-green-200" },
  { method: "ONLINE",    label: "Online",     color: "text-purple-700", bg: "bg-purple-50 border-purple-200" },
  { method: "BLENDED",   label: "Blended",    color: "text-blue-700",   bg: "bg-blue-50 border-blue-200" },
];

function makeEmpty(method: TeachingMethod): TimeSlotInput {
  return { startTime: "08:00", endTime: "15:00", days: [], teachingMethod: method, durationMinutes: 90 };
}

function MethodSection({ method, label, color, bg, slots }: {
  method: TeachingMethod; label: string; color: string; bg: string; slots: TimeSlot[];
}) {
  const { toast } = useToast();
  const create = TimeSlots.useCreate();
  const remove = TimeSlots.useDelete();
  const [form, setForm] = useState<TimeSlotInput>(makeEmpty(method));

  const toggleDay = (d: DayOfWeek) =>
    setForm(f => ({ ...f, days: f.days.includes(d) ? f.days.filter(x => x !== d) : [...f.days, d] }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (form.days.length === 0) {
      toast({ title: "Pick at least one day", variant: "destructive" });
      return;
    }
    try {
      await create.mutateAsync(form);
      setForm(makeEmpty(method));
      toast({ title: `${label} window added` });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  return (
    <Card className={`border ${bg}`}>
      <CardHeader className="pb-3">
        <CardTitle className={`text-base font-semibold ${color}`}>{label}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Form */}
        <form onSubmit={submit} className="space-y-3">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <div>
              <Label className="text-xs">Start Time</Label>
              <Input type="time" value={form.startTime}
                onChange={e => setForm({ ...form, startTime: e.target.value })} required />
            </div>
            <div>
              <Label className="text-xs">End Time</Label>
              <Input type="time" value={form.endTime}
                onChange={e => setForm({ ...form, endTime: e.target.value })} required />
            </div>
            <div>
              <Label className="text-xs">Duration</Label>
              <Select value={form.durationMinutes.toString()}
                onValueChange={v => setForm({ ...form, durationMinutes: Number(v) })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {DURATION_OPTIONS.map(o => (
                    <SelectItem key={o.value} value={o.value.toString()}>{o.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div>
            <Label className="text-xs mb-1.5 block">Days</Label>
            <div className="flex flex-wrap gap-3">
              {DAYS_OF_WEEK.map(d => (
                <label key={d} className="flex items-center gap-1.5 text-xs cursor-pointer select-none">
                  <Checkbox checked={form.days.includes(d)} onCheckedChange={() => toggleDay(d)} />
                  {d.slice(0, 3)}
                </label>
              ))}
            </div>
          </div>

          <Button type="submit" size="sm" disabled={create.isPending}>
            {create.isPending ? "Adding…" : "Add Window"}
          </Button>
        </form>

        {/* Table */}
        {slots.length > 0 && (
          <div className="rounded-md border overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-white/60">
                  <TableHead className="text-xs">Start</TableHead>
                  <TableHead className="text-xs">End</TableHead>
                  <TableHead className="text-xs">Duration</TableHead>
                  <TableHead className="text-xs">Days</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {slots.map(t => (
                  <TableRow key={t.id} className="bg-white/40">
                    <TableCell className="font-semibold text-sm">{t.startTime?.slice(0, 5)}</TableCell>
                    <TableCell className="font-semibold text-sm">{t.endTime?.slice(0, 5)}</TableCell>
                    <TableCell>
                      {t.durationMinutes
                        ? <Badge variant="secondary">{t.durationMinutes} min</Badge>
                        : <span className="text-muted-foreground text-xs">—</span>}
                    </TableCell>
                    <TableCell className="text-xs text-muted-foreground">
                      {t.days.map(d => d.slice(0, 3)).join(", ")}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" className="h-7 w-7"
                        onClick={() => remove.mutate(t.id)} disabled={remove.isPending}>
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {slots.length === 0 && (
          <p className="text-xs text-muted-foreground">No windows added yet.</p>
        )}
      </CardContent>
    </Card>
  );
}

export default function TimeSlotsPage() {
  const { toast } = useToast();
  const list      = TimeSlots.useList();
  const deleteAll = useDeleteAllTimeSlots();

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Time Slots</h1>
        </div>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button variant="destructive" size="sm" disabled={deleteAll.isPending || !list.data?.length}>
              <Trash2 className="h-4 w-4 mr-2" /> Delete All
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete all time slots?</AlertDialogTitle>
              <AlertDialogDescription>
                This removes all {list.data?.filter(t => t.durationMinutes != null).length ?? 0} windows permanently.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                onClick={() => deleteAll.mutate(undefined, {
                  onSuccess: () => toast({ title: "All time slots deleted" }),
                  onError:   (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                })}>
                Delete all
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>

      {/* ── Summary banner ── */}
      <div className="rounded-xl border border-[#1A4D35]/20 bg-[#1A4D35]/5 p-4 space-y-3">
        <p className="text-sm font-medium text-[#1A4D35]">How this page works</p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="flex items-start gap-3">
            <div className="mt-0.5 rounded-lg bg-[#1A4D35]/10 p-2">
              <Clock className="h-4 w-4 text-[#1A4D35]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-foreground">Define a time window</p>
              <p className="text-xs text-muted-foreground">
                Set a start and end time (e.g. 8:00 AM – 3:00 PM) and a lecture duration
                (e.g. 90 min) for each teaching method.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="mt-0.5 rounded-lg bg-[#1A4D35]/10 p-2">
              <CalendarDays className="h-4 w-4 text-[#1A4D35]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-foreground">Pick the days</p>
              <p className="text-xs text-muted-foreground">
                Choose which days of the week this window applies to.
                No two windows for the same method can overlap on the same days.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="mt-0.5 rounded-lg bg-[#1A4D35]/10 p-2">
              <Shuffle className="h-4 w-4 text-[#1A4D35]" />
            </div>
            <div>
              <p className="text-xs font-semibold text-foreground">Algorithm does the rest</p>
              <p className="text-xs text-muted-foreground">
                The scheduler divides the window into slots of the chosen duration and
                automatically assigns one to every course with the matching teaching method.
              </p>
            </div>
          </div>
        </div>
      </div>

      {METHOD_META.map(({ method, label, color, bg }) => (
        <MethodSection
          key={method}
          method={method}
          label={label}
          color={color}
          bg={bg}
          slots={list.data?.filter(t => t.teachingMethod === method && t.durationMinutes != null) ?? []}
        />
      ))}
    </div>
  );
}
