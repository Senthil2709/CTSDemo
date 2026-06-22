import { Component, OnInit } from '@angular/core';
import { LoanService } from '../../../core/services/loan.service';
import { LoanResponse } from '../../../core/models/loan.model';

@Component({
  selector: 'app-loan-list',
  templateUrl: './loan-list.component.html'
})
export class LoanListComponent implements OnInit {
  loans: LoanResponse[] = [];
  loading = true;

  constructor(private loanService: LoanService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.loanService.myLoans().subscribe({
      next: (res) => { this.loans = res; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
