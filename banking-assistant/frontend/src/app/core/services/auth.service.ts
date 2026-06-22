import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../models/auth.model';

const TOKEN_KEY = 'banking_assistant_token';
const AUTH_KEY = 'banking_assistant_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private authSubject = new BehaviorSubject<AuthResponse | null>(this.readStoredAuth());
  readonly auth$ = this.authSubject.asObservable();

  constructor(private http: HttpClient) {}

  private readStoredAuth(): AuthResponse | null {
    const raw = sessionStorage.getItem(AUTH_KEY);
    return raw ? JSON.parse(raw) as AuthResponse : null;
  }

  get currentAuth(): AuthResponse | null {
    return this.authSubject.value;
  }

  get token(): string | null {
    return this.currentAuth?.token ?? null;
  }

  get role(): string | null {
    return this.currentAuth?.role ?? null;
  }

  get isLoggedIn(): boolean {
    return !!this.currentAuth;
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, request)
      .pipe(tap(res => this.persistAuth(res)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, request)
      .pipe(tap(res => this.persistAuth(res)));
  }

  me(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiBaseUrl}/auth/me`);
  }

  logout(): void {
    sessionStorage.removeItem(AUTH_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    this.authSubject.next(null);
  }

  private persistAuth(auth: AuthResponse): void {
    sessionStorage.setItem(AUTH_KEY, JSON.stringify(auth));
    this.authSubject.next(auth);
  }
}
