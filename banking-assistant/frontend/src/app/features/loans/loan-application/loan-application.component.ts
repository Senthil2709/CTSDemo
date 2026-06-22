import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { debounceTime } from 'rxjs';
import { LoanService } from '../../../core/services/loan.service';
import { EmiCalculationResponse, LoanType } from '../../../core/models/loan.model';

const INDICATIVE_RATES: Record<LoanType, number> = {
  HOME: 8.5,
  PERSONAL: 12.0,
  AUTO: 9.75
};

@Component({
  selector: 'app-loan-application',
  templateUrl: './loan-application.component.html'
})
export class LoanApplicationComponent implements OnInit {
  @Output() applied = new EventEmitter<void>();

  form: FormGroup;
  emiPreview: EmiCalculationResponse | null = null;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(private fb: FormBuilder, private loanService: LoanService) {
    this.form = this.fb.group({
      loanType: ['PERSONAL', Validators.required],
      principalAmount: [200000, [Validators.required, Validators.min(1000)]],
      tenureMonths: [24, [Validators.required, Validators.min(3)]],
      purpose: ['']
    });
  }

  ngOnInit(): void {
    this.updatePreview();
    this.form.valueChanges.pipe(debounceTime(250)).subscribe(() => this.updatePreview());
  }

  private updatePreview(): void {
    const { loanType, principalAmount, tenureMonths } = this.form.value;
    if (!principalAmount || !tenureMonths || principalAmount < 1000 || tenureMonths < 3) {
      this.emiPreview = null;
      return;
    }
    this.loanService.calculateEmi({
      principalAmount,
      annualInterestRate: INDICATIVE_RATES[loanType as LoanType],
      tenureMonths
    }).subscribe(res => this.emiPreview = res);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.loanService.apply(this.form.value).subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = 'Loan application submitted for approval.';
        this.applied.emit();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || 'Could not submit your application. Please try again.';
      }
    });
  }
}
