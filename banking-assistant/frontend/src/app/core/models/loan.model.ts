export type LoanType = 'PERSONAL' | 'HOME' | 'AUTO';
export type RequestStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'DISBURSED' | 'ACTIVATED';

export interface LoanApplicationRequest {
  loanType: LoanType;
  principalAmount: number;
  tenureMonths: number;
  purpose?: string;
}

export interface EmiCalculationRequest {
  principalAmount: number;
  annualInterestRate: number;
  tenureMonths: number;
}

export interface EmiCalculationResponse {
  emi: number;
  totalPayment: number;
  totalInterest: number;
}

export interface LoanResponse {
  id: string;
  userId: string;
  loanType: string;
  principalAmount: number;
  interestRate: number;
  tenureMonths: number;
  emiAmount: number;
  status: RequestStatus;
  purpose?: string;
  createdAt: string;
  decisionNotes?: string;
}

export interface LoanDecisionRequest {
  status: RequestStatus;
  notes?: string;
}

export interface LoanEligibilityResult {
  eligible: boolean;
  reasons: string[];
  creditScore?: number;
}
