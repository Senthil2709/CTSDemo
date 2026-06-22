import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AccountResponse, SpendingSummaryResponse, TransactionResponse, UpgradeEligibilityResponse } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private base = `${environment.apiBaseUrl}/accounts`;

  constructor(private http: HttpClient) {}

  myAccounts(): Observable<AccountResponse[]> {
    return this.http.get<AccountResponse[]>(this.base);
  }

  transactions(accountId: string): Observable<TransactionResponse[]> {
    return this.http.get<TransactionResponse[]>(`${this.base}/${accountId}/transactions`);
  }

  spendingSummary(periodDays = 30): Observable<SpendingSummaryResponse> {
    return this.http.get<SpendingSummaryResponse>(`${this.base}/spending-summary`, { params: { periodDays } });
  }

  recommendAccountType(): Observable<string> {
    return this.http.get(`${this.base}/recommend`, { responseType: 'text' });
  }

  upgradeEligibility(): Observable<UpgradeEligibilityResponse> {
    return this.http.get<UpgradeEligibilityResponse>(`${this.base}/upgrade-eligibility`);
  }

  accountsForUser(userId: string): Observable<AccountResponse[]> {
    return this.http.get<AccountResponse[]>(`${this.base}/user/${userId}`);
  }
}
