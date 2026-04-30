import { useState } from "react";
import { TimeSlots, TEACHING_METHODS, DAYS_OF_WEEK, useDeleteAllTimeSlots, type TimeSlotInput, type TeachingMethod, type DayOfWeek } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription, AlertDialogFooter,
  AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

const empty: TimeSlotInput = { startTime: "09:00", endTime: "10:30", days: [], teachingMethod: "IN_PERSON" };

export default function TimeSlotsPage() {
  const { toast } = useToast();
  const list      = TimeSlots.useList();
  const create    = TimeSlots.useCreate();
  const remove    = TimeSlots.useDelete();
  const deleteAll = useDeleteAllTimeSlots();
  const [form, setForm] = useState<TimeSlotInput>(empty);

  const toggleDay = (d: DayOfWeek) =>
    setForm(f => ({ ...f, days: f.days.includes(d) ? f.days.filter(x => x !== d) : [...f.days, d] }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (form.days.length === 0) { toast({ title: "Pick at least one day", variant: "destructive" }); return; }
    try { await create.mutateAsync(form); setForm(empty); toast({ title: "Time slot added" }); }
    catch (err: any) { toast({ title: "Failed", description: err.message, variant: "destructive" }); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-semibold">Time Slots</h1>
          <p className="text-muted-foreground text-sm">Each slot covers one or more days of the week and a teaching method.</p>
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
                This will permanently delete all {list.data?.length ?? 0} time slots and remove them from any lectures. This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                onClick={() => deleteAll.mutate(undefined, {
                  onSuccess: () => toast({ title: "All time slots deleted" }),
                  onError: (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                })}>
                Delete all
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Plus className="h-4 w-4" /> Add time slot</CardTitle></CardHeader>
        <CardContent>
          <form onSubmit={submit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <Label>Start</Label>
                <Input type="time" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} required />
              </div>
              <div>
                <Label>End</Label>
                <Input type="time" value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })} required />
              </div>
              <div>
                <Label>Teaching method</Label>
                <Select value={form.teachingMethod} onValueChange={(v: TeachingMethod) => setForm({ ...form, teachingMethod: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>{TEACHING_METHODS.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}</SelectContent>
                </Select>
              </div>
            </div>
            <div>
              <Label className="mb-2 block">Days</Label>
              <div className="flex flex-wrap gap-3">
                {DAYS_OF_WEEK.map(d => (
                  <label key={d} className="flex items-center gap-2 text-sm cursor-pointer">
                    <Checkbox checked={form.days.includes(d)} onCheckedChange={() => toggleDay(d)} />
                    {d.slice(0, 3)}
                  </label>
                ))}
              </div>
            </div>
            <Button type="submit" disabled={create.isPending}>Add slot</Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">All time slots ({list.data?.length ?? 0})</CardTitle></CardHeader>
        <CardContent>
          {list.isLoading ? <div className="text-sm text-muted-foreground">Loading…</div> :
           !list.data?.length ? <div className="text-sm text-muted-foreground">No time slots yet.</div> :
            <Table>
              <TableHeader><TableRow><TableHead>Time</TableHead><TableHead>Days</TableHead><TableHead>Method</TableHead><TableHead /></TableRow></TableHeader>
              <TableBody>
                {list.data.map(t => (
                  <TableRow key={t.id}>
                    <TableCell className="font-medium">{t.startTime?.slice(0,5)}–{t.endTime?.slice(0,5)}</TableCell>
                    <TableCell>{t.days.map(d => d.slice(0, 3)).join(", ")}</TableCell>
                    <TableCell><Badge variant="secondary">{t.teachingMethod}</Badge></TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => remove.mutate(t.id)}><Trash2 className="h-4 w-4" /></Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          }
        </CardContent>
      </Card>
    </div>
  );
}
