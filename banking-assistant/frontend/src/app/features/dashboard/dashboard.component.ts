import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AccountService } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';
import { ServiceRequestService } from '../../core/services/service-request.service';
import { AccountResponse, SpendingSummaryResponse, TransactionResponse, UpgradeEligibilityResponse } from '../../core/models/account.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  loading = true;
  accounts: AccountResponse[] = [];
  spending: SpendingSummaryResponse | null = null;
  upgrade: UpgradeEligibilityResponse | null = null;
  recommendation = '';
  recentTransactions: TransactionResponse[] = [];
  selectedAccountId = '';
  upgradeRequested = false;
  requestingUpgrade = false;

  constructor(
    private accountService: AccountService,
    public authService: AuthService,
    private serviceRequestService: ServiceRequestService
  ) {}

  ngOnInit(): void {
    forkJoin({
      accounts: this.accountService.myAccounts(),
      spending: this.accountService.spendingSummary(30),
      upgrade: this.accountService.upgradeEligibility(),
      recommendation: this.accountService.recommendAccountType()
    }).subscribe({
      next: (res) => {
        this.accounts = res.accounts;
        this.spending = res.spending;
        this.upgrade = res.upgrade;
        this.recommendation = res.recommendation;
        this.loading = false;
        if (this.accounts.length) {
          this.viewTransactions(this.accounts[0].id);
        }
      },
      error: () => { this.loading = false; }
    });
  }

  get totalBalance(): number {
    return this.accounts.reduce((sum, a) => sum + a.balance, 0);
  }

  get spendingCategories(): { category: string; amount: number; pct: number }[] {
    if (!this.spending) return [];
    const entries = Object.entries(this.spending.spendingByCategory || {});
    const max = Math.max(...entries.map(([, v]) => v), 1);
    return entries
      .sort((a, b) => b[1] - a[1])
      .map(([category, amount]) => ({ category, amount, pct: Math.round((amount / max) * 100) }));
  }

  viewTransactions(accountId: string): void {
    this.selectedAccountId = accountId;
    this.accountService.transactions(accountId).subscribe(txns => {
      this.recentTransactions = txns.slice(0, 8);
    });
  }

  requestUpgrade(): void {
    if (!this.upgrade) return;
    this.requestingUpgrade = true;
    this.serviceRequestService.create({
      requestType: 'ACCOUNT_UPGRADE',
      payload: JSON.stringify({ recommendedTier: this.upgrade.recommendedTier })
    }).subscribe({
      next: () => { this.requestingUpgrade = false; this.upgradeRequested = true; },
      error: () => { this.requestingUpgrade = false; }
    });
  }
}
