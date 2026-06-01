import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription
} from "@/components/ui/dialog";
import { AlertTriangle } from "lucide-react";

interface ConfirmState {
  message: string;
  description?: string;
  confirmLabel?: string;
  onConfirm: () => void;
}

export function useConfirm() {
  const [state, setState] = useState<ConfirmState | null>(null);

  const confirm = useCallback((message: string, onConfirm: () => void, description?: string, confirmLabel?: string) => {
    setState({ message, onConfirm, description, confirmLabel });
  }, []);

  const handleConfirm = () => {
    state?.onConfirm();
    setState(null);
  };

  const handleCancel = () => setState(null);

  const ConfirmDialog = () => (
    <Dialog open={!!state} onOpenChange={(open) => !open && handleCancel()}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-destructive">
            <AlertTriangle className="h-5 w-5" />
            {state?.message}
          </DialogTitle>
          {state?.description && (
            <DialogDescription>{state.description}</DialogDescription>
          )}
        </DialogHeader>
        <DialogFooter className="mt-4 gap-2">
          <Button variant="outline" onClick={handleCancel}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={handleConfirm}>
            {state?.confirmLabel ?? "Yes, Delete"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );

  return { confirm, ConfirmDialog };
}
