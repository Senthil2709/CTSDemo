import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FinancialPlanService } from '../../core/services/financial-plan.service';
import { FinancialPlanResponse } from '../../core/models/financial-plan.model';

@Component({
  selector: 'app-financial-plan',
  templateUrl: './financial-plan.component.html'
})
export class FinancialPlanComponent {
  form: FormGroup;
  loading = false;
  errorMessage = '';
  result: FinancialPlanResponse | null = null;

  constructor(private fb: FormBuilder, private financialPlanService: FinancialPlanService) {
    this.form = this.fb.group({
      monthlyIncome: [80000, [Validators.required, Validators.min(0)]],
      monthlyExpenses: [45000, [Validators.required, Validators.min(0)]],
      existingMonthlyEmi: [0, [Validators.min(0)]],
      goal: ['HOME_PURCHASE', Validators.required],
      timelineMonths: [36, [Validators.required, Validators.min(1)]],
      riskAppetite: ['MODERATE', Validators.required],
      targetAmount: [1500000]
    });
  }

  generate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.financialPlanService.generate(this.form.value).subscribe({
      next: (res) => { this.result = res; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Could not generate a plan right now.';
      }
    });
  }

  get scheduleMilestones() {
    if (!this.result) return [];
    const schedule = this.result.monthlySchedule;
    if (!schedule.length) return [];
    const step = Math.max(1, Math.floor(schedule.length / 6));
    return schedule.filter((_, i) => i % step === 0 || i === schedule.length - 1);
  }
}
