import { useState } from "react";
import { Courses, Instructors, Lectures, useDeleteAllLectures, type LectureInput } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const empty: LectureInput = { courseId: 0, instructorId: null };

export default function LecturesPage() {
  const { toast } = useToast();
  const courses  = Courses.useList();
  const instructors = Instructors.useList();
  const list     = Lectures.useList();
  const create   = Lectures.useCreate();
  const remove   = Lectures.useDelete();
  const removeAll = useDeleteAllLectures();

  const [form, setForm] = useState<LectureInput>(empty);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.courseId) { toast({ title: "Pick a course", variant: "destructive" }); return; }
    try {
      await create.mutateAsync(form);
      setForm(empty);
      toast({ title: "Lecture section added" });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  const handleDeleteAll = async () => {
    if (!list.data?.length) return;
    if (!confirm(`Delete all ${list.data.length} lecture section(s)? This cannot be undone.`)) return;
    try {
      await removeAll.mutateAsync();
      toast({ title: "All lecture sections deleted" });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Lectures</h1>
        <p className="text-muted-foreground text-sm">
          A lecture is one section of a course. Assign an instructor — the genetic algorithm
          assigns room and time slot automatically.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Plus className="h-4 w-4" /> Add lecture section
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <Label>Course</Label>
              <Select
                value={form.courseId ? String(form.courseId) : ""}
                onValueChange={v => setForm(f => ({ ...f, courseId: Number(v) }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder={courses.data?.length ? "Select course" : "Add a course first"} />
                </SelectTrigger>
                <SelectContent>
                  {courses.data?.map(c => (
                    <SelectItem key={c.id} value={String(c.id)}>
                      {c.courseSymbol} {c.courseNumber}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label>Instructor</Label>
              <Select
                value={form.instructorId ? String(form.instructorId) : "none"}
                onValueChange={v => setForm(f => ({ ...f, instructorId: v !== "none" ? Number(v) : null }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select instructor…" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">— None —</SelectItem>
                  {(instructors.data ?? []).map(t => (
                    <SelectItem key={t.id} value={String(t.id)}>
                      {t.name}{t.department ? ` (${t.department.code})` : ""}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-end">
              <Button type="submit" disabled={create.isPending} className="w-full sm:w-auto">
                {create.isPending ? "Adding…" : "Add lecture"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base">All lecture sections ({list.data?.length ?? 0})</CardTitle>
          {(list.data?.length ?? 0) > 0 && (
            <Button
              variant="destructive"
              size="sm"
              onClick={handleDeleteAll}
              disabled={removeAll.isPending}
            >
              <Trash2 className="h-4 w-4 mr-1" />
              {removeAll.isPending ? "Deleting…" : "Delete all"}
            </Button>
          )}
        </CardHeader>
        <CardContent>
          {list.isLoading ? (
            <div className="text-sm text-muted-foreground">Loading…</div>
          ) : !list.data?.length ? (
            <div className="text-sm text-muted-foreground">No lecture sections yet.</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Instructor</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map(l => (
                  <TableRow key={l.id}>
                    <TableCell className="font-medium">
                      {l.course ? `${l.course.courseSymbol} ${l.course.courseNumber}` : "—"}
                    </TableCell>
                    <TableCell>
                      {l.instructor
                          ? <span>{l.instructor.name}</span>
                          : <span className="text-muted-foreground text-xs">—</span>}                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="icon"
                        disabled={remove.isPending}
                        onClick={() => remove.mutate(l.id)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
