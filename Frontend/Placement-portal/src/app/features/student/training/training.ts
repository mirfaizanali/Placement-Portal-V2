import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface TrainingModuleDto {
  id: string;
  title: string;
  description: string;
  learningLink: string;
  iconName: string;
  displayOrder: number;
}

@Component({
  selector: 'app-student-training',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule, LoadingSpinnerComponent],
  templateUrl: './training.html',
  styleUrl: './training.css'
})
export class Training implements OnInit {
  private api = inject(ApiService);

  modules = signal<TrainingModuleDto[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.get<TrainingModuleDto[]>('/api/training/modules').subscribe({
      next: res => {
        this.modules.set(res.data ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load training modules. Please try again later.');
        this.loading.set(false);
      }
    });
  }
}
