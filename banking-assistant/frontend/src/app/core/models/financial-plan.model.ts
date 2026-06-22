export interface FinancialPlanRequest {
  monthlyIncome: number;
  monthlyExpenses: number;
  existingMonthlyEmi?: number;
  goal: string;
  timelineMonths: number;
  riskAppetite: 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
  targetAmount?: number;
}

export interface MonthlyScheduleItem {
  monthNumber: number;
  plannedSavings: number;
  cumulativeSavings: number;
  projectedCorpus: number;
}

export interface FinancialPlanResponse {
  recommendedMonthlySavings: number;
  monthlySurplus: number;
  suggestedInvestmentProducts: string[];
  loanOptionsIfApplicable: string[];
  monthlySchedule: MonthlyScheduleItem[];
  estimatedGoalAchievementMonths?: number;
  goalAchievableInTimeline: boolean;
  regulatoryAndTaxNotes: string[];
  narrativeSummary: string;
}
