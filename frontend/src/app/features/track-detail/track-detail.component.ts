import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrackService, NoteService } from '../../core/services';
import { TrackDto, NoteTreeNodeDto, CreateNoteRequest, UpdateNoteRequest, GrantPermissionRequest } from '../../shared/models';

@Component({
  selector: 'app-track-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './track-detail.component.html',
  styleUrls: ['./track-detail.component.scss']
})
export class TrackDetailComponent implements OnInit {
  trackId!: number;
  track: TrackDto | null = null;
  notes: NoteTreeNodeDto[] = [];
  selectedNote: NoteTreeNodeDto | null = null;

  editForm: { title: string; content: string } = { title: '', content: '' };
  createForm: { title: string; content: string; parentId: number | null } = { title: '', content: '', parentId: null };

  shareForm: { username: string; permissionType: 'VIEW' | 'EDIT' } = { username: '', permissionType: 'VIEW' };
  showShareDialog = false;

  // Edit mode properties
  editMode = false;
  editLoading = false;
  editError = '';

  // Create dialog properties
  showCreateDialog = false;
  createLoading = false;
  createError = '';

  // Share dialog properties
  shareLoading = false;
  shareError = '';

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trackService: TrackService,
    private noteService: NoteService
  ) {}

  ngOnInit(): void {
    this.trackId = +this.route.snapshot.paramMap.get('id')!;
    this.loadTrack();
    this.loadNotes();
  }

  loadTrack(): void {
    this.loading = true;
    this.trackService.getTrack(this.trackId).subscribe({
      next: (track: TrackDto) => {
        this.track = track;
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Ошибка загрузки трека';
        this.loading = false;
      }
    });
  }

  loadNotes(): void {
    this.loading = true;
    this.noteService.getNotesTree(this.trackId).subscribe({
      next: (notes: NoteTreeNodeDto[]) => {
        this.notes = notes;
        this.loading = false;
      },
      error: (error: any) => {
        this.errorMessage = 'Ошибка загрузки заметок';
        this.loading = false;
      }
    });
  }

  selectNote(note: NoteTreeNodeDto): void {
    this.selectedNote = note;
    this.editForm = { title: note.title, content: note.content || '' };
    this.editMode = false;
    this.editError = '';
  }

  startEdit(): void {
    this.editMode = true;
    this.editError = '';
  }

  cancelEdit(): void {
    this.editMode = false;
    this.editError = '';
    if (this.selectedNote) {
      this.editForm = { title: this.selectedNote.title, content: this.selectedNote.content || '' };
    }
  }

  saveNote(): void {
    if (!this.selectedNote) return;

    const request: UpdateNoteRequest = {
      title: this.editForm.title,
      content: this.editForm.content
    };

    this.editLoading = true;
    this.editError = '';

    this.noteService.updateNote(this.selectedNote.id, request).subscribe({
      next: (updatedNote: any) => {
        this.editMode = false;
        this.loadNotes();
        this.successMessage = 'Заметка обновлена';
        this.editLoading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.editError = 'Ошибка обновления заметки';
        this.editLoading = false;
      }
    });
  }

  openCreateDialog(parentId: number | null = null): void {
    this.createForm = { title: '', content: '', parentId };
    this.showCreateDialog = true;
    this.createError = '';
  }

  closeCreateDialog(): void {
    this.showCreateDialog = false;
    this.createForm = { title: '', content: '', parentId: null };
    this.createError = '';
  }

  createNote(): void {
    const request: CreateNoteRequest = {
      title: this.createForm.title,
      content: this.createForm.content,
      parentId: this.createForm.parentId || undefined
    };

    this.createLoading = true;
    this.createError = '';

    this.noteService.createNote(this.trackId, request).subscribe({
      next: () => {
        this.closeCreateDialog();
        this.loadNotes();
        this.successMessage = 'Заметка создана';
        this.createLoading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.createError = 'Ошибка создания заметки';
        this.createLoading = false;
      }
    });
  }

  deleteNote(noteId: number): void {
    if (!confirm('Удалить заметку и все её подзаметки?')) return;

    this.loading = true;
    this.noteService.deleteNote(noteId).subscribe({
      next: () => {
        if (this.selectedNote?.id === noteId) {
          this.selectedNote = null;
        }
        this.loadNotes();
        this.successMessage = 'Заметка удалена';
        this.loading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.errorMessage = 'Ошибка удаления заметки';
        this.loading = false;
      }
    });
  }

  openShareDialog(): void {
    this.showShareDialog = true;
    this.shareError = '';
  }

  closeShareDialog(): void {
    this.showShareDialog = false;
    this.shareForm = { username: '', permissionType: 'VIEW' };
    this.shareError = '';
  }

  shareTrack(): void {
    const request: GrantPermissionRequest = {
      username: this.shareForm.username,
      permissionType: this.shareForm.permissionType
    };

    this.shareLoading = true;
    this.shareError = '';

    this.trackService.grantPermission(this.trackId, request).subscribe({
      next: () => {
        this.closeShareDialog();
        this.successMessage = 'Доступ предоставлен';
        this.shareLoading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error: any) => {
        this.shareError = 'Ошибка предоставления доступа';
        this.shareLoading = false;
      }
    });
  }

  deleteTrack(): void {
    if (!confirm('Удалить трек и все заметки?')) return;

    this.loading = true;
    this.trackService.deleteTrack(this.trackId).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error: any) => {
        this.errorMessage = 'Ошибка удаления трека';
        this.loading = false;
      }
    });
  }

  canEdit(): boolean {
    return this.track?.myRole === 'OWNER' || this.track?.myRole === 'EDIT';
  }

  canDelete(): boolean {
    return this.track?.myRole === 'OWNER';
  }

  canShare(): boolean {
    return this.track?.myRole === 'OWNER';
  }

  getRoleBadgeClass(role: string): string {
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

  getRoleLabel(role: string): string {
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

  formatDate(date: string): string {
    const d = new Date(date);
    return d.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
