import { jsx as _jsx } from "react/jsx-runtime";
import { ThemeProvider } from '@/contexts/theme-context';
import { WorkItemDashboardPage } from '@/features/work-items/pages/work-item-dashboard-page';
function App() {
    return (_jsx(ThemeProvider, { children: _jsx(WorkItemDashboardPage, {}) }));
}
export default App;
