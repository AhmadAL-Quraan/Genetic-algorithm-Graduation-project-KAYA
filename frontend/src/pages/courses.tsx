import { useState } from "react";
import {
  Courses, Rooms, TimeSlots, Teachers, Departments, useDeleteAllCourses,
  ROOM_TYPES, TEACHING_METHODS,
  type Course, type CourseInput, type RoomType, type TeachingMethod, type DayOfWeek,
  exportScheduleUrl,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Trash2, Plus, ChevronDown, ChevronUp, Download } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: "Mon", TUESDAY: "Tue", WEDNESDAY: "Wed", THURSDAY: "Thu",
  FRIDAY: "Fri", SATURDAY: "Sat", SUNDAY: "Sun",
};

const empty: CourseInput = {
  courseSymbol: "", courseNumber: "", majors: [],
  roomGroups: "LECTURE", timeGroups: "IN_PERSON",
  departmentId: undefined, teacherId: undefined, sectionNumber: 1,
  roomId: undefined, timeSlotId: undefined,
};

export default function CoursesPage() {
  const { toast } = useToast();
  const list        = Courses.useList();
  const rooms       = Rooms.useList();
  const timeSlots   = TimeSlots.useList();
  const teachers    = Teachers.useList();
  const departments = Departments.useList();
  const create      = Courses.useCreate();
  const remove      = Courses.useDelete();
  const deleteAll   = useDeleteAllCourses();

  const [form, setForm]             = useState<CourseInput>(empty);
  const [majorsText, setMajorsText] = useState("");
  const [showForm, setShowForm]     = useState(true);

  const set = (patch: Partial<CourseInput>) => setForm(f => ({ ...f, ...patch }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const majors = majorsText.split(",").map(s => s.trim()).filter(Boolean);
    if (majors.length === 0) {
      toast({ title: "Add at least one major", variant: "destructive" });
      return;
    }
    try {
      await create.mutateAsync({ ...form, majors });
      setForm(empty);
      setMajorsText("");
      toast({ title: "Course created" });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Courses</h1>
          <p className="text-muted-foreground text-sm">
            Each course can be linked to a department, doctor, room, and time slot.
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <a href={exportScheduleUrl()} download>
            <Button variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" /> Export Excel
            </Button>
          </a>

          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button variant="destructive" size="sm" disabled={deleteAll.isPending || !list.data?.length}>
                <Trash2 className="h-4 w-4 mr-2" />
                Delete All
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Delete all courses?</AlertDialogTitle>
                <AlertDialogDescription>
                  This will permanently delete all {list.data?.length ?? 0} courses and their associated
                  lecture assignments. This action cannot be undone.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Cancel</AlertDialogCancel>
                <AlertDialogAction
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  onClick={() => {
                    deleteAll.mutate(undefined, {
                      onSuccess: () => toast({ title: `All courses deleted` }),
                      onError: (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                    });
                  }}>
                  Delete all
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>

          <Button variant="ghost" size="sm" onClick={() => setShowForm(v => !v)}>
            {showForm
              ? <><ChevronUp className="h-4 w-4 mr-1" />Collapse</>
              : <><ChevronDown className="h-4 w-4 mr-1" />Add Course</>}
          </Button>
        </div>
      </div>

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Plus className="h-4 w-4" /> Add Course
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={submit} className="space-y-5">

              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Course Info</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                  <div>
                    <Label>Course Symbol</Label>
                    <Input value={form.courseSymbol} onChange={e => set({ courseSymbol: e.target.value })}
                      placeholder="CS" required />
                  </div>
                  <div>
                    <Label>Course Number</Label>
                    <Input value={form.courseNumber} onChange={e => set({ courseNumber: e.target.value })}
                      placeholder="101" required />
                  </div>
                  <div>
                    <Label>Majors (comma-separated)</Label>
                    <Input value={majorsText} onChange={e => setMajorsText(e.target.value)} placeholder="CS, SE" />
                  </div>
                  <div>
                    <Label>Required Room Type</Label>
                    <Select value={form.roomGroups} onValueChange={(v: RoomType) => set({ roomGroups: v })}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>{ROOM_TYPES.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}</SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Teaching Method</Label>
                    <Select value={form.timeGroups} onValueChange={(v: TeachingMethod) => set({ timeGroups: v })}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>{TEACHING_METHODS.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}</SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Department (optional)</Label>
                    <Select
                      value={form.departmentId?.toString() ?? "none"}
                      onValueChange={v => set({ departmentId: v !== "none" ? Number(v) : undefined })}>
                      <SelectTrigger><SelectValue placeholder="Select…" /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">— None —</SelectItem>
                        {(departments.data ?? []).map(d => (
                          <SelectItem key={d.id} value={d.id.toString()}>{d.code} – {d.name}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>

              <div className="border-t pt-4">
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Doctor &amp; Section</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <Label>Doctor (optional)</Label>
                    <Select
                      value={form.teacherId?.toString() ?? "none"}
                      onValueChange={v => set({ teacherId: v !== "none" ? Number(v) : undefined })}>
                      <SelectTrigger><SelectValue placeholder="Select doctor…" /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">— None —</SelectItem>
                        {(teachers.data ?? []).map(t => (
                          <SelectItem key={t.id} value={t.id.toString()}>
                            {t.name}{t.department ? ` (${t.department.code})` : ""}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Section Number</Label>
                    <Input type="number" min={1} value={form.sectionNumber ?? 1}
                      onChange={e => set({ sectionNumber: parseInt(e.target.value) || 1 })} />
                  </div>
                </div>
              </div>

              <div className="border-t pt-4">
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Room (optional)</p>
                <div>
                  <Label>Select Room</Label>
                  <Select
                    value={form.roomId?.toString() ?? "none"}
                    onValueChange={v => set({ roomId: v !== "none" ? Number(v) : undefined })}>
                    <SelectTrigger><SelectValue placeholder="Select room…" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">— None —</SelectItem>
                      {(rooms.data ?? []).map(r => (
                        <SelectItem key={r.id} value={r.id.toString()}>
                          {r.building} {r.roomNumber} ({r.roomType})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="border-t pt-4">
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Time Slot (optional)</p>
                <div>
                  <Label>Select Time Slot</Label>
                  <Select
                    value={form.timeSlotId?.toString() ?? "none"}
                    onValueChange={v => set({ timeSlotId: v !== "none" ? Number(v) : undefined })}>
                    <SelectTrigger><SelectValue placeholder="Select time slot…" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">— None —</SelectItem>
                      {(timeSlots.data ?? []).map(ts => (
                        <SelectItem key={ts.id} value={ts.id.toString()}>
                          {(ts.startTime as string)?.slice(0, 5)}–{(ts.endTime as string)?.slice(0, 5)}
                          {" · "}{ts.days.map(d => d.slice(0, 3)).join(", ")}
                          {" · "}{ts.teachingMethod}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="pt-1">
                <Button type="submit" disabled={create.isPending}>
                  {create.isPending ? "Saving…" : "Add Course"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">All Courses ({list.data?.length ?? 0})</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {list.isLoading ? (
            <div className="p-4 text-sm text-muted-foreground">Loading…</div>
          ) : list.error ? (
            <div className="p-4 text-sm text-destructive">{(list.error as Error).message}</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Course</TableHead>
                    <TableHead>Dept</TableHead>
                    <TableHead>Majors</TableHead>
                    <TableHead>Doctor</TableHead>
                    <TableHead>Room</TableHead>
                    <TableHead>Time Slot</TableHead>
                    <TableHead>Method</TableHead>
                    <TableHead className="w-10" />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {list.data?.map((c: Course) => (
                    <TableRow key={c.id}>
                      <TableCell className="font-mono font-semibold whitespace-nowrap">
                        {c.courseSymbol} {c.courseNumber}
                      </TableCell>
                      <TableCell>
                        {c.department
                          ? <Badge variant="outline" className="text-xs">{c.department.code}</Badge>
                          : <span className="text-muted-foreground text-xs">—</span>}
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          {c.majors.map(m => <Badge key={m} variant="secondary" className="text-xs">{m}</Badge>)}
                        </div>
                      </TableCell>
                      <TableCell>
                        {c.teacher
                          ? <span className="text-sm">{c.teacher.name}</span>
                          : c.instructor
                          ? <span className="text-sm text-muted-foreground">{c.instructor}</span>
                          : <span className="text-muted-foreground text-xs">—</span>}
                      </TableCell>
                      <TableCell>
                        {c.room
                          ? <span className="text-sm">{c.room.building} {c.room.roomNumber}</span>
                          : <span className="text-muted-foreground text-xs">—</span>}
                      </TableCell>
                      <TableCell>
                        {c.timeSlot ? (
                          <div className="text-xs leading-relaxed">
                            <div className="font-medium">
                              {(c.timeSlot.startTime as string)?.slice(0, 5)}–{(c.timeSlot.endTime as string)?.slice(0, 5)}
                            </div>
                            <div className="text-muted-foreground">
                              {(c.timeSlot.days ?? []).map((d: DayOfWeek) => DAY_LABELS[d]).join(" · ")}
                            </div>
                          </div>
                        ) : <span className="text-muted-foreground text-xs">—</span>}
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="text-xs">{c.timeGroups}</Badge>
                      </TableCell>
                      <TableCell>
                        <Button size="icon" variant="ghost"
                          onClick={() => remove.mutate(c.id)}
                          disabled={remove.isPending}>
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                  {!list.data?.length && (
                    <TableRow>
                      <TableCell colSpan={8} className="text-center text-muted-foreground py-10">
                        No courses yet. Add one above.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
