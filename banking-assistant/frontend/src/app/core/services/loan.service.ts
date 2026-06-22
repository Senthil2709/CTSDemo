import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EmiCalculationRequest, EmiCalculationResponse, LoanApplicationRequest,
  LoanDecisionRequest, LoanEligibilityResult, LoanResponse, LoanType
} from '../models/loan.model';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private base = `${environment.apiBaseUrl}/loans`;

  constructor(private http: HttpClient) {}

  apply(request: LoanApplicationRequest): Observable<LoanResponse> {
    return this.http.post<LoanResponse>(`${this.base}/apply`, request);
  }

  myLoans(): Observable<LoanResponse[]> {
    return this.http.get<LoanResponse[]>(this.base);
  }

  getById(loanId: string): Observable<LoanResponse> {
    return this.http.get<LoanResponse>(`${this.base}/${loanId}`);
  }

  calculateEmi(request: EmiCalculationRequest): Observable<EmiCalculationResponse> {
    return this.http.post<EmiCalculationResponse>(`${this.base}/emi-calculator`, request);
  }

  checkEligibility(loanType: LoanType, principalAmount: number, tenureMonths: number): Observable<LoanEligibilityResult> {
    return this.http.get<LoanEligibilityResult>(`${this.base}/eligibility`, {
      params: { loanType, principalAmount, tenureMonths }
    });
  }

  recommendProduct(purpose: string): Observable<string> {
    return this.http.get(`${this.base}/recommend`, { params: { purpose }, responseType: 'text' });
  }

  pending(): Observable<LoanResponse[]> {
    return this.http.get<LoanResponse[]>(`${this.base}/pending`);
  }

  all(): Observable<LoanResponse[]> {
    return this.http.get<LoanResponse[]>(`${this.base}/all`);
  }

  decide(loanId: string, request: LoanDecisionRequest): Observable<LoanResponse> {
    return this.http.post<LoanResponse>(`${this.base}/${loanId}/decision`, request);
  }
}
