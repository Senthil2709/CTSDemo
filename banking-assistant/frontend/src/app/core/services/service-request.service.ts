import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApprovalQueueResponse, ServiceRequestCreate, ServiceRequestDecisionRequest, ServiceRequestResponse
} from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private base = `${environment.apiBaseUrl}/service-requests`;
  private adminBase = `${environment.apiBaseUrl}/admin`;

  constructor(private http: HttpClient) {}

  create(request: ServiceRequestCreate): Observable<ServiceRequestResponse> {
    return this.http.post<ServiceRequestResponse>(this.base, request);
  }

  myRequests(): Observable<ServiceRequestResponse[]> {
    return this.http.get<ServiceRequestResponse[]>(this.base);
  }

  pending(): Observable<ServiceRequestResponse[]> {
    return this.http.get<ServiceRequestResponse[]>(`${this.base}/pending`);
  }

  decide(requestId: string, decision: ServiceRequestDecisionRequest): Observable<ServiceRequestResponse> {
    return this.http.post<ServiceRequestResponse>(`${this.base}/${requestId}/decision`, decision);
  }

  approvalQueue(): Observable<ApprovalQueueResponse> {
    return this.http.get<ApprovalQueueResponse>(`${this.adminBase}/approval-queue`);
  }
}
