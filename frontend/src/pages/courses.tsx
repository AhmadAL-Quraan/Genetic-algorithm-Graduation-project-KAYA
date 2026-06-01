import { useState } from "react";
import {
  Courses, useDeleteAllCourses,
  ROOM_TYPES, TEACHING_METHODS,
  type Course, type CourseInput, type RoomType, type TeachingMethod,
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

const empty: CourseInput = {
  courseSymbol: "", courseNumber: "",
  roomGroups: "LECTURE", timeGroups: "IN_PERSON",
  instructorId: undefined,
  majors: [],
};

const METHOD_COLORS: Record<string, string> = {
  IN_PERSON: "bg-green-100 text-green-800 border-green-200",
  BLENDED:   "bg-blue-100  text-blue-800  border-blue-200",
  ONLINE:    "bg-purple-100 text-purple-800 border-purple-200",
};

export default function CoursesPage() {
  const { toast } = useToast();
  const list      = Courses.useList();
  const create    = Courses.useCreate();
  const remove    = Courses.useDelete();
  const deleteAll = useDeleteAllCourses();

  const [form, setForm]         = useState<CourseInput>(empty);
  const [showForm, setShowForm] = useState(true);

  const set = (patch: Partial<CourseInput>) => setForm(f => ({ ...f, ...patch }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await create.mutateAsync(form);
      setForm(empty);
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
            Pick a teaching method — the algorithm assigns a time slot automatically from
            the windows you configured in the Time Slots page.
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
                <Trash2 className="h-4 w-4 mr-2" /> Delete All
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Delete all courses?</AlertDialogTitle>
                <AlertDialogDescription>
                  This will permanently delete all {list.data?.length ?? 0} courses and their
                  associated lecture assignments. This action cannot be undone.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Cancel</AlertDialogCancel>
                <AlertDialogAction
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  onClick={() => deleteAll.mutate(undefined, {
                    onSuccess: () => toast({ title: "All courses deleted" }),
                    onError: (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                  })}>
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

              {/* Course Info */}
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">Course Info</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                  <div>
                    <Label>Course Symbol</Label>
                    <Input value={form.courseSymbol}
                      onChange={e => set({ courseSymbol: e.target.value })}
                      placeholder="CS" required />
                  </div>
                  <div>
                    <Label>Course Number</Label>
                    <Input value={form.courseNumber}
                      onChange={e => set({ courseNumber: e.target.value })}
                      placeholder="101" required />
                  </div>
                  <div>
                    <Label>Required Room Type</Label>
                    <Select value={form.roomGroups} onValueChange={(v: RoomType) => set({ roomGroups: v })}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        {ROOM_TYPES.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Teaching Method</Label>
                    <Select value={form.timeGroups} onValueChange={(v: TeachingMethod) => set({ timeGroups: v })}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        {TEACHING_METHODS.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}
                      </SelectContent>
                    </Select>
                    <p className="text-xs text-muted-foreground mt-1">
                      The algorithm will pick a time from the matching window.
                    </p>
                  </div>
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
                    <TableHead>Room Type</TableHead>
                    <TableHead>Teaching Method</TableHead>
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
                        <Badge variant="outline" className="text-xs">{c.roomGroups}</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className={`text-xs ${METHOD_COLORS[c.timeGroups]}`}>
                          {c.timeGroups}
                        </Badge>
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
                      <TableCell colSpan={6} className="text-center text-muted-foreground py-10">
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
