import { RequestStatus } from './loan.model';

export type ServiceRequestType = 'LOAN' | 'ACCOUNT_UPGRADE' | 'CARD' | 'OTHER';

export interface ServiceRequestCreate {
  requestType: ServiceRequestType;
  referenceId?: string;
  payload?: string;
}

export interface ServiceRequestResponse {
  id: string;
  userId: string;
  requestType: ServiceRequestType;
  referenceId?: string;
  status: RequestStatus;
  payload?: string;
  createdAt: string;
  decisionNotes?: string;
}

export interface ServiceRequestDecisionRequest {
  status: RequestStatus;
  notes?: string;
}

export interface ApprovalQueueResponse {
  pendingLoans: import('./loan.model').LoanResponse[];
  pendingServiceRequests: ServiceRequestResponse[];
  totalPending: number;
}
