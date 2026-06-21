import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface DashboardAnalytics {
  totalStudents: number;
  placedStudents: number;
  placementRate: number;
  averagePackage: number;
  highestPackage: number;
  scheduledDrives: number;
  pendingApplications: number;
}

@Component({
  selector: 'app-officer-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, MatCardModule, MatIconModule, MatButtonModule, MatTooltipModule, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  authService = inject(AuthService);

  analytics = signal<DashboardAnalytics | null>(null);
  loading = signal(true);
  errorMsg = signal('');

  userName = computed(() => this.authService.currentUser()?.fullName ?? 'Officer');

  ngOnInit(): void {
    this.api.get<DashboardAnalytics>('/api/analytics/dashboard').subscribe({
      next: res => {
        this.analytics.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load analytics.');
        this.loading.set(false);
      }
    });
  }

  formatPackage(value: number): string {
    if (!value) return '—';
    return `₹${(value / 100000).toFixed(1)} LPA`;
  }

  goToAnalytics(): void {
    this.router.navigate(['/officer/analytics']);
  }
}
