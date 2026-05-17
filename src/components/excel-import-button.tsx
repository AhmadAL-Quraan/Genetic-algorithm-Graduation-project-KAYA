import { useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Upload, Loader2 } from "lucide-react";
import { useImportExcel, type ImportSummary } from "@/lib/api";
import { useToast } from "@/hooks/use-toast";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";

export function ExcelImportButton() {
  const inputRef = useRef<HTMLInputElement>(null);
  const importMut = useImportExcel();
  const { toast } = useToast();
  const [summary, setSummary] = useState<ImportSummary | null>(null);

  const onChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const s = await importMut.mutateAsync(file);
      setSummary(s);
      toast({ title: "Imported", description: `${s.rowsProcessed} rows processed` });
    } catch (err) {
      toast({
        title: "Import failed",
        description: (err as Error).message,
        variant: "destructive",
      });
    } finally {
      if (inputRef.current) inputRef.current.value = "";
    }
  };

  return (
    <>
      <input ref={inputRef} type="file" accept=".xlsx,.xls" hidden onChange={onChange} />
      <Button
        variant="outline"
        size="sm"
        disabled={importMut.isPending}
        onClick={() => inputRef.current?.click()}
      >
        {importMut.isPending ? (
          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
        ) : (
          <Upload className="mr-2 h-4 w-4" />
        )}
        Import from Excel
      </Button>

      <Dialog open={!!summary} onOpenChange={(o) => !o && setSummary(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Import summary</DialogTitle>
            <DialogDescription>Result of parsing the Excel file.</DialogDescription>
          </DialogHeader>
          {summary && (
            <div className="space-y-2 text-sm">
              <p>Rows processed: <strong>{summary.rowsProcessed}</strong></p>
              <p>Rows skipped: <strong>{summary.rowsSkipped}</strong></p>
              <p>Courses created: <strong>{summary.coursesCreated}</strong></p>
              <p>Rooms created: <strong>{summary.roomsCreated}</strong></p>
              <p>Time slots created: <strong>{summary.timeSlotsCreated}</strong></p>
              <p>Instructors created: <strong>{summary.instructorsCreated}</strong></p>
              <p>Lectures created: <strong>{summary.lecturesCreated}</strong></p>
              {summary.warnings?.length > 0 && (
                <div className="mt-3">
                  <p className="font-semibold mb-1">Warnings:</p>
                  <ul className="list-inside list-disc text-xs text-muted-foreground space-y-0.5 max-h-48 overflow-auto">
                    {summary.warnings.map((w, i) => <li key={i}>{w}</li>)}
                  </ul>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}