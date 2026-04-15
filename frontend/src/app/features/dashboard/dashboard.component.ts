import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TrackService } from '../../core/services/track.service';
import { TrackSummaryDto, CreateTrackRequest, TrackRole } from '../../shared/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private trackService = inject(TrackService);
  private router = inject(Router);

  tracks: TrackSummaryDto[] = [];
  loading = false;
  errorMessage = '';

  // Пагинация
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;

  // Фильтрация
  filter: 'all' | 'owned' | 'shared' = 'all';

  // Создание трека
  showCreateDialog = false;
  createForm = {
    title: '',
    description: ''
  };
  createLoading = false;
  createError = '';

  ngOnInit(): void {
    this.loadTracks();
  }

  loadTracks(): void {
    this.loading = true;
    this.errorMessage = '';

    this.trackService.getTracks(this.page, this.size, 'updatedAt,desc').subscribe({
      next: (response) => {
        this.tracks = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Ошибка загрузки треков';
        this.loading = false;
      }
    });
  }

  onPageChange(newPage: number): void {
    this.page = newPage;
    this.loadTracks();
  }

  onSizeChange(newSize: number): void {
    this.size = newSize;
    this.page = 0;
    this.loadTracks();
  }

  onFilterChange(newFilter: 'all' | 'owned' | 'shared'): void {
    this.filter = newFilter;
    this.page = 0;
    this.loadTracks();
  }

  openCreateDialog(): void {
    this.showCreateDialog = true;
    this.createForm = { title: '', description: '' };
    this.createError = '';
  }

  closeCreateDialog(): void {
    this.showCreateDialog = false;
    this.createForm = { title: '', description: '' };
    this.createError = '';
  }

  createTrack(): void {
    if (!this.createForm.title.trim()) {
      this.createError = 'Название трека обязательно';
      return;
    }

    this.createLoading = true;
    this.createError = '';

    const request: CreateTrackRequest = {
      title: this.createForm.title.trim(),
      description: this.createForm.description.trim() || undefined
    };

    this.trackService.createTrack(request).subscribe({
      next: (track) => {
        this.createLoading = false;
        this.closeCreateDialog();
        this.loadTracks();
      },
      error: (error) => {
        this.createError = error.error?.message || 'Ошибка создания трека';
        this.createLoading = false;
      }
    });
  }

  openTrack(trackId: number): void {
    this.router.navigate(['/tracks', trackId]);
  }

  canEditTrack(track: TrackSummaryDto): boolean {
    return track.myRole === 'OWNER' || track.myRole === 'EDIT';
  }

  canDeleteTrack(track: TrackSummaryDto): boolean {
    return track.myRole === 'OWNER';
  }

  deleteTrack(trackId: number): void {
    if (!confirm('Вы уверены, что хотите удалить этот трек? Все заметки будут удалены.')) {
      return;
    }

    this.trackService.deleteTrack(trackId).subscribe({
      next: () => {
        this.loadTracks();
      },
      error: (error) => {
        alert(error.error?.message || 'Ошибка удаления трека');
      }
    });
  }

  getRoleBadgeClass(role: TrackRole): string {
    switch (role) {
      case 'OWNER':
        return 'badge-owner';
      case 'EDIT':
        return 'badge-edit';
      case 'VIEW':
        return 'badge-view';
      default:
        return '';
    }
  }

  getRoleLabel(role: TrackRole): string {
    switch (role) {
      case 'OWNER':
        return 'Владелец';
      case 'EDIT':
        return 'Редактор';
      case 'VIEW':
        return 'Читатель';
      default:
        return role;
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
