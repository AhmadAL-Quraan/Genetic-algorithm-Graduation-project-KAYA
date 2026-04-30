import { useState } from "react";
import { Teachers, Departments, useDeleteAllTeachers, type TeacherInput } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription, AlertDialogFooter,
  AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

const empty: TeacherInput = { name: "", email: "", departmentId: undefined };

export default function TeachersPage() {
  const { toast } = useToast();
  const list        = Teachers.useList();
  const departments = Departments.useList();
  const create      = Teachers.useCreate();
  const remove      = Teachers.useDelete();
  const deleteAll   = useDeleteAllTeachers();
  const [form, setForm] = useState<TeacherInput>(empty);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await create.mutateAsync({
        name: form.name,
        email: form.email || undefined,
        departmentId: form.departmentId || undefined,
      });
      setForm(empty);
      toast({ title: "Doctor added" });
    } catch (err: any) {
      toast({ title: "Failed", description: err.message, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-semibold">Doctors</h1>
          <p className="text-muted-foreground text-sm">Doctors and instructors available for course assignment.</p>
        </div>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button variant="destructive" size="sm" disabled={deleteAll.isPending || !list.data?.length}>
              <Trash2 className="h-4 w-4 mr-2" /> Delete All
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete all doctors?</AlertDialogTitle>
              <AlertDialogDescription>
                This will permanently delete all {list.data?.length ?? 0} doctors and remove them from any lectures. This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                onClick={() => deleteAll.mutate(undefined, {
                  onSuccess: () => toast({ title: "All doctors deleted" }),
                  onError: (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                })}>
                Delete all
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2"><Plus className="h-4 w-4" /> Add doctor</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div>
              <Label>Name</Label>
              <Input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}
                placeholder="Dr. Smith" required />
            </div>
            <div>
              <Label>Email (optional)</Label>
              <Input type="email" value={form.email ?? ""} onChange={e => setForm({ ...form, email: e.target.value })}
                placeholder="dr.smith@uni.edu" />
            </div>
            <div>
              <Label>Department (optional)</Label>
              <Select
                value={form.departmentId?.toString() ?? "none"}
                onValueChange={v => setForm({ ...form, departmentId: v !== "none" ? Number(v) : undefined })}>
                <SelectTrigger><SelectValue placeholder="Select…" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">— None —</SelectItem>
                  {(departments.data ?? []).map(d => (
                    <SelectItem key={d.id} value={d.id.toString()}>{d.code} – {d.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button type="submit" disabled={create.isPending} className="w-full">Add</Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">All doctors ({list.data?.length ?? 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {list.isLoading ? (
            <div className="text-sm text-muted-foreground">Loading…</div>
          ) : !list.data?.length ? (
            <div className="text-sm text-muted-foreground">No doctors yet.</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map(t => (
                  <TableRow key={t.id}>
                    <TableCell className="font-medium">{t.name}</TableCell>
                    <TableCell className="text-muted-foreground text-sm">{t.email ?? "—"}</TableCell>
                    <TableCell>
                      {t.department
                        ? <Badge variant="secondary">{t.department.code}</Badge>
                        : <span className="text-muted-foreground text-xs">—</span>}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => remove.mutate(t.id)}
                        disabled={remove.isPending}>
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
