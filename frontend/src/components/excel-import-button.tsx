import { useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Upload, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useQueryClient } from "@tanstack/react-query";

interface ImportSummary {
  rowsProcessed: number;
  rowsSkipped: number;
  coursesCreated: number;
  roomsCreated: number;
  timeSlotsCreated: number;
  lecturesCreated: number;
  teachersCreated: number;
  warnings: string[];
}

export function ExcelImportButton({ variant = "default" as const }) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const { toast } = useToast();
  const qc = useQueryClient();

  async function onFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    setBusy(true);
    try {
      const fd = new FormData();
      fd.append("file", file);
      const res = await fetch("/api/import/excel", { method: "POST", body: fd });
      const text = await res.text();
      if (!res.ok) throw new Error(text || res.statusText);
      const summary = JSON.parse(text) as ImportSummary;
      const parts = [
        `${summary.lecturesCreated} lectures`,
        `${summary.coursesCreated} courses`,
        summary.teachersCreated > 0 ? `${summary.teachersCreated} doctors` : null,
        `${summary.roomsCreated} rooms`,
        `${summary.timeSlotsCreated} time slots`,
      ].filter(Boolean).join(", ");
      toast({
        title: `Imported ${file.name}`,
        description: `${parts} created. ${summary.rowsSkipped} rows skipped.`,
      });
      await Promise.all([
        qc.invalidateQueries({ queryKey: ["courses"] }),
        qc.invalidateQueries({ queryKey: ["rooms"] }),
        qc.invalidateQueries({ queryKey: ["time-slots"] }),
        qc.invalidateQueries({ queryKey: ["lectures"] }),
        qc.invalidateQueries({ queryKey: ["teachers"] }),
      ]);
    } catch (err: any) {
      toast({ title: "Import failed", description: err.message, variant: "destructive" });
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        className="hidden"
        onChange={onFile}
      />
      <Button variant={variant} disabled={busy} onClick={() => inputRef.current?.click()}>
        {busy ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Upload className="h-4 w-4 mr-2" />}
        {busy ? "Importing…" : "Import from Excel"}
      </Button>
    </>
  );
}
