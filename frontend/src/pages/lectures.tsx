import { useState } from "react";
import { Courses, Lectures, type LectureInput } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export default function LecturesPage() {
  const { toast } = useToast();
  const courses = Courses.useList();
  const list = Lectures.useList();
  const create = Lectures.useCreate();
  const remove = Lectures.useDelete();

  const [form, setForm] = useState<LectureInput>({ courseId: 0, instructor: "", number: 1 });

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.courseId) { toast({ title: "Pick a course", variant: "destructive" }); return; }
    try {
      await create.mutateAsync(form);
      setForm({ courseId: 0, instructor: "", number: 1 });
      toast({ title: "Lecture added" });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Lectures</h1>
        <p className="text-muted-foreground text-sm">A lecture is one section of a course taught by an instructor. The genetic algorithm assigns rooms and time slots.</p>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Plus className="h-4 w-4" /> Add lecture</CardTitle></CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 sm:grid-cols-4 gap-3">
            <div className="sm:col-span-2">
              <Label>Course</Label>
              <Select
                value={form.courseId ? String(form.courseId) : ""}
                onValueChange={v => setForm({ ...form, courseId: Number(v) })}
              >
                <SelectTrigger><SelectValue placeholder={courses.data?.length ? "Select course" : "Add a course first"} /></SelectTrigger>
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
              <Label>Section #</Label>
              <Input type="number" min={1} value={form.number ?? 1} onChange={e => setForm({ ...form, number: Number(e.target.value) })} />
            </div>
            <div>
              <Label>Instructor</Label>
              <Input value={form.instructor} onChange={e => setForm({ ...form, instructor: e.target.value })} required />
            </div>
            <div className="sm:col-span-4">
              <Button type="submit" disabled={create.isPending}>Add lecture</Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">All lectures ({list.data?.length ?? 0})</CardTitle></CardHeader>
        <CardContent>
          {list.isLoading ? <div className="text-sm text-muted-foreground">Loading…</div> :
           !list.data?.length ? <div className="text-sm text-muted-foreground">No lectures yet.</div> :
            <Table>
              <TableHeader><TableRow>
                <TableHead>Course</TableHead><TableHead>Section</TableHead>
                <TableHead>Instructor</TableHead><TableHead /></TableRow></TableHeader>
              <TableBody>
                {list.data.map(l => (
                  <TableRow key={l.id}>
                    <TableCell className="font-medium">
                      {l.course ? `${l.course.courseSymbol} ${l.course.courseNumber}` : "—"}
                    </TableCell>
                    <TableCell>{l.number}</TableCell>
                    <TableCell>{l.instructor}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => remove.mutate(l.id)}><Trash2 className="h-4 w-4" /></Button>
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
