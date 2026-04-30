# Workspace

## Overview

KAYA — a university timetable scheduler powered by a Java/Spring Boot genetic-algorithm backend with a React + Vite frontend. pnpm workspace monorepo.

## Stack

- **Monorepo tool**: pnpm workspaces
- **Backend**: Java 17/19, Spring Boot 3.2.5, Spring Data JPA, Hibernate, PostgreSQL, Maven
- **Genetic algorithm**: KAYA engine (uniform crossover, adaptive mutation, elitism, tournament selection)
- **Frontend**: React 19, Vite, TypeScript, TanStack Query, wouter, shadcn/ui, Tailwind
- **Database**: PostgreSQL (accessed via JDBC)
- Dev Environment: Docker



## Backend (`@workspace/api-server`)

Spring Boot 3 application that exposes REST endpoints and runs the genetic algorithm.

- Java entrypoint: `com.kaya.KayaApplication`
- Reads `PORT` (default 8080) and uses `PGHOST`/`PGPORT`/`PGDATABASE`/`PGUSER`/`PGPASSWORD`
- Hibernate `ddl-auto=update` auto-creates/upgrades tables
- CORS allows all origins (development)
- Dev: `pnpm --filter @workspace/api-server run dev` → `mvn -q spring-boot:run`

### REST endpoints

**Setup & Data**
- `GET/POST/PUT/DELETE /api/rooms`          — `{ building, roomNumber, roomType }`
- `GET/POST/PUT/DELETE /api/teachers`       — `{ name, email?, departmentId? }`
- `GET/POST/PUT/DELETE /api/departments`    — `{ name, code }`
- `GET/POST/PUT/DELETE /api/time-slots`     — `{ startTime, endTime, days[], teachingMethod }`

**Overview**
- `GET/POST/DELETE /api/courses`            — merged form: creates Room/TimeSlot/Lecture if IDs not provided
- `GET/POST/PUT/DELETE /api/lectures`
- `GET/POST/DELETE /api/time-table`
- `POST /api/time-table/generate`           — runs the GA; optional GAConfig body
- `GET /api/conflicts`                      — room, teacher, student-group conflict detection
- `GET /api/export/schedule`                — xlsx export of all scheduled lectures
- `GET /api/export/schedule/{timetableId}`  — xlsx export of a specific timetable
- `GET /api/healthz`

### Course form (POST /api/courses)

Supports two modes:
1. **ID-based**: provide `teacherId`, `roomId`, `timeSlotId` → resolves from existing setup entities
2. **String-based** (legacy): provide `instructor`, `building`+`roomNumber`, `startTime`+`endTime`+`days[]` → auto-creates missing Room/TimeSlot

## Frontend (`@workspace/university-scheduler`)

React + Vite SPA. All API calls proxy to `/api/...`.

### Navigation

**SETUP & DATA**
- `/rooms` — manage lecture halls, labs, other spaces
- `/teachers` — manage doctors/instructors (linked to departments)
- `/departments` — manage academic departments
- `/time-slots` — manage weekly time slots

**OVERVIEW**
- `/` — Dashboard: stats for all entities, conflict badge, export button
- `/courses` — Courses: use dropdowns for teacher, room, timeslot, department
- `/schedule` — Timetables: GA config + generate + calendar/list view
- `/conflicts` — Conflict browser: room, teacher, student-group conflicts with detail cards

### Key frontend files

- `src/lib/api.ts` — all types, makeResource(), Departments, Teachers, Courses, Rooms, TimeSlots, Lectures, TimeTables, useConflicts(), exportScheduleUrl()
- `src/components/layout.tsx` — sidebar with SETUP & DATA / OVERVIEW sections, live conflict badge
- `src/App.tsx` — all routes



# Run the project

### 1. Requirements 

* `pnpm-lock.yaml`
* `package-lock.json`)

- Docker
- Docker Compose

### 2. Clone the repo

```bash
git clone https://github.com/your-username/university-scheduler.git
cd university-scheduler
```

### 3. Run everything

```bash
docker compose up --build
```

### 4. Open in browser

* Frontend: http://localhost:5000
* Backend: http://localhost:8080


---

##  Notes

* First run may take time (dependencies download)
* Make sure ports 5000, 8080, 5432 are free
## How to use

1. Set up **Rooms**, **Teachers**, **Departments**, and **Time Slots** (Setup & Data section).
2. Create **Courses** — choose teacher, room, and time slot from dropdowns.
3. Run the genetic algorithm on the **Schedule** page for an optimized timetable.
4. Check **Conflicts** to review any room/teacher/student-group overlaps.
5. Export to Excel from Courses page or Dashboard.
