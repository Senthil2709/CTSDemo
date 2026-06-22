import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ChatComponent } from './features/chat/chat.component';
import { LoanListComponent } from './features/loans/loan-list/loan-list.component';
import { FinancialPlanComponent } from './features/financial-plan/financial-plan.component';
import { PolicyComponent } from './features/policy/policy.component';
import { ApprovalQueueComponent } from './features/admin/approval-queue/approval-queue.component';
import { AuditComponent } from './features/audit/audit.component';

import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'chat', component: ChatComponent, canActivate: [AuthGuard] },
  { path: 'loans', component: LoanListComponent, canActivate: [AuthGuard] },
  { path: 'financial-plan', component: FinancialPlanComponent, canActivate: [AuthGuard] },
  { path: 'policy', component: PolicyComponent, canActivate: [AuthGuard] },
  { path: 'audit', component: AuditComponent, canActivate: [AuthGuard] },
  {
    path: 'approvals',
    component: ApprovalQueueComponent,
    canActivate: [RoleGuard],
    data: { roles: ['RELATIONSHIP_MANAGER', 'BRANCH_ADMIN'] }
  },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
