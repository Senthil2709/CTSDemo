import { Component, OnInit } from '@angular/core';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { LoanService } from '../../../core/services/loan.service';
import { LoanResponse } from '../../../core/models/loan.model';
import { ServiceRequestResponse } from '../../../core/models/service-request.model';

@Component({
  selector: 'app-approval-queue',
  templateUrl: './approval-queue.component.html'
})
export class ApprovalQueueComponent implements OnInit {
  loading = true;
  pendingLoans: LoanResponse[] = [];
  pendingRequests: ServiceRequestResponse[] = [];
  busyId: string | null = null;
  notesByLoan: Record<string, string> = {};
  notesByRequest: Record<string, string> = {};

  constructor(private serviceRequestService: ServiceRequestService, private loanService: LoanService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.serviceRequestService.approvalQueue().subscribe({
      next: (res) => {
        this.pendingLoans = res.pendingLoans;
        this.pendingRequests = res.pendingServiceRequests;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  decideLoan(loan: LoanResponse, status: 'APPROVED' | 'REJECTED'): void {
    this.busyId = loan.id;
    this.loanService.decide(loan.id, { status, notes: this.notesByLoan[loan.id] || '' }).subscribe({
      next: () => { this.busyId = null; this.refresh(); },
      error: () => { this.busyId = null; }
    });
  }

  decideRequest(request: ServiceRequestResponse, status: 'APPROVED' | 'REJECTED'): void {
    this.busyId = request.id;
    this.serviceRequestService.decide(request.id, { status, notes: this.notesByRequest[request.id] || '' }).subscribe({
      next: () => { this.busyId = null; this.refresh(); },
      error: () => { this.busyId = null; }
    });
  }
}
