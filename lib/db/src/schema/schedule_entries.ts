import { pgTable, serial, integer, text, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { coursesTable } from "./courses";
import { roomsTable } from "./rooms";
import { timeslotsTable } from "./timeslots";
import { teachersTable } from "./teachers";

export const scheduleEntriesTable = pgTable("schedule_entries", {
  id: serial("id").primaryKey(),
  courseId: integer("course_id").notNull().references(() => coursesTable.id, { onDelete: "cascade" }),
  roomId: integer("room_id").notNull().references(() => roomsTable.id),
  timeslotId: integer("timeslot_id").notNull().references(() => timeslotsTable.id),
  teacherId: integer("teacher_id").references(() => teachersTable.id),
  status: text("status").notNull().default("draft"),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertScheduleEntrySchema = createInsertSchema(scheduleEntriesTable).omit({ id: true, createdAt: true });
export type InsertScheduleEntry = z.infer<typeof insertScheduleEntrySchema>;
export type ScheduleEntry = typeof scheduleEntriesTable.$inferSelect;
