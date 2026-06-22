export interface AccountResponse {
  id: string;
  accountNumber: string;
  accountType: string;
  balance: number;
  currency: string;
  status: string;
}

export interface TransactionResponse {
  type: 'DEBIT' | 'CREDIT';
  category?: string;
  amount: number;
  description?: string;
  balanceAfter: number;
  transactedAt: string;
}

export interface SpendingSummaryResponse {
  totalCredits: number;
  totalDebits: number;
  spendingByCategory: Record<string, number>;
  periodDays: number;
}

export interface UpgradeEligibilityResponse {
  currentTier: string;
  recommendedTier: string;
  eligibleForUpgrade: boolean;
  reasons: string[];
}
