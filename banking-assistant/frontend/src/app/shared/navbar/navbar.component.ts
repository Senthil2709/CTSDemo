import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  constructor(public authService: AuthService, private router: Router) {}

  get isStaff(): boolean {
    const role = this.authService.role;
    return role === 'RELATIONSHIP_MANAGER' || role === 'BRANCH_ADMIN';
  }

  get fullName(): string {
    return this.authService.currentAuth?.fullName ?? '';
  }

  get tier(): string {
    return this.authService.role === 'CUSTOMER' ? '' : (this.authService.role ?? '');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
