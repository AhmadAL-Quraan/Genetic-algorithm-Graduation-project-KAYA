import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import {
  Courses,
  Instructors,
  Lectures,
  type LectureInput,
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
import { Input } from "@/components/ui/input";
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

const empty: LectureInput = { courseId: 0, instructorId: 0, number: 1 };

function LecturesPage() {
  const { toast } = useToast();
  const courses = Courses.useList();
  const instructors = Instructors.useList();
  const list = Lectures.useList();
  const create = Lectures.useCreate();
  const remove = Lectures.useDelete();
  const removeAll = Lectures.useDeleteAll();

  const [form, setForm] = useState<LectureInput>(empty);

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
      toast({ title: "Lecture section added" });
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
    if (!confirm(`Delete all ${list.data.length} lecture section(s)?`)) return;
    try {
      await removeAll.mutateAsync();
      toast({ title: "All lecture sections deleted" });
    } catch (err) {
      toast({
        title: "Failed",
        description: (err as Error).message,
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Lectures</h1>
        <p className="text-sm text-muted-foreground">
          A lecture is one section of a course. Assign an instructor — the algorithm
          assigns a room and time slot automatically.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Plus className="h-4 w-4" /> Add lecture section
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 gap-3 sm:grid-cols-4">
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

            <div>
              <Label>Section #</Label>
              <Input
                type="number"
                min={1}
                value={form.number ?? 1}
                onChange={(e) =>
                  setForm((f) => ({ ...f, number: Number(e.target.value) }))
                }
              />
            </div>

            <div className="flex items-end">
              <Button
                type="submit"
                disabled={create.isPending}
                className="w-full sm:w-auto"
              >
                {create.isPending ? "Adding…" : "Add lecture"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base">
            All lecture sections ({list.data?.length ?? 0})
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
              No lecture sections yet.
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Sec</TableHead>
                  <TableHead>Instructor</TableHead>
                  <TableHead>Room</TableHead>
                  <TableHead>Time slot</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell className="font-medium">
                      {l.course
                        ? `${l.course.courseSymbol} ${l.course.courseNumber}`
                        : "—"}
                    </TableCell>
                    <TableCell>{l.number ?? "—"}</TableCell>
                    <TableCell>{l.instructor?.instructorName ?? "—"}</TableCell>
                    <TableCell>
                      {l.room
                        ? `${l.room.building} ${l.room.roomNumber}`
                        : "—"}
                    </TableCell>
                    <TableCell className="text-xs">
                      {l.timeSlot
                        ? `${l.timeSlot.days?.map((d) => d.slice(0, 3)).join(",")} ${l.timeSlot.startTime?.slice(0, 5)}–${l.timeSlot.endTime?.slice(0, 5)}`
                        : "—"}
                    </TableCell>
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