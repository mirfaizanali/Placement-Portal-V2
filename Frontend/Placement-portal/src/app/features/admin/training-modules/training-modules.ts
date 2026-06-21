import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
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

interface TrainingModuleForm {
  title: string;
  description: string;
  learningLink: string;
  iconName: string;
  displayOrder: number;
}

const EMPTY_FORM: TrainingModuleForm = {
  title: '',
  description: '',
  learningLink: '',
  iconName: 'school',
  displayOrder: 0
};

@Component({
  selector: 'app-admin-training-modules',
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './training-modules.html',
  styleUrl: './training-modules.css'
})
export class TrainingModules implements OnInit {
  private api = inject(ApiService);

  modules = signal<TrainingModuleDto[]>([]);
  loading = signal(true);
  saving = signal(false);
  showAddForm = signal(false);
  editingId = signal<string | null>(null);
  form = signal<TrainingModuleForm>({ ...EMPTY_FORM });
  successMsg = signal('');
  errorMsg = signal('');

  ngOnInit(): void {
    this.loadModules();
  }

  loadModules(): void {
    this.loading.set(true);
    this.api.get<TrainingModuleDto[]>('/api/admin/training-modules').subscribe({
      next: res => {
        this.modules.set(res.data ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load training modules.');
        this.loading.set(false);
      }
    });
  }

  openAdd(): void {
    this.editingId.set(null);
    this.form.set({
      ...EMPTY_FORM,
      displayOrder: (this.modules().reduce((max, m) => Math.max(max, m.displayOrder), 0) + 1)
    });
    this.errorMsg.set('');
    this.showAddForm.set(true);
  }

  cancelAdd(): void {
    this.showAddForm.set(false);
    this.form.set({ ...EMPTY_FORM });
  }

  startEdit(m: TrainingModuleDto): void {
    this.showAddForm.set(false);
    this.editingId.set(m.id);
    this.form.set({
      title: m.title,
      description: m.description,
      learningLink: m.learningLink,
      iconName: m.iconName,
      displayOrder: m.displayOrder
    });
    this.errorMsg.set('');
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.set({ ...EMPTY_FORM });
  }

  updateForm<K extends keyof TrainingModuleForm>(field: K, value: TrainingModuleForm[K]): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  submitCreate(): void {
    if (!this.validate()) return;
    this.saving.set(true);
    this.api.post<TrainingModuleDto>('/api/admin/training-modules', this.form()).subscribe({
      next: res => {
        this.modules.update(list => [...list, res.data].sort((a, b) => a.displayOrder - b.displayOrder));
        this.cancelAdd();
        this.saving.set(false);
        this.flashSuccess('Training module created.');
      },
      error: err => {
        this.errorMsg.set(err?.error?.message ?? 'Failed to create training module.');
        this.saving.set(false);
      }
    });
  }

  submitEdit(): void {
    const id = this.editingId();
    if (!id || !this.validate()) return;
    this.saving.set(true);
    this.api.put<TrainingModuleDto>(`/api/admin/training-modules/${id}`, this.form()).subscribe({
      next: res => {
        this.modules.update(list =>
          list.map(m => m.id === id ? res.data : m).sort((a, b) => a.displayOrder - b.displayOrder)
        );
        this.cancelEdit();
        this.saving.set(false);
        this.flashSuccess('Training module updated.');
      },
      error: err => {
        this.errorMsg.set(err?.error?.message ?? 'Failed to update training module.');
        this.saving.set(false);
      }
    });
  }

  deleteModule(m: TrainingModuleDto): void {
    if (!confirm(`Delete "${m.title}"? This will remove it from the student training page.`)) return;
    this.api.delete<void>(`/api/admin/training-modules/${m.id}`).subscribe({
      next: () => {
        this.modules.update(list => list.filter(x => x.id !== m.id));
        this.flashSuccess('Training module deleted.');
      },
      error: () => this.errorMsg.set('Failed to delete training module.')
    });
  }

  private validate(): boolean {
    const f = this.form();
    if (!f.title.trim() || !f.description.trim() || !f.learningLink.trim() || !f.iconName.trim()) {
      this.errorMsg.set('Please fill in title, description, learning link and icon.');
      return false;
    }
    if (!/^https?:\/\//.test(f.learningLink)) {
      this.errorMsg.set('Learning link must start with http:// or https://');
      return false;
    }
    if (f.displayOrder == null || f.displayOrder < 0) {
      this.errorMsg.set('Display order must be 0 or greater.');
      return false;
    }
    this.errorMsg.set('');
    return true;
  }

  private flashSuccess(msg: string): void {
    this.successMsg.set(msg);
    setTimeout(() => this.successMsg.set(''), 3000);
  }
}
