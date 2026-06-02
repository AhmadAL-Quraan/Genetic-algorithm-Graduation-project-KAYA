import { useMutation, useQuery, useQueryClient, type QueryKey } from "@tanstack/react-query";

export type RoomType = "LECTURE" | "LAB" | "OTHER";
export type TeachingMethod = "BLENDED" | "IN_PERSON" | "ONLINE";
export type DayOfWeek =
  | "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export interface Department { id: number; name: string; code: string; }
export interface DepartmentInput { name: string; code: string; }

export interface Instructor { id: number; name: string; email?: string; department?: Department; }
export interface InstructorInput { name: string; email?: string; departmentId?: number; }

export interface TimeSlot {
  id: number;
  startTime: string;
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number;
}
export interface TimeSlotInput {
  startTime: string;
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number;
}

export interface Course {
  id: number;
  courseSymbol: string;
  courseNumber: string;
  majors: string[];
  roomGroups: RoomType;
  timeGroups: TeachingMethod;
  department?: Department;
  room?: Room;
  timeSlot?: TimeSlot;
}
export interface CourseInput {
  courseSymbol: string;
  courseNumber: string;
  majors: string[];
  roomGroups: RoomType;
  timeGroups: TeachingMethod;
  departmentId?: number;
  roomId?: number;
  timeSlotId?: number;
}

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

export interface Lecture {
  id: number;
  course: Course | null;
  instructor?: { id: number; name: string } | null;
  timeSlot: TimeSlot | null;
  room: Room | null;
}
export interface LectureInput {
  courseId: number;
  instructorId?: number | null;
  timeSlotId?: number | null;
  roomId?: number | null;
}

export interface ConflictItem {
  type: "ROOM" | "INSTRUCTOR" | "STUDENT";
  message: string;
  lectureAId: number; lectureBId: number;
  courseA: string; courseB: string;
  instructorA?: string; instructorB?: string;
  timeSlot: string;
}

export interface FitnessReport {
  roomConflicts: number; instructorConflicts: number; studentConflicts: number; totalPenalty: number;
}
export interface TimeTable {
  id: number; fitness: number; generatedAt: string | null;
  fitnessReport: FitnessReport | null; lectures: Lecture[];
}

const API_BASE = "/api";

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await fetch(API_BASE + path, {
    method,
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    let message = res.statusText;
    try {
      const json = JSON.parse(text);
      message = json.message || json.error || text;
    } catch {
      message = text || res.statusText;
    }
    throw new Error(message);
  }
  if (res.status === 204) return undefined as T;
  const ct = res.headers.get("content-type") || "";
  if (!ct.includes("application/json")) return undefined as T;
  return res.json() as Promise<T>;
}

function makeResource<T, I>(name: string) {
  const key: QueryKey = [name];
  return {
    useList: () => useQuery({ queryKey: key, queryFn: () => request<T[]>("GET", `/${name}`) }),
    useCreate: () => {
      const qc = useQueryClient();
      return useMutation({
        mutationFn: (input: I) => request<T>("POST", `/${name}`, input),
        onSuccess: () => qc.invalidateQueries({ queryKey: key }),
      });
    },
    useUpdate: () => {
      const qc = useQueryClient();
      return useMutation({
        mutationFn: ({ id, input }: { id: number; input: I }) =>
          request<T>("PUT", `/${name}/${id}`, input),
        onSuccess: () => qc.invalidateQueries({ queryKey: key }),
      });
    },
    useDelete: () => {
      const qc = useQueryClient();
      return useMutation({
        mutationFn: (id: number) => request<void>("DELETE", `/${name}/${id}`),
        onSuccess: () => qc.invalidateQueries({ queryKey: key }),
      });
    },
  };
}

export const Departments = makeResource<Department, DepartmentInput>("departments");
export const Instructors    = makeResource<Instructor, InstructorInput>("instructors");
export const Courses     = makeResource<Course, CourseInput>("courses");
export const Rooms       = makeResource<Room, RoomInput>("rooms");
export const TimeSlots   = makeResource<TimeSlot, TimeSlotInput>("time-slots");
export const Lectures    = makeResource<Lecture, LectureInput>("lectures");
export const TimeTables  = makeResource<TimeTable, { lectureIds: number[]; reportId?: number }>("time-table");

export function useDeleteAllCourses() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/courses"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["courses"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
    },
  });
}

export function useDeleteAllRooms() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/rooms"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["rooms"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}

export function useDeleteAllInstructors() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/instructors"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["instructors"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}

export function useDeleteAllDepartments() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/departments"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["departments"] });
      qc.invalidateQueries({ queryKey: ["instructors"] });
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
  });
}

export function useDeleteAllTimeSlots() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/time-slots"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["time-slots"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}

export function useDeleteAllLectures() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/lectures"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["lectures"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
    },
  });
}

export function useConflicts() {
  return useQuery<ConflictItem[]>({
    queryKey: ["conflicts"],
    queryFn: () => request<ConflictItem[]>("GET", "/conflicts"),
  });
}

export interface GAConfig {
  maxGenerations?: number;
  populationSize?: number;
  elitismRatio?: number;
  tournamentSize?: number;
  initialMutationRate?: number;
  mutationImpactRatio?: number;
  stagnationToleranceRatio?: number;
  numIslands?: number;
  migrationInterval?: number;
  migrationRate?: number;
  useIslandModel?: boolean;
}

export function useGenerateTimetable() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (cfg: GAConfig) =>
        request<TimeTableResponse>("POST", "/generator/generate", cfg),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["time-table"] }),
  });
}

export function exportScheduleUrl() { return `${API_BASE}/export/schedule`; }
export function exportTimetableUrl(id: number) { return `${API_BASE}/export/schedule/${id}`; }

export async function findOrCreateTimeSlot(input: TimeSlotInput): Promise<TimeSlot> {
  const all = await request<TimeSlot[]>("GET", "/time-slots");
  const norm = (s: string) => s.length === 5 ? `${s}:00` : s;
  const want = {
    start: norm(input.startTime), end: norm(input.endTime),
    days: [...input.days].sort().join(","), method: input.teachingMethod,
  };
  const existing = all.find(t =>
      norm(t.startTime) === want.start && norm(t.endTime) === want.end &&
      [...t.days].sort().join(",") === want.days && t.teachingMethod === want.method);

  if (existing) return existing;

  const createdSlots = await request<TimeSlot[]>("POST", "/time-slots", input);
  return createdSlots[0];
}

export async function updateLectureAssignment(
  lecture: Lecture, patch: { timeSlotId?: number | null; roomId?: number | null }
): Promise<Lecture> {
  const input: LectureInput = {
    courseId: lecture.course?.id ?? 0,
    instructorId: lecture.instructor?.id ?? null,
    timeSlotId: patch.timeSlotId !== undefined ? patch.timeSlotId : lecture.timeSlot?.id ?? null,
    roomId:    patch.roomId    !== undefined ? patch.roomId    : lecture.room?.id ?? null,
  };
  return request<Lecture>("PUT", `/lectures/${lecture.id}`, input);
}

export const ROOM_TYPES: RoomType[] = ["LECTURE", "LAB", "OTHER"];
export const TEACHING_METHODS: TeachingMethod[] = ["BLENDED", "IN_PERSON", "ONLINE"];
export const DAYS_OF_WEEK: DayOfWeek[] = [
  "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY",
];

export const DURATION_OPTIONS = [
  { value: 50,  label: "50 min" },
  { value: 60,  label: "60 min (1 hr)" },
  { value: 75,  label: "75 min" },
  { value: 90,  label: "90 min (1.5 hr)" },
  { value: 100, label: "100 min" },
  { value: 120, label: "120 min (2 hr)" },
  { value: 150, label: "150 min (2.5 hr)" },
  { value: 180, label: "180 min (3 hr)" },
];
