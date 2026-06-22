import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditAskRequest, AuditAskResponse } from '../models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private base = `${environment.apiBaseUrl}/audit`;

  constructor(private http: HttpClient) {}

  ask(question: string): Observable<AuditAskResponse> {
    const body: AuditAskRequest = { question };
    return this.http.post<AuditAskResponse>(`${this.base}/ask`, body);
  }
}
