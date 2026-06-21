import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

export const routes: Routes = [
  // Landing Page
  { 
    path: '', 
    loadComponent: () => import('./features/landing/landing').then(m => m.Landing),
    pathMatch: 'full' 
  },

  // Auth
  {
    path: 'auth',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./layout/auth-layout/auth-layout').then(m => m.AuthLayout),
    children: [
      { path: 'login',    loadComponent: () => import('./features/auth/login/login').then(m => m.Login) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register').then(m => m.Register) },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // Student
  {
    path: 'student',
    canActivate: [authGuard, roleGuard],
    data: { requiredRole: 'STUDENT' },
    loadComponent: () => import('./layout/student-layout/student-layout').then(m => m.StudentLayout),
    children: [
      { path: 'dashboard',       loadComponent: () => import('./features/student/dashboard/dashboard').then(m => m.Dashboard) },
      { path: 'profile',         loadComponent: () => import('./features/student/profile/profile').then(m => m.Profile) },
      { path: 'resume',          loadComponent: () => import('./features/student/resume/resume').then(m => m.Resume) },
      { path: 'jobs',            loadComponent: () => import('./features/student/job-search/job-search').then(m => m.JobSearch) },
      { path: 'drives',          loadComponent: () => import('./features/student/drives/drives').then(m => m.StudentDrives) },
      { path: 'applications',    loadComponent: () => import('./features/student/applications/applications').then(m => m.Applications) },
      { path: 'training',        loadComponent: () => import('./features/student/training/training').then(m => m.Training) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },


  // Employer
  {
    path: 'employer',
    canActivate: [authGuard, roleGuard],
    data: { requiredRole: 'EMPLOYER' },
    loadComponent: () => import('./layout/employer-layout/employer-layout').then(m => m.EmployerLayout),
    children: [
      { path: 'dashboard',   loadComponent: () => import('./features/employer/dashboard/dashboard').then(m => m.Dashboard) },
      { path: 'post-job',    loadComponent: () => import('./features/employer/post-job/post-job').then(m => m.PostJob) },
      { path: 'applicants',  loadComponent: () => import('./features/employer/applicants/applicants').then(m => m.Applicants) },
      { path: 'interviews',  loadComponent: () => import('./features/employer/interviews/interviews').then(m => m.Interviews) },
      { path: 'profile',     loadComponent: () => import('./features/employer/profile/profile').then(m => m.EmployerProfile) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Placement Officer
  {
    path: 'officer',
    canActivate: [authGuard, roleGuard],
    data: { requiredRole: 'PLACEMENT_OFFICER' },
    loadComponent: () => import('./layout/officer-layout/officer-layout').then(m => m.OfficerLayout),
    children: [
      { path: 'dashboard',    loadComponent: () => import('./features/placement-officer/dashboard/dashboard').then(m => m.Dashboard) },
      { path: 'drives',       loadComponent: () => import('./features/placement-officer/drives/drives').then(m => m.Drives) },
      { path: 'employers',    loadComponent: () => import('./features/placement-officer/employer-management/employer-management').then(m => m.EmployerManagement) },
      { path: 'analytics',    loadComponent: () => import('./features/placement-officer/analytics/analytics').then(m => m.Analytics) },
      { path: 'students',     loadComponent: () => import('./features/placement-officer/student-overview/student-overview').then(m => m.StudentOverview) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Admin
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { requiredRole: 'ADMIN' },
    loadComponent: () => import('./layout/admin-layout/admin-layout').then(m => m.AdminLayout),
    children: [
      { path: 'dashboard',    loadComponent: () => import('./features/admin/dashboard/dashboard').then(m => m.AdminDashboard) },
      { path: 'users',        loadComponent: () => import('./features/admin/user-management/user-management').then(m => m.UserManagement) },
      { path: 'config',       loadComponent: () => import('./features/admin/system-config/system-config').then(m => m.SystemConfig) },
      { path: 'audit-logs',     loadComponent: () => import('./features/admin/audit-logs/audit-logs').then(m => m.AuditLogs) },
      { path: 'training-modules', loadComponent: () => import('./features/admin/training-modules/training-modules').then(m => m.TrainingModules) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: '/auth/login' }
];
