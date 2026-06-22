import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PolicyAnswerResponse } from '../models/policy.model';

@Injectable({ providedIn: 'root' })
export class PolicyService {
  private base = `${environment.apiBaseUrl}/policy`;

  constructor(private http: HttpClient) {}

  ask(question: string): Observable<PolicyAnswerResponse> {
    return this.http.post<PolicyAnswerResponse>(`${this.base}/ask`, { question });
  }
}
