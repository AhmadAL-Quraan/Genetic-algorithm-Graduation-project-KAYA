import { pgTable, text, serial, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const timeslotsTable = pgTable("timeslots", {
  id: serial("id").primaryKey(),
  day: text("day").notNull(),
  startTime: text("start_time").notNull(),
  endTime: text("end_time").notNull(),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertTimeslotSchema = createInsertSchema(timeslotsTable).omit({ id: true, createdAt: true });
export type InsertTimeslot = z.infer<typeof insertTimeslotSchema>;
export type Timeslot = typeof timeslotsTable.$inferSelect;
