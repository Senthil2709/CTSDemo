import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FinancialPlanRequest, FinancialPlanResponse } from '../models/financial-plan.model';

@Injectable({ providedIn: 'root' })
export class FinancialPlanService {
  private base = `${environment.apiBaseUrl}/financial-plan`;

  constructor(private http: HttpClient) {}

  generate(request: FinancialPlanRequest): Observable<FinancialPlanResponse> {
    return this.http.post<FinancialPlanResponse>(`${this.base}/generate`, request);
  }
}
