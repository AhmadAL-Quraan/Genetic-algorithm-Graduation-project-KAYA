import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Instructors, type InstructorInput } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

export const Route = createFileRoute("/instructors")({ component: InstructorsPage });

const empty: InstructorInput = { instructorName: "" };

function InstructorsPage() {
  const { toast } = useToast();
  const list = Instructors.useList();
  const create = Instructors.useCreate();
  const remove = Instructors.useDelete();
  const deleteAll = Instructors.useDeleteAll();
  const [form, setForm] = useState<InstructorInput>(empty);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await create.mutateAsync(form);
      setForm(empty);
      toast({ title: "Instructor added" });
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
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Instructors</h1>
          <p className="text-sm text-muted-foreground">
            Instructors available for lecture assignment.
          </p>
        </div>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button
              variant="destructive"
              size="sm"
              disabled={deleteAll.isPending || !list.data?.length}
            >
              <Trash2 className="mr-2 h-4 w-4" /> Delete All
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete all instructors?</AlertDialogTitle>
              <AlertDialogDescription>
                This will permanently delete all {list.data?.length ?? 0} instructors.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                onClick={() => deleteAll.mutate()}
              >
                Delete all
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Plus className="h-4 w-4" /> Add Instructor
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={submit}
            className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_auto]"
          >
            <div>
              <Label>Name</Label>
              <Input
                value={form.instructorName}
                onChange={(e) => setForm({ instructorName: e.target.value })}
                placeholder="Dr. Smith"
                required
              />
            </div>
            <div className="flex items-end">
              <Button type="submit" disabled={create.isPending} className="w-full">
                Add
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            All Instructors ({list.data?.length ?? 0})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {list.isLoading ? (
            <div className="text-sm text-muted-foreground">Loading…</div>
          ) : !list.data?.length ? (
            <div className="text-sm text-muted-foreground">No instructors yet.</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.data.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell className="font-medium">{t.instructorName}</TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => remove.mutate(t.id)}
                        disabled={remove.isPending}
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