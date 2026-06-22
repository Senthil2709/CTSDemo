import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const allowedRoles = route.data['roles'] as string[] | undefined;
    const role = this.authService.role;

    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return false;
    }
    if (allowedRoles && role && !allowedRoles.includes(role)) {
      this.router.navigate(['/dashboard']);
      return false;
    }
    return true;
  }
}
