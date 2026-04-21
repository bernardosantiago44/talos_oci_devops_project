import { ThemeProvider } from '@/contexts/theme-context';
import { WorkItemDashboardPage } from '@/features/work-items/pages/work-item-dashboard-page';

function App() {
  return (
    <ThemeProvider>
      <WorkItemDashboardPage />
    </ThemeProvider>
  );
}

export default App;