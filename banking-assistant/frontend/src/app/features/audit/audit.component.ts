import { Component } from '@angular/core';
import { AuditService } from '../../core/services/audit.service';
import { AuditAskResponse, AuditToolCall } from '../../core/models/audit.model';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html'
})
export class AuditComponent {
  question = '';
  loading = false;
  result: AuditAskResponse | null = null;
  errorMessage = '';
  expandedTools = false;

  suggestedQuestions = [
    'Show me all high-value transactions above ₹1,00,000',
    'List accounts with failed KYC verification',
    'Which loan applications are pending review?',
    'Show suspicious activity in the last 30 days'
  ];

  constructor(private auditService: AuditService) {}

  ask(question?: string): void {
    const q = (question ?? this.question).trim();
    if (!q) return;
    this.question = q;
    this.loading = true;
    this.errorMessage = '';
    this.result = null;
    this.expandedTools = false;

    this.auditService.ask(q).subscribe({
      next: (res) => { this.result = res; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'MCP query failed. Please try again.';
      }
    });
  }

  formatToolOutput(output: unknown): string {
    if (output === null || output === undefined) return '—';
    if (typeof output === 'string') return output;
    return JSON.stringify(output, null, 2);
  }
}
