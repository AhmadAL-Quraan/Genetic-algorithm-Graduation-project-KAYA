import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import {
  Courses,
  Instructors,
  ManualEntries,
  type ManualEntry,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export const Route = createFileRoute("/lectures")({ component: LecturesPage });

const empty: ManualEntry = { courseId: 0, instructorId: 0 };

function LecturesPage() {
  const { toast } = useToast();
  const courses = Courses.useList();
  const instructors = Instructors.useList();
  const list = ManualEntries.useList();
  const create = ManualEntries.useCreate();
  const removeAll = ManualEntries.useDeleteAll();

  const [form, setForm] = useState<ManualEntry>(empty);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.courseId) {
      toast({ title: "Pick a course", variant: "destructive" });
      return;
    }
    if (!form.instructorId) {
      toast({ title: "Pick an instructor", variant: "destructive" });
      return;
    }
    try {
      await create.mutateAsync(form);
      setForm(empty);
      toast({ title: "Manual entry added" });
    } catch (err) {
      toast({
        title: "Failed",
        description: (err as Error).message,
        variant: "destructive",
      });
    }
  };

  const handleDeleteAll = async () => {
    if (!list.data?.length) return;
    if (!confirm(`Delete all ${list.data.length} manual entries?`)) return;
    try {
      await removeAll.mutateAsync();
      toast({ title: "All manual entries deleted" });
    } catch (err) {
      toast({
        title: "Failed",
        description: (err as Error).message,
        variant: "destructive",
      });
    }
  };

  const findCourse = (id: number) =>
<<<<<<< HEAD
    courses.data?.find((c) => c.id === id) ?? null;
  const findInstructor = (id: number) =>
    instructors.data?.find((i) => i.id === id) ?? null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Lectures</h1>
        <p className="text-sm text-muted-foreground">
          Pair a course with an instructor. The algorithm assigns a room and
          time slot automatically.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Plus className="h-4 w-4" /> Add manual entry
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <div>
              <Label>Course</Label>
              <Select
                value={form.courseId ? String(form.courseId) : ""}
                onValueChange={(v) =>
                  setForm((f) => ({ ...f, courseId: Number(v) }))
                }
              >
                <SelectTrigger>
                  <SelectValue
                    placeholder={
                      courses.data?.length ? "Select course" : "Add a course first"
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {courses.data?.map((c) => (
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
                value={form.instructorId ? String(form.instructorId) : ""}
                onValueChange={(v) =>
                  setForm((f) => ({ ...f, instructorId: Number(v) }))
                }
              >
                <SelectTrigger>
                  <SelectValue
                    placeholder={
                      instructors.data?.length ? "Select instructor" : "Add one first"
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {(instructors.data ?? []).map((t) => (
                    <SelectItem key={t.id} value={String(t.id)}>
                      {t.instructorName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-end">
              <Button
                type="submit"
                disabled={create.isPending}
                className="w-full sm:w-auto"
              >
                {create.isPending ? "Adding…" : "Add entry"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base">
            All manual entries ({list.data?.length ?? 0})
          </CardTitle>
          {(list.data?.length ?? 0) > 0 && (
            <Button
              variant="destructive"
              size="sm"
              onClick={handleDeleteAll}
              disabled={removeAll.isPending}
            >
              <Trash2 className="mr-1 h-4 w-4" />
              {removeAll.isPending ? "Deleting…" : "Delete all"}
            </Button>
          )}
        </CardHeader>
        <CardContent>
          {list.isLoading ? (
            <div className="text-sm text-muted-foreground">Loading…</div>
          ) : !list.data?.length ? (
            <div className="text-sm text-muted-foreground">
              No manual entries yet.
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Instructor</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map((m, idx) => {
                  const c = findCourse(m.courseId);
                  const i = findInstructor(m.instructorId);
                  return (
                    <TableRow key={`${m.courseId}-${m.instructorId}-${idx}`}>
                      <TableCell className="font-medium">
                        {c ? `${c.courseSymbol} ${c.courseNumber}` : `#${m.courseId}`}
                      </TableCell>
                      <TableCell>
                        {i ? i.instructorName : `#${m.instructorId}`}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
=======
      courses.data?.find((c) => c.id === id) ?? null;
  const findInstructor = (id: number) =>
      instructors.data?.find((i) => i.id === id) ?? null;

  return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-semibold">Lectures</h1>
          <p className="text-sm text-muted-foreground">
            Pair a course with an instructor. The algorithm assigns a room and
            time slot automatically.
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Plus className="h-4 w-4" /> Add manual entry
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={submit} className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              <div>
                <Label>Course</Label>
                <Select
                    value={form.courseId ? String(form.courseId) : ""}
                    onValueChange={(v) =>
                        setForm((f) => ({ ...f, courseId: Number(v) }))
                    }
                >
                  <SelectTrigger>
                    <SelectValue
                        placeholder={
                          courses.data?.length ? "Select course" : "Add a course first"
                        }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {courses.data?.map((c) => (
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
                    value={form.instructorId ? String(form.instructorId) : ""}
                    onValueChange={(v) =>
                        setForm((f) => ({ ...f, instructorId: Number(v) }))
                    }
                >
                  <SelectTrigger>
                    <SelectValue
                        placeholder={
                          instructors.data?.length ? "Select instructor" : "Add one first"
                        }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {(instructors.data ?? []).map((t) => (
                        <SelectItem key={t.id} value={String(t.id)}>
                          {t.instructorName}
                        </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="flex items-end">
                <Button
                    type="submit"
                    disabled={create.isPending}
                    className="w-full sm:w-auto"
                >
                  {create.isPending ? "Adding…" : "Add entry"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-base">
              All manual entries ({list.data?.length ?? 0})
            </CardTitle>
            {(list.data?.length ?? 0) > 0 && (
                <Button
                    variant="destructive"
                    size="sm"
                    onClick={handleDeleteAll}
                    disabled={removeAll.isPending}
                >
                  <Trash2 className="mr-1 h-4 w-4" />
                  {removeAll.isPending ? "Deleting…" : "Delete all"}
                </Button>
            )}
          </CardHeader>
          <CardContent>
            {list.isLoading ? (
                <div className="text-sm text-muted-foreground">Loading…</div>
            ) : !list.data?.length ? (
                <div className="text-sm text-muted-foreground">
                  No manual entries yet.
                </div>
            ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Course</TableHead>
                      <TableHead>Instructor</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {list.data.map((m, idx) => {
                      const c = findCourse(m.courseId);
                      const i = findInstructor(m.instructorId);
                      return (
                          <TableRow key={`${m.courseId}-${m.instructorId}-${idx}`}>
                            <TableCell className="font-medium">
                              {c ? `${c.courseSymbol} ${c.courseNumber}` : `#${m.courseId}`}
                            </TableCell>
                            <TableCell>
                              {i ? i.instructorName : `#${m.instructorId}`}
                            </TableCell>
                          </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
            )}
          </CardContent>
        </Card>
      </div>
>>>>>>> 5d14d34 (KAYA SB&Frontend Version 8 - Ahmad Obeidat)
  );
}
