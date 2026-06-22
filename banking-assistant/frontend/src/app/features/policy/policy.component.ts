import { Component } from '@angular/core';
import { PolicyService } from '../../core/services/policy.service';
import { PolicyAnswerResponse } from '../../core/models/policy.model';

@Component({
  selector: 'app-policy',
  templateUrl: './policy.component.html'
})
export class PolicyComponent {
  question = '';
  loading = false;
  result: PolicyAnswerResponse | null = null;
  errorMessage = '';

  suggestedQuestions = [
    'What documents are required for KYC?',
    'What are the eligibility criteria for a home loan?',
    'What fees apply to my savings account?',
    'How do I raise a grievance?'
  ];

  constructor(private policyService: PolicyService) {}

  ask(question?: string): void {
    const q = (question ?? this.question).trim();
    if (!q) return;
    this.question = q;
    this.loading = true;
    this.errorMessage = '';
    this.policyService.ask(q).subscribe({
      next: (res) => { this.result = res; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Could not look that up right now.';
      }
    });
  }
}
