import { useMutation, useQuery, useQueryClient, type QueryKey } from "@tanstack/react-query";

export type RoomType = "LECTURE" | "LAB" | "OTHER";
export type TeachingMethod = "BLENDED" | "IN_PERSON" | "ONLINE";
export type DayOfWeek =
  | "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export interface Department { id: number; name: string; code: string; }
export interface DepartmentInput { name: string; code: string; }

export interface Teacher { id: number; name: string; email?: string; department?: Department; }
export interface TeacherInput { name: string; email?: string; departmentId?: number; }

export interface Course {
  id: number;
  courseSymbol: string;
  courseNumber: string;
  majors: string[];
  roomGroups: RoomType;
  timeGroups: TeachingMethod;
  department?: Department;
  lectureId?: number;
  teacher?: Teacher;
  instructor?: string;
  sectionNumber?: number;
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
  teacherId?: number;
  instructor?: string;
  sectionNumber?: number;
  roomId?: number;
  building?: string;
  roomNumber?: string;
  roomType?: RoomType;
  timeSlotId?: number;
  startTime?: string;
  endTime?: string;
  days?: DayOfWeek[];
}

export interface Room { id: number; building: string; roomNumber: string; roomType: RoomType; }
export interface RoomInput { building: string; roomNumber: string; roomType: RoomType; }

export interface TimeSlot {
  id: number; startTime: string; endTime: string; days: DayOfWeek[]; teachingMethod: TeachingMethod;
}
export interface TimeSlotInput {
  startTime: string; endTime: string; days: DayOfWeek[]; teachingMethod: TeachingMethod;
}

export interface Lecture {
  id: number;
  course: Course | null;
  number: number;
  instructor: string;
  teacher?: Teacher;
  timeSlot: TimeSlot | null;
  room: Room | null;
}
export interface LectureInput {
  courseId: number; instructor: string; number?: number;
  timeSlotId?: number | null; roomId?: number | null;
}

export interface ConflictItem {
  type: "ROOM" | "TEACHER" | "STUDENT";
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
    throw new Error(`${method} ${path} failed (${res.status}): ${text || res.statusText}`);
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
export const Teachers    = makeResource<Teacher, TeacherInput>("teachers");
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

export function useDeleteAllTeachers() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => request<void>("DELETE", "/teachers"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["teachers"] });
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
      qc.invalidateQueries({ queryKey: ["teachers"] });
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

export function useConflicts() {
  return useQuery<ConflictItem[]>({
    queryKey: ["conflicts"],
    queryFn: () => request<ConflictItem[]>("GET", "/conflicts"),
  });
}

export interface GAConfig {
  maxGenerations?: number; populationSize?: number; elitismCount?: number;
  tournamentSize?: number; initialMutationRate?: number; mutationImpactRatio?: number;
}

export function useGenerateTimetable() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cfg: GAConfig) => request<TimeTable>("POST", `/time-table/generate`, cfg),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["time-table"] }),
  });
}

export function exportScheduleUrl() { return `${API_BASE}/export/schedule`; }
export function exportTimetableUrl(id: number) { return `${API_BASE}/export/schedule/${id}`; }

export async function findOrCreateTimeSlot(input: TimeSlotInput): Promise<TimeSlot> {
  const all = await request<TimeSlot[]>("GET", "/time-slots");
  const norm = (s: string) => s.length === 5 ? `${s}:00` : s;
  const want = { start: norm(input.startTime), end: norm(input.endTime),
    days: [...input.days].sort().join(","), method: input.teachingMethod };
  const existing = all.find(t =>
    norm(t.startTime) === want.start && norm(t.endTime) === want.end &&
    [...t.days].sort().join(",") === want.days && t.teachingMethod === want.method);
  if (existing) return existing;
  return request<TimeSlot>("POST", "/time-slots", input);
}

export async function updateLectureAssignment(
  lecture: Lecture, patch: { timeSlotId?: number | null; roomId?: number | null }
): Promise<Lecture> {
  const input: LectureInput = {
    courseId: lecture.course?.id ?? 0, instructor: lecture.instructor, number: lecture.number,
    timeSlotId: patch.timeSlotId !== undefined ? patch.timeSlotId : lecture.timeSlot?.id ?? null,
    roomId:    patch.roomId    !== undefined ? patch.roomId    : lecture.room?.id ?? null,
  };
  return request<Lecture>("PUT", `/lectures/${lecture.id}`, input);
}

export const ROOM_TYPES: RoomType[] = ["LECTURE", "LAB", "OTHER"];
export const TEACHING_METHODS: TeachingMethod[] = ["BLENDED", "IN_PERSON", "ONLINE"];
export const DAYS_OF_WEEK: DayOfWeek[] = [
  "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
];
