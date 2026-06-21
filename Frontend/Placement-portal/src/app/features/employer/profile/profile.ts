import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface EmployerProfileData {
  id: string;
  userId: string;
  companyName: string;
  companyWebsite: string;
  industry: string;
  companySize: string;
  hrContactName: string;
  hrContactPhone: string;
  description: string;
  location: string;
  isVerified: boolean;
  logoUrl: string;
}

@Component({
  selector: 'app-employer-profile',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, LoadingSpinnerComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class EmployerProfile implements OnInit {
  private api = inject(ApiService);

  profile = signal<EmployerProfileData | null>(null);
  loading = signal(true);
  saving = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  editForm = signal({
    companyName: '',
    companyWebsite: '',
    industry: '',
    companySize: '',
    hrContactName: '',
    hrContactPhone: '',
    location: '',
    description: ''
  });

  companySizes = [
    { value: 'STARTUP',    label: '1-10 (Startup)' },
    { value: 'SMALL',      label: '11-50 (Small)' },
    { value: 'MEDIUM',     label: '51-250 (Medium)' },
    { value: 'LARGE',      label: '251-1000 (Large)' },
    { value: 'ENTERPRISE', label: '1000+ (Enterprise)' },
  ];

  ngOnInit(): void {
    this.api.get<EmployerProfileData>('/api/employers/me').subscribe({
      next: res => {
        this.profile.set(res.data);
        this.editForm.set({
          companyName: res.data.companyName ?? '',
          companyWebsite: res.data.companyWebsite ?? '',
          industry: res.data.industry ?? '',
          companySize: res.data.companySize ?? '',
          hrContactName: res.data.hrContactName ?? '',
          hrContactPhone: res.data.hrContactPhone ?? '',
          location: res.data.location ?? '',
          description: res.data.description ?? ''
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load profile.');
        this.loading.set(false);
      }
    });
  }

  updateForm(field: string, value: string): void {
    this.editForm.update(f => ({ ...f, [field]: value }));
  }

  save(): void {
    this.saving.set(true);
    this.errorMsg.set('');

    const form = this.editForm();
    const payload: Record<string, string | null> = {};
    for (const [key, value] of Object.entries(form)) {
      payload[key] = value === '' ? null : value;
    }

    this.api.put<EmployerProfileData>('/api/employers/me', payload).subscribe({
      next: res => {
        this.profile.set(res.data);
        this.saving.set(false);
        this.successMsg.set('Profile updated successfully!');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: (err) => {
        const serverMsg = err?.error?.message
          || (err?.error?.fieldErrors && Object.entries(err.error.fieldErrors).map(([k, v]) => `${k}: ${v}`).join('; '))
          || err?.message
          || 'Unknown error';
        this.errorMsg.set(`Failed to save profile: ${serverMsg}`);
        this.saving.set(false);
      }
    });
  }
}
