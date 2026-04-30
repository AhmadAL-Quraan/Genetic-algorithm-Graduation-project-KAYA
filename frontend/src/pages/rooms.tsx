import { useState } from "react";
import { Rooms, ROOM_TYPES, useDeleteAllRooms, type RoomInput, type RoomType } from "@/lib/api";
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

const empty: RoomInput = { building: "", roomNumber: "", roomType: "LECTURE" };

export default function RoomsPage() {
  const { toast } = useToast();
  const list      = Rooms.useList();
  const create    = Rooms.useCreate();
  const remove    = Rooms.useDelete();
  const deleteAll = useDeleteAllRooms();
  const [form, setForm] = useState<RoomInput>(empty);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try { await create.mutateAsync(form); setForm(empty); toast({ title: "Room added" }); }
    catch (err: any) { toast({ title: "Failed", description: err.message, variant: "destructive" }); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-semibold">Rooms</h1>
          <p className="text-muted-foreground text-sm">Lecture halls, labs, and other spaces available for scheduling.</p>
        </div>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button variant="destructive" size="sm" disabled={deleteAll.isPending || !list.data?.length}>
              <Trash2 className="h-4 w-4 mr-2" /> Delete All
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete all rooms?</AlertDialogTitle>
              <AlertDialogDescription>
                This will permanently delete all {list.data?.length ?? 0} rooms and remove them from any lectures. This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                onClick={() => deleteAll.mutate(undefined, {
                  onSuccess: () => toast({ title: "All rooms deleted" }),
                  onError: (err: any) => toast({ title: "Failed", description: err.message, variant: "destructive" }),
                })}>
                Delete all
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base flex items-center gap-2"><Plus className="h-4 w-4" /> Add room</CardTitle></CardHeader>
        <CardContent>
          <form onSubmit={submit} className="grid grid-cols-1 sm:grid-cols-4 gap-3">
            <div>
              <Label>Building</Label>
              <Input value={form.building} onChange={e => setForm({ ...form, building: e.target.value })} required />
            </div>
            <div>
              <Label>Room #</Label>
              <Input value={form.roomNumber} onChange={e => setForm({ ...form, roomNumber: e.target.value })} required />
            </div>
            <div>
              <Label>Type</Label>
              <Select value={form.roomType} onValueChange={(v: RoomType) => setForm({ ...form, roomType: v })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>{ROOM_TYPES.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}</SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button type="submit" disabled={create.isPending} className="w-full">Add</Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">All rooms ({list.data?.length ?? 0})</CardTitle></CardHeader>
        <CardContent>
          {list.isLoading ? <div className="text-sm text-muted-foreground">Loading…</div> :
           !list.data?.length ? <div className="text-sm text-muted-foreground">No rooms yet.</div> :
            <Table>
              <TableHeader><TableRow><TableHead>Building</TableHead><TableHead>Room</TableHead><TableHead>Type</TableHead><TableHead /></TableRow></TableHeader>
              <TableBody>
                {list.data.map(r => (
                  <TableRow key={r.id}>
                    <TableCell>{r.building}</TableCell>
                    <TableCell className="font-medium">{r.roomNumber}</TableCell>
                    <TableCell><Badge variant="secondary">{r.roomType}</Badge></TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => remove.mutate(r.id)}><Trash2 className="h-4 w-4" /></Button>
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
