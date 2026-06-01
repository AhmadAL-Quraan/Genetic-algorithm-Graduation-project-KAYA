import { pgTable, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";
import { instructorsTable } from "./instructors";
import { timeslotsTable } from "./timeslots";

export const instructorAvailabilityTable = pgTable("instructor_availability", {
  id: serial("id").primaryKey(),
  instructorId: integer("instructor_id").notNull().references(() => instructorsTable.id, { onDelete: "cascade" }),
  timeslotId: integer("timeslot_id").notNull().references(() => timeslotsTable.id, { onDelete: "cascade" }),
  isUnavailable: boolean("is_unavailable").notNull().default(true),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertInstructorAvailabilitySchema = createInsertSchema(instructorAvailabilityTable).omit({ id: true, createdAt: true });
export type InsertInstructorAvailability = z.infer<typeof insertInstructorAvailabilitySchema>;
export type InstructorAvailability = typeof instructorAvailabilityTable.$inferSelect;
