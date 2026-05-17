import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationOptions,
} from "@tanstack/react-query";

/* ------------------------------------------------------------------ */
/* Base config                                                        */
/* ------------------------------------------------------------------ */

export const API_BASE: string =
  (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, "") ??
  "http://localhost:8080";

async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    let msg = res.statusText;
    try {
      const j = JSON.parse(text);
      msg = j.message || j.error || text || res.statusText;
    } catch {
      msg = text || res.statusText;
    }
    throw new Error(`${res.status} ${msg}`);
  }

  if (res.status === 204) return undefined as T;
  const ct = res.headers.get("content-type") ?? "";
  if (!ct.includes("application/json")) return undefined as T;
  return (await res.json()) as T;
}

/* ------------------------------------------------------------------ */
/* Domain types                                                        */
/* ------------------------------------------------------------------ */

export type RoomType = "LECTURE" | "LAB" | "OTHER";
export const ROOM_TYPES: RoomType[] = ["LECTURE", "LAB", "OTHER"];

export type TeachingMethod = "IN_PERSON" | "ONLINE" | "BLENDED";
export const TEACHING_METHODS: TeachingMethod[] = [
  "IN_PERSON",
  "BLENDED",
  "ONLINE",
];

export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export const DAYS_OF_WEEK: DayOfWeek[] = [
  "SUNDAY",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
];

export const DURATION_OPTIONS = [
  { value: 50, label: "50 min" },
  { value: 60, label: "60 min" },
  { value: 75, label: "75 min" },
  { value: 90, label: "90 min" },
  { value: 120, label: "120 min" },
  { value: 150, label: "150 min" },
  { value: 180, label: "180 min" },
];

export interface Room {
  id: number;
  building: string;
  roomNumber: string;
  roomType: RoomType;
}
export interface RoomInput {
  building: string;
  roomNumber: string;
  roomType: RoomType;
}

export interface Instructor {
  id: number;
  instructorName: string;
}
export interface InstructorInput {
  instructorName: string;
}

export interface TimeSlot {
  id: number;
  startTime: string; // "HH:mm:ss"
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number | null;
}
export interface TimeSlotInput {
  startTime: string; // "HH:mm"
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number;
}

export interface Course {
  id: number;
  courseSymbol: string;
  courseNumber: string;
  roomGroups: RoomType;
  timeGroups: TeachingMethod;
}
export interface CourseInput {
  courseSymbol: string;
  courseNumber: string;
  roomGroups: RoomType;
  timeGroups: TeachingMethod;
}

export interface Lecture {
  id: number;
  course: Course | null;
  number: number | null;
  instructor: Instructor | null;
  timeSlot: TimeSlot | null;
  room: Room | null;
}
export interface LectureInput {
  courseId: number;
  instructorId: number;
  number?: number | null;
  timeSlotId?: number | null;
  roomId?: number | null;
}

export interface FitnessReport {
  id: number;
  roomConflicts: number;
  instructorConflicts: number;
  studentConflicts: number;
  totalPenalty: number;
}

export interface TimeTable {
  id: number;
  fitnessReport: FitnessReport | null;
  lectures: Lecture[];
}

export interface ConflictResponse {
  type: string;
  message: string;
  lectureAId: number;
  lectureBId: number;
  courseA: string;
  courseB: string;
  instructorA: string | null;
  instructorB: string | null;
  timeSlot: string;
}

export interface ManualEntry {
  courseId: number;
  instructorId: number;
}

export interface ImportSummary {
  rowsProcessed: number;
  rowsSkipped: number;
  coursesCreated: number;
  roomsCreated: number;
  timeSlotsCreated: number;
  lecturesCreated: number;
  instructorsCreated: number;
  warnings: string[];
}

/* ------------------------------------------------------------------ */
/* Generic CRUD factory                                                */
/* ------------------------------------------------------------------ */

function makeResource<TItem, TInput>(key: string, path: string) {
  const useList = () =>
    useQuery<TItem[]>({
      queryKey: [key],
      queryFn: () => http<TItem[]>(path),
    });

  const useCreate = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (body: TInput) =>
        http<TItem>(path, { method: "POST", body: JSON.stringify(body) }),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  const useUpdate = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: ({ id, body }: { id: number; body: TInput }) =>
        http<TItem>(`${path}/${id}`, {
          method: "PUT",
          body: JSON.stringify(body),
        }),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  const useDelete = (options?: UseMutationOptions<void, Error, number>) => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (id: number) =>
        http<void>(`${path}/${id}`, { method: "DELETE" }),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
      ...options,
    });
  };

  const useDeleteAll = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: () => http<void>(path, { method: "DELETE" }),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  return { useList, useCreate, useUpdate, useDelete, useDeleteAll };
}

/* ------------------------------------------------------------------ */
/* Resources                                                           */
/* ------------------------------------------------------------------ */

export const Rooms = makeResource<Room, RoomInput>("rooms", "/rooms");
export const Instructors = makeResource<Instructor, InstructorInput>(
  "instructors",
  "/instructors",
);
export const Courses = makeResource<Course, CourseInput>("courses", "/courses");
export const Lectures = makeResource<Lecture, LectureInput>(
  "lectures",
  "/lectures",
);
export const TimeSlotsResource = makeResource<TimeSlot, TimeSlotInput>(
  "time-slots",
  "/time-slots",
);
// POST /time-slots returns a list (one slot per day) - wrap useCreate so it still invalidates
export const TimeSlots = {
  useList: TimeSlotsResource.useList,
  useDelete: TimeSlotsResource.useDelete,
  useDeleteAll: TimeSlotsResource.useDeleteAll,
  useCreate: () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (body: TimeSlotInput) =>
        http<TimeSlot[]>("/time-slots", {
          method: "POST",
          body: JSON.stringify(body),
        }),
      onSuccess: () => qc.invalidateQueries({ queryKey: ["time-slots"] }),
    });
  },
};

export const TimeTables = {
  useList: () =>
    useQuery<TimeTable[]>({
      queryKey: ["time-table"],
      queryFn: () => http<TimeTable[]>("/time-table"),
    }),
  useDelete: () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (id: number) =>
        http<void>(`/time-table/${id}`, { method: "DELETE" }),
      onSuccess: () => qc.invalidateQueries({ queryKey: ["time-table"] }),
    });
  },
};

export const useConflicts = () =>
  useQuery<ConflictResponse[]>({
    queryKey: ["conflicts"],
    queryFn: () => http<ConflictResponse[]>("/conflicts"),
  });

/* Manual entries (course + instructor pairs) */
export const ManualEntries = {
  useList: () =>
    useQuery<ManualEntry[]>({
      queryKey: ["manual-entry"],
      queryFn: () => http<ManualEntry[]>("/manual-entry"),
    }),
  useCreate: () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (body: ManualEntry) =>
        http<ManualEntry>("/manual-entry", {
          method: "POST",
          body: JSON.stringify(body),
        }),
      onSuccess: () => qc.invalidateQueries({ queryKey: ["manual-entry"] }),
    });
  },
  useDeleteAll: () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: () => http<void>("/manual-entry", { method: "DELETE" }),
      onSuccess: () => qc.invalidateQueries({ queryKey: ["manual-entry"] }),
    });
  },
};

/* Run the GA / generator – backend exposes POST /manual-entry-generator */
export const useGenerateTimetable = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () =>
      http<TimeTable>("/manual-entry-generator", { method: "POST" }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["time-table"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
};

/* Excel import */
export const useImportExcel = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (file: File) => {
      const form = new FormData();
      form.append("file", file);
      const res = await fetch(`${API_BASE}/import/excel`, {
        method: "POST",
        body: form,
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`${res.status} ${t || res.statusText}`);
      }
      return (await res.json()) as ImportSummary;
    },
    onSuccess: () => {
      qc.invalidateQueries();
    },
  });
};

/* Export URLs (download links) */
export const exportScheduleUrl = () => `${API_BASE}/export/schedule`;
export const exportLatestUrl = () => `${API_BASE}/export/schedule/latest`;
export const exportTimetableUrl = (id: number) =>
  `${API_BASE}/export/schedule/${id}`;