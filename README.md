# KAYA — University Course Timetable System for Yarmouk University

## Run & Operate

| Command | Purpose |
|---|---|
| `pnpm --filter @workspace/api-server run dev` | Start Spring Boot backend (port 8080) |
| `pnpm --filter @workspace/university-scheduler run dev` | Start React frontend (port 5000+) |
| `cd backend && mvn compile` | Compile backend only |
| `cd backend && mvn spring-boot:run` | Run backend directly |

**Required env vars**: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `PORT`, `BASE_PATH`

## Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA, Hibernate, PostgreSQL, Maven
- **Genetic algorithm**: KAYA engine — uniform crossover, adaptive mutation, elitism, tournament selection
- **Frontend**: React 19, Vite, TypeScript, TanStack Query, wouter, shadcn/ui, Tailwind
- **Database**: PostgreSQL (JDBC via env vars)
- **Monorepo**: pnpm workspaces

## Where things live

```
backend/src/main/java/com/kaya/
├── KayaApplication.java
├── algorithm/          # GA: EvolutionEngine, GeneticOperators, FitnessCalculator,
│                       #     GAConfig, Selection, PoolHelper, TimeTableInitializer,
│                       #     Island, IslandManager, EvolutionEngineIsland
│                       #     run/StartPoint.java  ← entry to GA run
├── config/             # CorsConfig, JacksonConfig
├── controller/         # Course, Instructor, Room, TimeSlot, Lecture, TimeTable,
│                       #   FitnessReport, Teacher, Department, Conflict
├── dataManager/        # SectionGenerator, ManualEntry*, manualEntryGenerator*
├── dto/                # request/, response/, mapper/
├── model/              # Course, Instructor, Room, TimeSlot, Lecture, TimeTable,
│                       #   FitnessReport, Teacher, Department + enums/
├── repository/         # 10 JPA repos
└── service/            # one service per entity
backend/src/main/resources/application.properties   ← DB + port config
frontend/src/lib/api.ts                             ← all API types + hooks
frontend/src/pages/                                 ← one file per page
```

## Architecture decisions

- **Friends' code as base**: All core algorithm and entity classes match the team's shared code from `LATESET/` (extracted from JAR). Added `GAConfig.elitismRatio`, `stagnationToleranceRatio`, `mutationImpactRatio` defaults that were missing.
- **Two instructor models**: `Instructor` entity (friends' code, at `/api/instructors`) and `Teacher` entity (at `/api/teachers`) which is what the frontend uses — supports `name`, `email`, and `Department` FK.
- **Department**: Not in friends' code; added as standalone entity with CRUD at `/api/departments`.
- **Generate endpoint**: `POST /api/time-table/generate` accepts optional `GAConfig` map, runs algorithm synchronously, saves result, returns `TimeTableResponse`. Cancel via `POST /api/time-table/cancel`.
- **Conflicts**: `GET /api/conflicts` reads the latest saved timetable's FitnessReport and returns pairwise `ConflictItem` objects for the frontend conflict browser.
- **Section numbers**: assigned after GA run by `SectionGenerator.generate()` which sorts lectures by course then numbers sections sequentially.

## Product

- Set up Rooms, Teachers, Departments, Time Slots in the Setup & Data section
- Add Courses (linked to rooms, time slots, instructors via ManualEntry or direct)
- Run `POST /api/time-table/generate` to trigger the genetic algorithm
- View the generated timetable calendar and export to Excel
- Browse detected conflicts by type (room, instructor, student year group)

## User preferences

- Backend must use friends' code from the JAR (`LATESET/`) as the foundation
- Frontend behavior must remain identical to the original working system

## Gotchas

- `StartPoint.runFromDatabase` now takes `cancelCheck` and `progressCallback` params (both nullable)
- `manualEntryGeneratorService` calls `StartPoint.runFromDatabase(lectures, rooms, timeSlots, null, null)`
- `FitnessReport.conflictingLectures` is a `Set<Lecture>` mapped via `@OneToMany` — not a join table entity
- Spring Boot 3.2.5 requires `spring-boot-starter-web` (not `webmvc`); friends' pom.xml had 4.0.5 which was corrected
- Hibernate `ddl-auto=update` handles schema evolution automatically

## Pointers

- Friends' original source: `/tmp/jar_extract/LATESET/src/` (extracted from attached JAR)
- API types: `frontend/src/lib/api.ts`
- GA entry point: `backend/src/main/java/com/kaya/algorithm/run/StartPoint.java`
