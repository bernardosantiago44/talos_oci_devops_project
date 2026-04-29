import { TrendingUp, BarChart2 } from 'lucide-react';
import { DeveloperAnalyticsCard } from '../components/dashboard/developer-analytics-card';
import { VelocityFulfillmentCard } from '../components/dashboard/velocity-fulfillment-card';

export function AnalyticsPage() {
  return (
    <div className="space-y-8">

      {/* Section: Velocity */}
      <div>
        <div className="mb-3 flex items-center gap-2">
          <TrendingUp className="h-4 w-4 text-sky-500" />
          <h2 className="text-sm font-semibold text-zinc-600 dark:text-zinc-300 uppercase tracking-wider">Velocity Fulfillment</h2>
        </div>
        <VelocityFulfillmentCard />
      </div>

      {/* Divider */}
      <div className="border-t border-zinc-200 dark:border-zinc-800" />

      {/* Section: Developer Performance */}
      <div>
        <div className="mb-3 flex items-center gap-2">
          <BarChart2 className="h-4 w-4 text-violet-500" />
          <h2 className="text-sm font-semibold text-zinc-600 dark:text-zinc-300 uppercase tracking-wider">Developer Performance</h2>
        </div>
        <DeveloperAnalyticsCard />
      </div>

    </div>
  );
}
