import { pgTable, text, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { teachersTable } from "./teachers";
import { departmentsTable } from "./departments";

export const coursesTable = pgTable("courses", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  code: text("code").notNull(),
  teacherId: integer("teacher_id").references(() => teachersTable.id),
  departmentId: integer("department_id").references(() => departmentsTable.id),
  studentCount: integer("student_count").notNull().default(30),
  roomType: text("room_type").notNull().default("lecture"),
  studentGroups: text("student_groups").array().notNull().default([]),
  isShared: boolean("is_shared").notNull().default(false),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertCourseSchema = createInsertSchema(coursesTable).omit({ id: true, createdAt: true });
export type InsertCourse = z.infer<typeof insertCourseSchema>;
export type Course = typeof coursesTable.$inferSelect;
