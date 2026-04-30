import { Switch, Route, Router as WouterRouter } from "wouter";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";

import { AppLayout } from "@/components/layout";
import Dashboard from "@/pages/dashboard";
import CoursesPage from "@/pages/courses";
import RoomsPage from "@/pages/rooms";
import TeachersPage from "@/pages/teachers";
import DepartmentsPage from "@/pages/departments";
import TimeSlotsPage from "@/pages/time-slots";
import SchedulePage from "@/pages/timetables";
import ConflictsPage from "@/pages/conflicts";
import NotFound from "@/pages/not-found";

const queryClient = new QueryClient({
  defaultOptions: { queries: { refetchOnWindowFocus: false, retry: 1 } },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <WouterRouter base={import.meta.env.BASE_URL.replace(/\/$/, "")}>
          <AppLayout>
            <Switch>
              <Route path="/" component={Dashboard} />
              <Route path="/courses" component={CoursesPage} />
              <Route path="/rooms" component={RoomsPage} />
              <Route path="/teachers" component={TeachersPage} />
              <Route path="/departments" component={DepartmentsPage} />
              <Route path="/time-slots" component={TimeSlotsPage} />
              <Route path="/schedule" component={SchedulePage} />
              <Route path="/timetable">{() => { window.location.replace(window.location.pathname.replace("/timetable", "/schedule")); return null; }}</Route>
              <Route path="/conflicts" component={ConflictsPage} />
              <Route component={NotFound} />
            </Switch>
          </AppLayout>
        </WouterRouter>
        <Toaster />
      </TooltipProvider>
    </QueryClientProvider>
  );
}

export default App;
