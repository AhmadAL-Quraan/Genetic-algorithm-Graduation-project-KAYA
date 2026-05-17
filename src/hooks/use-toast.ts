import { toast as sonnerToast } from "sonner";

type ToastOpts = {
  title?: string;
  description?: string;
  variant?: "default" | "destructive";
};

export function useToast() {
  return {
    toast: ({ title, description, variant }: ToastOpts) => {
      const msg = title ?? "";
      const opts = description ? { description } : undefined;
      if (variant === "destructive") sonnerToast.error(msg, opts);
      else sonnerToast.success(msg, opts);
    },
  };
}

export const toast = sonnerToast;