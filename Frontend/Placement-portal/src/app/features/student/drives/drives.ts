import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { PagedResponse } from '../../../core/models/api.model';
import { JobDto } from '../../../core/models/job.model';
import { ResumeDto } from '../../../core/models/student.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface DriveDto {
  id: string;
  title: string;
  driveDate: string;
  venue: string;
  mode: string;
  description: string;
  status: string;
  companyName?: string;
  jobs: JobDto[];
}

interface ApplyForm {
  jobId: string;
  resumeId: string;
  coverLetter: string;
}

@Component({
  selector: 'app-student-drives',
  standalone: true,
  imports: [FormsModule, SlicePipe, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './drives.html',
  styleUrl: './drives.css'
})
export class StudentDrives implements OnInit {
  private api = inject(ApiService);

  drives = signal<DriveDto[]>([]);
  resumes = signal<ResumeDto[]>([]);
  appliedJobIds = signal<Set<string>>(new Set());
  loading = signal(true);

  selectedDrive = signal<DriveDto | null>(null);
  selectedJobForApply = signal<JobDto | null>(null);
  applyForm = signal<ApplyForm>({ jobId: '', resumeId: '', coverLetter: '' });
  applying = signal(false);
  applyError = signal('');
  applySuccess = signal('');

  ngOnInit(): void {
    this.loadDrives();
    this.api.get<ResumeDto[]>('/api/resumes/my').subscribe({
      next: res => this.resumes.set(res.data ?? [])
    });
    this.api.get<PagedResponse<{ jobId: string }>>('/api/applications/my', { page: 0, size: 100 }).subscribe({
      next: res => {
        const ids = new Set(res.data.content.map(a => a.jobId).filter(Boolean));
        this.appliedJobIds.set(ids);
      }
    });
  }

  loadDrives(): void {
    this.loading.set(true);
    this.api.get<PagedResponse<DriveDto>>('/api/drives', { page: 0, size: 50 }).subscribe({
      next: res => {
        this.drives.set(res.data.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openDrive(drive: DriveDto): void {
    this.selectedDrive.set(drive);
    this.selectedJobForApply.set(null);
    this.applyError.set('');
    this.applySuccess.set('');
  }

  closeDrive(): void {
    this.selectedDrive.set(null);
    this.selectedJobForApply.set(null);
  }

  openApplyModal(job: JobDto): void {
    this.selectedJobForApply.set(job);
    this.applyForm.set({ jobId: job.id, resumeId: '', coverLetter: '' });
    this.applyError.set('');
    this.applySuccess.set('');
  }

  closeApplyModal(): void {
    this.selectedJobForApply.set(null);
  }

  updateApplyForm(field: string, value: string): void {
    this.applyForm.update(f => ({ ...f, [field]: value }));
  }

  submitApplication(): void {
    if (!this.applyForm().resumeId) {
      this.applyError.set('Please select a resume.');
      return;
    }
    this.applying.set(true);
    this.applyError.set('');
    this.api.post<void>('/api/applications', this.applyForm()).subscribe({
      next: () => {
        const jobId = this.applyForm().jobId;
        this.appliedJobIds.update(ids => { ids.add(jobId); return new Set(ids); });
        this.applying.set(false);
        this.applySuccess.set('Application submitted successfully!');
        setTimeout(() => this.closeApplyModal(), 1500);
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to submit application.';
        this.applyError.set(msg);
        this.applying.set(false);
      }
    });
  }

  isApplied(jobId: string): boolean {
    return this.appliedJobIds().has(jobId);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  formatSalary(min: number, max: number): string {
    if (!min && !max) return 'Not disclosed';
    return `₹${(min / 100000).toFixed(1)}L – ₹${(max / 100000).toFixed(1)}L`;
  }
}
