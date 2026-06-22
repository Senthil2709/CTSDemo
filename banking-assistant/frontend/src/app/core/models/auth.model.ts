export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  fullName: string;
  role: 'CUSTOMER' | 'RELATIONSHIP_MANAGER' | 'BRANCH_ADMIN';
  expiresInMs: number;
}

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: string;
  accountTier: string;
  kycStatus: string;
}
