import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Invalid username or password.';
      }
    });
  }

  fillDemo(role: 'customer' | 'rm' | 'admin'): void {
    const creds: Record<string, { username: string; password: string }> = {
      customer: { username: 'customer1', password: 'Customer@123' },
      rm: { username: 'relmanager', password: 'Manager@123' },
      admin: { username: 'branchadmin', password: 'Admin@123' }
    };
    this.form.patchValue(creds[role]);
  }
}
