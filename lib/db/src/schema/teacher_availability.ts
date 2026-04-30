import { pgTable, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { teachersTable } from "./teachers";
import { timeslotsTable } from "./timeslots";

export const teacherAvailabilityTable = pgTable("teacher_availability", {
  id: serial("id").primaryKey(),
  teacherId: integer("teacher_id").notNull().references(() => teachersTable.id, { onDelete: "cascade" }),
  timeslotId: integer("timeslot_id").notNull().references(() => timeslotsTable.id, { onDelete: "cascade" }),
  isUnavailable: boolean("is_unavailable").notNull().default(true),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertTeacherAvailabilitySchema = createInsertSchema(teacherAvailabilityTable).omit({ id: true, createdAt: true });
export type InsertTeacherAvailability = z.infer<typeof insertTeacherAvailabilitySchema>;
export type TeacherAvailability = typeof teacherAvailabilityTable.$inferSelect;
