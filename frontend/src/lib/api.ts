// TanStack Query hooks for fetching/caching/updating backend data
import { useMutation, useQuery, useQueryClient, type QueryKey } from "@tanstack/react-query";


// Example delete-all request flow:
// Frontend -> Controller -> Service -> Repository -> Database
// DELETE /api/teachers
//         ↓
// TeacherController.deleteAll()
//         ↓
// teacherService.deleteAll()
//         ↓
// teacherRepository.deleteAll()
//         ↓
// All teachers deleted


// Allowed room categories
export type RoomType = "LECTURE" | "LAB" | "OTHER";

// Allowed teaching methods
export type TeachingMethod = "BLENDED" | "IN_PERSON" | "ONLINE";

// Allowed weekdays
export type DayOfWeek =
    | "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";


// Department entity returned from backend
export interface Department {
  id: number;
  name: string;
  code: string;
}

// Department creation/update payload
export interface DepartmentInput {
  name: string;
  code: string;
}


// Teacher entity returned from backend
export interface Teacher {
  id: number;
  name: string;
  email?: string;
  department?: Department;
}

// Teacher creation/update payload
export interface TeacherInput {
  name: string;
  email?: string;
  departmentId?: number;
}


// Time slot entity returned from backend
export interface TimeSlot {
  id: number;
  startTime: string;
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number;
}

// Time slot creation/update payload
export interface TimeSlotInput {
  startTime: string;
  endTime: string;
  days: DayOfWeek[];
  teachingMethod: TeachingMethod;
  durationMinutes: number;
}


// Course entity returned from backend
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

// Course creation/update payload
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


// Room entity returned from backend
export interface Room {
  id: number;
  building: string;
  roomNumber: string;
  roomType: RoomType;
}

// Room creation/update payload
export interface RoomInput {
  building: string;
  roomNumber: string;
  roomType: RoomType;
}


// Lecture entity returned from backend
export interface Lecture {
  id: number;
  course: Course | null;
  teacher?: { id: number; name: string } | null;
  instructor?: string | null;
  timeSlot: TimeSlot | null;
  room: Room | null;
}

// Lecture creation/update payload
export interface LectureInput {
  courseId: number;
  teacherId?: number | null;
  timeSlotId?: number | null;
  roomId?: number | null;
}


// Conflict information produced by timetable generator
export interface ConflictItem {
  type: "ROOM" | "TEACHER" | "STUDENT";
  message: string;
  lectureAId: number;
  lectureBId: number;
  courseA: string;
  courseB: string;
  instructorA?: string;
  instructorB?: string;
  timeSlot: string;
}


// Timetable fitness statistics
export interface FitnessReport {
  roomConflicts: number;
  instructorConflicts: number;
  studentConflicts: number;
  totalPenalty: number;
}

// Generated timetable entity
export interface TimeTable {
  id: number;
  fitness: number;
  generatedAt: string | null;
  fitnessReport: FitnessReport | null;
  lectures: Lecture[];
}


// Base backend API path
const API_BASE = "/api";


// Generic reusable HTTP request helper
async function request<T>(
    method: string,
    path: string,
    body?: unknown
): Promise<T> {

  // Send HTTP request
  const res = await fetch(API_BASE + path, {
    method,

    // Add JSON headers if request contains body
    headers: body ? { "Content-Type": "application/json" } : undefined,

    // Convert JavaScript object -> JSON string
    body: body ? JSON.stringify(body) : undefined,
  });

  // Handle failed requests
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

  // Handle empty responses
  if (res.status === 204) return undefined as T;

  // Check if response is JSON
  const ct = res.headers.get("content-type") || "";

  if (!ct.includes("application/json")) {
    return undefined as T;
  }

  // Parse JSON response
  return res.json() as Promise<T>;
}


// Generic CRUD resource factory
function makeResource<T, I>(name: string) {

  // Query cache key
  const key: QueryKey = [name];

  return {

    // Fetch all entities
    useList: () =>
        useQuery({
          queryKey: key,
          queryFn: () => request<T[]>("GET", `/${name}`),
        }),

    // Create entity
    useCreate: () => {
      const qc = useQueryClient();

      return useMutation({
        mutationFn: (input: I) =>
            request<T>("POST", `/${name}`, input),

        // Refresh cache after success
        onSuccess: () =>
            qc.invalidateQueries({ queryKey: key }),
      });
    },

    // Update entity
    useUpdate: () => {
      const qc = useQueryClient();

      return useMutation({
        mutationFn: ({ id, input }: { id: number; input: I }) =>
            request<T>("PUT", `/${name}/${id}`, input),

        onSuccess: () =>
            qc.invalidateQueries({ queryKey: key }),
      });
    },

    // Delete entity by id
    useDelete: () => {
      const qc = useQueryClient();

      return useMutation({
        mutationFn: (id: number) =>
            request<void>("DELETE", `/${name}/${id}`),

        onSuccess: () =>
            qc.invalidateQueries({ queryKey: key }),
      });
    },
  };
}


// CRUD hooks for departments
export const Departments =
    makeResource<Department, DepartmentInput>("departments");

// CRUD hooks for teachers
export const Teachers =
    makeResource<Teacher, TeacherInput>("teachers");

// CRUD hooks for courses
export const Courses =
    makeResource<Course, CourseInput>("courses");

// CRUD hooks for rooms
export const Rooms =
    makeResource<Room, RoomInput>("rooms");

// CRUD hooks for time slots
export const TimeSlots =
    makeResource<TimeSlot, TimeSlotInput>("time-slots");

// CRUD hooks for lectures
export const Lectures =
    makeResource<Lecture, LectureInput>("lectures");

// CRUD hooks for generated timetables
export const TimeTables =
    makeResource<TimeTable, { lectureIds: number[]; reportId?: number }>("time-table");


// Delete all courses hook
export function useDeleteAllCourses() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/courses"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["courses"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
    },
  });
}


// Delete all rooms hook
export function useDeleteAllRooms() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/rooms"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["rooms"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}


// Delete all teachers hook
export function useDeleteAllTeachers() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/teachers"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["teachers"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}


// Delete all departments hook
export function useDeleteAllDepartments() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/departments"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["departments"] });
      qc.invalidateQueries({ queryKey: ["teachers"] });
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
  });
}


// Delete all time slots hook
export function useDeleteAllTimeSlots() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/time-slots"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["time-slots"] });
      qc.invalidateQueries({ queryKey: ["lectures"] });
    },
  });
}


// Delete all lectures hook
export function useDeleteAllLectures() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/lectures"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["lectures"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
    },
  });
}


// Duplicate delete-all-teachers hook (can probably be removed)
export function useDeleteAllInstructor() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: () =>
        request<void>("DELETE", "/teachers"),

    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["lectures"] });
      qc.invalidateQueries({ queryKey: ["conflicts"] });
    },
  });
}


// Fetch timetable conflicts
export function useConflicts() {
  return useQuery<ConflictItem[]>({
    queryKey: ["conflicts"],

    queryFn: () =>
        request<ConflictItem[]>("GET", "/conflicts"),
  });
}


// Genetic Algorithm configuration
export interface GAConfig {
  maxGenerations?: number;
  populationSize?: number;
  elitismCount?: number;
  tournamentSize?: number;
  initialMutationRate?: number;
  mutationImpactRatio?: number;
}


// Generate timetable using backend GA algorithm
export function useGenerateTimetable() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (cfg: GAConfig) =>
        request<TimeTable>("POST", `/manual-entry-generator`, cfg),

    onSuccess: () =>
        qc.invalidateQueries({ queryKey: ["time-table"] }),
  });
}


// Export latest schedule URL
export function exportScheduleUrl() {
  return `${API_BASE}/export/schedule`;
}


// Export specific timetable by id URL
export function exportTimetableUrl(id: number) {
  return `${API_BASE}/export/schedule/${id}`;
}


// Find matching time slot or create new one
export async function findOrCreateTimeSlot(
    input: TimeSlotInput
): Promise<TimeSlot> {

  // Fetch all existing time slots
  const all = await request<TimeSlot[]>("GET", "/time-slots");

  // Normalize time format
  const norm = (s: string) =>
      s.length === 5 ? `${s}:00` : s;

  // Normalize target values
  const want = {
    start: norm(input.startTime),
    end: norm(input.endTime),
    days: [...input.days].sort().join(","),
    method: input.teachingMethod,
  };

  // Search for matching slot
  const existing = all.find(t =>
      norm(t.startTime) === want.start &&
      norm(t.endTime) === want.end &&
      [...t.days].sort().join(",") === want.days &&
      t.teachingMethod === want.method
  );

  // Return existing slot if found
  if (existing) return existing;

  // Otherwise create new slot
  return request<TimeSlot>("POST", "/time-slots", input);
}


// Update lecture room/time assignment
export async function updateLectureAssignment(
    lecture: Lecture,
    patch: {
      timeSlotId?: number | null;
      roomId?: number | null;
    }
): Promise<Lecture> {

  // Build lecture update payload
  const input: LectureInput = {
    courseId: lecture.course?.id ?? 0,
    teacherId: lecture.teacher?.id ?? null,

    // Use patched values if provided
    timeSlotId:
        patch.timeSlotId !== undefined
            ? patch.timeSlotId
            : lecture.timeSlot?.id ?? null,

    roomId:
        patch.roomId !== undefined
            ? patch.roomId
            : lecture.room?.id ?? null,
  };

  // Send update request
  return request<Lecture>(
      "PUT",
      `/lectures/${lecture.id}`,
      input
  );
}


// Allowed room type values
export const ROOM_TYPES: RoomType[] = [
  "LECTURE",
  "LAB",
  "OTHER",
];


// Allowed teaching method values
export const TEACHING_METHODS: TeachingMethod[] = [
  "BLENDED",
  "IN_PERSON",
  "ONLINE",
];


// Allowed weekday values
export const DAYS_OF_WEEK: DayOfWeek[] = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];


// Predefined lecture duration options
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