// === CHUNK: TRACK_DETAIL_COMPONENT [UI] ===
// Описание: Компонент для управления деталями трека, включая заметки и права доступа.
// Dependencies: Angular Core, Common, Forms, Router, TrackService, NoteService, shared models

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrackService, NoteService } from '../../core/services';
import { TrackDto, NoteTreeNodeDto, CreateNoteRequest, UpdateNoteRequest, GrantPermissionRequest } from '../../shared/models';
import { logLine } from '../../core/lib/log';

// [START_TRACK_DETAIL_COMPONENT]
/*
 * ANCHOR: TRACK_DETAIL_COMPONENT
 * PURPOSE: Управление деталями трека, включая заметки и права доступа.
 *
 * @PreConditions:
 * - пользователь аутентифицирован и имеет доступ к треку
 * - TrackService и NoteService доступны через DI
 *
 * @PostConditions:
 * - трек и связанные заметки загружены и отображены в UI
 *
 * @Invariants:
 * - трек и заметки остаются согласованными с сервером
 *
 * @SideEffects:
 * - HTTP запросы к API для получения/модификации данных
 *
 * @ForbiddenChanges:
 * - нельзя менять логику доступа без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода, рефакторинг UI
 */
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

  // [START_TRACK_DETAIL_ON_INIT]
  /*
   * ANCHOR: TRACK_DETAIL_ON_INIT
   * PURPOSE: Инициализация компонента и загрузка данных трека.
   *
   * @PreConditions:
   * - компонент создан Angular фреймворком
   * - trackId присутствует в параметрах маршрута
   *
   * @PostConditions:
   * - trackId извлечён из параметров маршрута
   * - трек и заметки загружены с сервера
   *
   * @Invariants:
   * - метод вызывается автоматически при создании компонента
   *
   * @SideEffects:
   * - HTTP запросы к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать вызовы loadTrack() и loadNotes()
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную инициализацию
   */
  ngOnInit(): void {
    logLine('ui', 'DEBUG', 'ngOnInit', 'TRACK_DETAIL_ON_INIT', 'ENTRY', {});

    this.trackId = +this.route.snapshot.paramMap.get('id')!;
    this.loadTrack();
    this.loadNotes();

    logLine('ui', 'DEBUG', 'ngOnInit', 'TRACK_DETAIL_ON_INIT', 'EXIT', {
      track_id: this.trackId
    });
  }
  // [END_TRACK_DETAIL_ON_INIT]

  // [START_TRACK_DETAIL_LOAD_TRACK]
  /*
   * ANCHOR: TRACK_DETAIL_LOAD_TRACK
   * PURPOSE: Загрузка данных трека с сервера.
   *
   * @PreConditions:
   * - trackId валидный ID трека
   *
   * @PostConditions:
   * - при успехе: track заполнен данными
   * - при ошибке: errorMessage содержит описание ошибки
   *
   * @Invariants:
   * - loading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP GET запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование данных трека
   */
  loadTrack(): void {
    logLine('ui', 'DEBUG', 'loadTrack', 'TRACK_DETAIL_LOAD_TRACK', 'ENTRY', {
      track_id: this.trackId
    });

    this.loading = true;
    this.trackService.getTrack(this.trackId).subscribe({
      next: (track: TrackDto) => {
        this.track = track;
        this.loading = false;

        logLine('ui', 'DEBUG', 'loadTrack', 'TRACK_DETAIL_LOAD_TRACK', 'EXIT', {
          track_id: this.trackId,
          success: true
        });
      },
      error: () => {
        this.errorMessage = 'Ошибка загрузки трека';
        this.loading = false;

        logLine('ui', 'ERROR', 'loadTrack', 'TRACK_DETAIL_LOAD_TRACK', 'ERROR', {
          track_id: this.trackId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_LOAD_TRACK]

  // [START_TRACK_DETAIL_LOAD_NOTES]
  /*
   * ANCHOR: TRACK_DETAIL_LOAD_NOTES
   * PURPOSE: Загрузка дерева заметок трека с сервера.
   *
   * @PreConditions:
   * - trackId валидный ID трека
   *
   * @PostConditions:
   * - при успехе: notes заполнен деревом заметок
   * - при ошибке: errorMessage содержит описание ошибки
   *
   * @Invariants:
   * - loading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP GET запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование заметок
   */
  loadNotes(): void {
    logLine('ui', 'DEBUG', 'loadNotes', 'TRACK_DETAIL_LOAD_NOTES', 'ENTRY', {
      track_id: this.trackId
    });

    this.loading = true;
    this.noteService.getNotesTree(this.trackId).subscribe({
      next: (notes: NoteTreeNodeDto[]) => {
        this.notes = notes;
        this.loading = false;

        logLine('ui', 'DEBUG', 'loadNotes', 'TRACK_DETAIL_LOAD_NOTES', 'EXIT', {
          track_id: this.trackId,
          notes_count: notes.length
        });
      },
      error: () => {
        this.errorMessage = 'Ошибка загрузки заметок';
        this.loading = false;

        logLine('ui', 'ERROR', 'loadNotes', 'TRACK_DETAIL_LOAD_NOTES', 'ERROR', {
          track_id: this.trackId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_LOAD_NOTES]

  // [START_TRACK_DETAIL_SELECT_NOTE]
  /*
   * ANCHOR: TRACK_DETAIL_SELECT_NOTE
   * PURPOSE: Выбор заметки для просмотра/редактирования.
   *
   * @PreConditions:
   * - note валидный объект NoteTreeNodeDto
   *
   * @PostConditions:
   * - selectedNote обновлён
   * - editForm заполнен данными заметки
   * - editMode сброшен в false
   *
   * @Invariants:
   * - editForm всегда синхронизирован с selectedNote
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не сбросить editMode при выборе заметки
   *
   * @AllowedRefactorZone:
   * - можно добавить автосохранение при смене заметки
   */
  selectNote(note: NoteTreeNodeDto): void {
    logLine('ui', 'DEBUG', 'selectNote', 'TRACK_DETAIL_SELECT_NOTE', 'ENTRY', {
      note_id: note.id
    });

    this.selectedNote = note;
    this.editForm = { title: note.title, content: note.content || '' };
    this.editMode = false;
    this.editError = '';

    logLine('ui', 'DEBUG', 'selectNote', 'TRACK_DETAIL_SELECT_NOTE', 'EXIT', {
      note_id: note.id
    });
  }
  // [END_TRACK_DETAIL_SELECT_NOTE]

  // [START_TRACK_DETAIL_START_EDIT]
  /*
   * ANCHOR: TRACK_DETAIL_START_EDIT
   * PURPOSE: Включение режима редактирования заметки.
   *
   * @PreConditions:
   * - selectedNote не null
   *
   * @PostConditions:
   * - editMode установлен в true
   *
   * @Invariants:
   * - editError всегда сбрасывается при входе в режим редактирования
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку прав на редактирование
   */
  startEdit(): void {
    logLine('ui', 'DEBUG', 'startEdit', 'TRACK_DETAIL_START_EDIT', 'ENTRY', {
      note_id: this.selectedNote?.id
    });

    this.editMode = true;
    this.editError = '';

    logLine('ui', 'DEBUG', 'startEdit', 'TRACK_DETAIL_START_EDIT', 'EXIT', {
      edit_mode: true
    });
  }
  // [END_TRACK_DETAIL_START_EDIT]

  // [START_TRACK_DETAIL_CANCEL_EDIT]
  /*
   * ANCHOR: TRACK_DETAIL_CANCEL_EDIT
   * PURPOSE: Отмена редактирования заметки.
   *
   * @PreConditions:
   * - editMode установлен в true
   *
   * @PostConditions:
   * - editMode сброшен в false
   * - editForm восстановлен к исходным данным
   *
   * @Invariants:
   * - editForm всегда синхронизирован с selectedNote после отмены
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не восстановить editForm
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение отмены при несохранённых изменениях
   */
  cancelEdit(): void {
    logLine('ui', 'DEBUG', 'cancelEdit', 'TRACK_DETAIL_CANCEL_EDIT', 'ENTRY', {
      note_id: this.selectedNote?.id
    });

    this.editMode = false;
    this.editError = '';
    if (this.selectedNote) {
      this.editForm = { title: this.selectedNote.title, content: this.selectedNote.content || '' };
    }

    logLine('ui', 'DEBUG', 'cancelEdit', 'TRACK_DETAIL_CANCEL_EDIT', 'EXIT', {
      edit_mode: false
    });
  }
  // [END_TRACK_DETAIL_CANCEL_EDIT]

  // [START_TRACK_DETAIL_SAVE_NOTE]
  /*
   * ANCHOR: TRACK_DETAIL_SAVE_NOTE
   * PURPOSE: Сохранение изменений заметки.
   *
   * @PreConditions:
   * - selectedNote не null
   * - editForm.title непустая строка
   *
   * @PostConditions:
   * - при успехе: заметка обновлена, режим редактирования закрыт, заметки перезагружены
   * - при ошибке: editError содержит описание ошибки
   *
   * @Invariants:
   * - editLoading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP PUT запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать перезагрузку заметок после сохранения
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей формы
   */
  saveNote(): void {
    if (!this.selectedNote) return;

    const noteId = this.selectedNote.id;

    logLine('ui', 'DEBUG', 'saveNote', 'TRACK_DETAIL_SAVE_NOTE', 'ENTRY', {
      note_id: noteId
    });

    const request: UpdateNoteRequest = { title: this.editForm.title, content: this.editForm.content };
    this.editLoading = true;
    this.editError = '';
    this.noteService.updateNote(noteId, request).subscribe({
      next: () => {
        this.editMode = false;
        this.loadNotes();
        this.successMessage = 'Заметка обновлена';
        this.editLoading = false;
        setTimeout(() => this.successMessage = '', 3000);

        logLine('ui', 'INFO', 'saveNote', 'TRACK_DETAIL_SAVE_NOTE', 'STATE_CHANGE', {
          entity: 'note',
          action: 'updated',
          note_id: noteId
        });
      },
      error: () => {
        this.editError = 'Ошибка обновления заметки';
        this.editLoading = false;

        logLine('ui', 'ERROR', 'saveNote', 'TRACK_DETAIL_SAVE_NOTE', 'ERROR', {
          note_id: noteId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_SAVE_NOTE]

  // [START_TRACK_DETAIL_OPEN_CREATE_DIALOG]
  /*
   * ANCHOR: TRACK_DETAIL_OPEN_CREATE_DIALOG
   * PURPOSE: Открытие диалога создания заметки.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - диалог открыт, форма очищена, parentId установлен
   *
   * @Invariants:
   * - createForm всегда сбрасывается при открытии диалога
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не очистить форму при открытии
   *
   * @AllowedRefactorZone:
   * - можно добавить предзаполнение формы
   */
  openCreateDialog(parentId: number | null = null): void {
    logLine('ui', 'DEBUG', 'openCreateDialog', 'TRACK_DETAIL_OPEN_CREATE_DIALOG', 'ENTRY', {
      parent_id: parentId
    });

    this.createForm = { title: '', content: '', parentId };
    this.showCreateDialog = true;
    this.createError = '';

    logLine('ui', 'DEBUG', 'openCreateDialog', 'TRACK_DETAIL_OPEN_CREATE_DIALOG', 'EXIT', {
      dialog_open: true
    });
  }
  // [END_TRACK_DETAIL_OPEN_CREATE_DIALOG]

  // [START_TRACK_DETAIL_CLOSE_CREATE_DIALOG]
  /*
   * ANCHOR: TRACK_DETAIL_CLOSE_CREATE_DIALOG
   * PURPOSE: Закрытие диалога создания заметки.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - диалог закрыт, форма очищена
   *
   * @Invariants:
   * - createForm всегда сбрасывается при закрытии диалога
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не очистить форму при закрытии
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение закрытия при несохранённых данных
   */
  closeCreateDialog(): void {
    logLine('ui', 'DEBUG', 'closeCreateDialog', 'TRACK_DETAIL_CLOSE_CREATE_DIALOG', 'ENTRY', {});

    this.showCreateDialog = false;
    this.createForm = { title: '', content: '', parentId: null };
    this.createError = '';

    logLine('ui', 'DEBUG', 'closeCreateDialog', 'TRACK_DETAIL_CLOSE_CREATE_DIALOG', 'EXIT', {
      dialog_open: false
    });
  }
  // [END_TRACK_DETAIL_CLOSE_CREATE_DIALOG]

  // [START_TRACK_DETAIL_CREATE_NOTE]
  /*
   * ANCHOR: TRACK_DETAIL_CREATE_NOTE
   * PURPOSE: Создание новой заметки.
   *
   * @PreConditions:
   * - createForm.title непустая строка
   *
   * @PostConditions:
   * - при успехе: заметка создана, диалог закрыт, заметки перезагружены
   * - при ошибке: createError содержит описание ошибки
   *
   * @Invariants:
   * - createLoading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP POST запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать перезагрузку заметок после создания
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей формы
   */
  createNote(): void {
    logLine('ui', 'DEBUG', 'createNote', 'TRACK_DETAIL_CREATE_NOTE', 'ENTRY', {
      title: this.createForm.title,
      parent_id: this.createForm.parentId
    });

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

        logLine('ui', 'INFO', 'createNote', 'TRACK_DETAIL_CREATE_NOTE', 'STATE_CHANGE', {
          entity: 'note',
          action: 'created',
          track_id: this.trackId
        });
      },
      error: () => {
        this.createError = 'Ошибка создания заметки';
        this.createLoading = false;

        logLine('ui', 'ERROR', 'createNote', 'TRACK_DETAIL_CREATE_NOTE', 'ERROR', {
          track_id: this.trackId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_CREATE_NOTE]

  // [START_TRACK_DETAIL_DELETE_NOTE]
  /*
   * ANCHOR: TRACK_DETAIL_DELETE_NOTE
   * PURPOSE: Удаление заметки с подтверждением.
   *
   * @PreConditions:
   * - noteId существует и пользователь имеет право EDIT
   *
   * @PostConditions:
   * - при подтверждении: заметка удалена, заметки перезагружены
   * - при отмене: ничего не происходит
   *
   * @Invariants:
   * - удаление требует подтверждения пользователя
   *
   * @SideEffects:
   * - HTTP DELETE запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать подтверждение удаления
   *
   * @AllowedRefactorZone:
   * - можно изменить текст подтверждения
   */
  deleteNote(noteId: number): void {
    logLine('ui', 'DEBUG', 'deleteNote', 'TRACK_DETAIL_DELETE_NOTE', 'ENTRY', {
      note_id: noteId
    });

    if (!confirm('Удалить заметку и все её подзаметки?')) {
      logLine('ui', 'DEBUG', 'deleteNote', 'TRACK_DETAIL_DELETE_NOTE', 'DECISION', {
        decision: 'cancel_delete'
      });
      return;
    }

    this.loading = true;
    this.noteService.deleteNote(noteId).subscribe({
      next: () => {
        if (this.selectedNote?.id === noteId) this.selectedNote = null;
        this.loadNotes();
        this.successMessage = 'Заметка удалена';
        this.loading = false;
        setTimeout(() => this.successMessage = '', 3000);

        logLine('ui', 'INFO', 'deleteNote', 'TRACK_DETAIL_DELETE_NOTE', 'STATE_CHANGE', {
          entity: 'note',
          action: 'deleted',
          note_id: noteId
        });
      },
      error: () => {
        this.errorMessage = 'Ошибка удаления заметки';
        this.loading = false;

        logLine('ui', 'ERROR', 'deleteNote', 'TRACK_DETAIL_DELETE_NOTE', 'ERROR', {
          note_id: noteId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_DELETE_NOTE]

  // [START_TRACK_DETAIL_TOGGLE_NOTE_COMPLETED]
  /*
   * ANCHOR: TRACK_DETAIL_TOGGLE_NOTE_COMPLETED
   * PURPOSE: Переключение статуса выполненности заметки.
   *
   * @PreConditions:
   * - noteId существует и пользователь имеет право EDIT
   *
   * @PostConditions:
   * - при успехе: статус completed заметки инвертирован, заметки перезагружены
   * - при ошибке: errorMessage содержит описание ошибки
   *
   * @Invariants:
   * - loading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP PATCH запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать перезагрузку заметок после переключения
   *
   * @AllowedRefactorZone:
   * - можно добавить анимацию переключения
   */
  toggleNoteCompleted(noteId: number, event: Event): void {
    event.stopPropagation(); // Предотвращаем выбор заметки при клике на checkbox

    logLine('ui', 'DEBUG', 'toggleNoteCompleted', 'TRACK_DETAIL_TOGGLE_NOTE_COMPLETED', 'ENTRY', {
      note_id: noteId
    });

    this.noteService.toggleNoteCompleted(noteId).subscribe({
      next: (updatedNote) => {
        // Обновляем заметку в дереве
        this.updateNoteInTree(this.notes, noteId, updatedNote);
        this.successMessage = updatedNote.completed ? 'Заметка отмечена как выполненная' : 'Статус выполненности снят';
        setTimeout(() => this.successMessage = '', 3000);

        logLine('ui', 'INFO', 'toggleNoteCompleted', 'TRACK_DETAIL_TOGGLE_NOTE_COMPLETED', 'STATE_CHANGE', {
          entity: 'note',
          action: 'toggled_completed',
          note_id: noteId,
          completed: updatedNote.completed
        });
      },
      error: () => {
        this.errorMessage = 'Ошибка переключения статуса заметки';

        logLine('ui', 'ERROR', 'toggleNoteCompleted', 'TRACK_DETAIL_TOGGLE_NOTE_COMPLETED', 'ERROR', {
          note_id: noteId
        });
      }
    });
  }

  // Вспомогательный метод для обновления заметки в дереве
  private updateNoteInTree(notes: NoteTreeNodeDto[], noteId: number, updatedNote: any): void {
    for (const note of notes) {
      if (note.id === noteId) {
        note.completed = updatedNote.completed;
        return;
      }
      if (note.children && note.children.length > 0) {
        this.updateNoteInTree(note.children, noteId, updatedNote);
      }
    }
  }
  // [END_TRACK_DETAIL_TOGGLE_NOTE_COMPLETED]

  // [START_TRACK_DETAIL_OPEN_SHARE_DIALOG]
  /*
   * ANCHOR: TRACK_DETAIL_OPEN_SHARE_DIALOG
   * PURPOSE: Открытие диалога предоставления доступа к треку.
   *
   * @PreConditions:
   * - пользователь имеет право OWNER на трек
   *
   * @PostConditions:
   * - диалог открыт, форма очищена
   *
   * @Invariants:
   * - shareForm всегда сбрасывается при открытии диалога
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не очистить форму при открытии
   *
   * @AllowedRefactorZone:
   * - можно добавить предзаполнение формы
   */
  openShareDialog(): void {
    logLine('ui', 'DEBUG', 'openShareDialog', 'TRACK_DETAIL_OPEN_SHARE_DIALOG', 'ENTRY', {
      track_id: this.trackId
    });

    this.showShareDialog = true;
    this.shareError = '';

    logLine('ui', 'DEBUG', 'openShareDialog', 'TRACK_DETAIL_OPEN_SHARE_DIALOG', 'EXIT', {
      dialog_open: true
    });
  }
  // [END_TRACK_DETAIL_OPEN_SHARE_DIALOG]

  // [START_TRACK_DETAIL_CLOSE_SHARE_DIALOG]
  /*
   * ANCHOR: TRACK_DETAIL_CLOSE_SHARE_DIALOG
   * PURPOSE: Закрытие диалога предоставления доступа.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - диалог закрыт, форма очищена
   *
   * @Invariants:
   * - shareForm всегда сбрасывается при закрытии диалога
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя не очистить форму при закрытии
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение закрытия при несохранённых данных
   */
  closeShareDialog(): void {
    logLine('ui', 'DEBUG', 'closeShareDialog', 'TRACK_DETAIL_CLOSE_SHARE_DIALOG', 'ENTRY', {});

    this.showShareDialog = false;
    this.shareForm = { username: '', permissionType: 'VIEW' };
    this.shareError = '';

    logLine('ui', 'DEBUG', 'closeShareDialog', 'TRACK_DETAIL_CLOSE_SHARE_DIALOG', 'EXIT', {
      dialog_open: false
    });
  }
  // [END_TRACK_DETAIL_CLOSE_SHARE_DIALOG]

  // [START_TRACK_DETAIL_SHARE_TRACK]
  /*
   * ANCHOR: TRACK_DETAIL_SHARE_TRACK
   * PURPOSE: Предоставление доступа к треку другому пользователю.
   *
   * @PreConditions:
   * - shareForm.username непустая строка
   * - пользователь имеет право OWNER на трек
   *
   * @PostConditions:
   * - при успехе: доступ предоставлён, диалог закрыт
   * - при ошибке: shareError содержит описание ошибки
   *
   * @Invariants:
   * - shareLoading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP POST запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать перезагрузку данных трека после предоставления доступа
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей формы
   */
  shareTrack(): void {
    logLine('ui', 'DEBUG', 'shareTrack', 'TRACK_DETAIL_SHARE_TRACK', 'ENTRY', {
      track_id: this.trackId,
      username: this.shareForm.username,
      permission_type: this.shareForm.permissionType
    });

    const request: GrantPermissionRequest = { username: this.shareForm.username, permissionType: this.shareForm.permissionType };
    this.shareLoading = true;
    this.shareError = '';
    this.trackService.grantPermission(this.trackId, request).subscribe({
      next: () => {
        this.closeShareDialog();
        this.successMessage = 'Доступ предоставлен';
        this.shareLoading = false;
        setTimeout(() => this.successMessage = '', 3000);

        logLine('ui', 'INFO', 'shareTrack', 'TRACK_DETAIL_SHARE_TRACK', 'STATE_CHANGE', {
          entity: 'permission',
          action: 'granted',
          track_id: this.trackId,
          username: this.shareForm.username
        });
      },
      error: () => {
        this.shareError = 'Ошибка предоставления доступа';
        this.shareLoading = false;

        logLine('ui', 'ERROR', 'shareTrack', 'TRACK_DETAIL_SHARE_TRACK', 'ERROR', {
          track_id: this.trackId,
          username: this.shareForm.username
        });
      }
    });
  }
  // [END_TRACK_DETAIL_SHARE_TRACK]

  // [START_TRACK_DETAIL_DELETE_TRACK]
  /*
   * ANCHOR: TRACK_DETAIL_DELETE_TRACK
   * PURPOSE: Удаление трека с подтверждением.
   *
   * @PreConditions:
   * - пользователь имеет право OWNER на трек
   *
   * @PostConditions:
   * - при подтверждении: трек удалён, переход на dashboard
   * - при отмене: ничего не происходит
   *
   * @Invariants:
   * - удаление требует подтверждения пользователя
   *
   * @SideEffects:
   * - HTTP DELETE запрос к API
   * - навигация на dashboard
   *
   * @ForbiddenChanges:
   * - нельзя убрать подтверждение удаления
   *
   * @AllowedRefactorZone:
   * - можно изменить текст подтверждения
   */
  deleteTrack(): void {
    logLine('ui', 'DEBUG', 'deleteTrack', 'TRACK_DETAIL_DELETE_TRACK', 'ENTRY', {
      track_id: this.trackId
    });

    if (!confirm('Удалить трек и все заметки?')) {
      logLine('ui', 'DEBUG', 'deleteTrack', 'TRACK_DETAIL_DELETE_TRACK', 'DECISION', {
        decision: 'cancel_delete'
      });
      return;
    }

    this.loading = true;
    this.trackService.deleteTrack(this.trackId).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);

        logLine('ui', 'INFO', 'deleteTrack', 'TRACK_DETAIL_DELETE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          action: 'deleted',
          track_id: this.trackId
        });
      },
      error: () => {
        this.errorMessage = 'Ошибка удаления трека';
        this.loading = false;

        logLine('ui', 'ERROR', 'deleteTrack', 'TRACK_DETAIL_DELETE_TRACK', 'ERROR', {
          track_id: this.trackId
        });
      }
    });
  }
  // [END_TRACK_DETAIL_DELETE_TRACK]

  // [START_TRACK_DETAIL_CAN_EDIT]
  /*
   * ANCHOR: TRACK_DETAIL_CAN_EDIT
   * PURPOSE: Проверка прав на редактирование трека.
   *
   * @PreConditions:
   * - track загружен
   *
   * @PostConditions:
   * - возвращается true если пользователь имеет право EDIT или OWNER
   *
   * @Invariants:
   * - результат зависит только от роли пользователя
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику проверки прав без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительные проверки
   */
  canEdit(): boolean {
    return this.track?.myRole === 'OWNER' || this.track?.myRole === 'EDIT';
  }
  // [END_TRACK_DETAIL_CAN_EDIT]

  // [START_TRACK_DETAIL_CAN_DELETE]
  /*
   * ANCHOR: TRACK_DETAIL_CAN_DELETE
   * PURPOSE: Проверка прав на удаление трека.
   *
   * @PreConditions:
   * - track загружен
   *
   * @PostConditions:
   * - возвращается true если пользователь имеет право OWNER
   *
   * @Invariants:
   * - результат зависит только от роли пользователя
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику проверки прав без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительные проверки
   */
  canDelete(): boolean {
    return this.track?.myRole === 'OWNER';
  }
  // [END_TRACK_DETAIL_CAN_DELETE]

  // [START_TRACK_DETAIL_CAN_SHARE]
  /*
   * ANCHOR: TRACK_DETAIL_CAN_SHARE
   * PURPOSE: Проверка прав на предоставление доступа к треку.
   *
   * @PreConditions:
   * - track загружен
   *
   * @PostConditions:
   * - возвращается true если пользователь имеет право OWNER
   *
   * @Invariants:
   * - результат зависит только от роли пользователя
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику проверки прав без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительные проверки
   */
  canShare(): boolean {
    return this.track?.myRole === 'OWNER';
  }
  // [END_TRACK_DETAIL_CAN_SHARE]

  // [START_TRACK_DETAIL_GET_ROLE_BADGE_CLASS]
  /*
   * ANCHOR: TRACK_DETAIL_GET_ROLE_BADGE_CLASS
   * PURPOSE: Получение CSS класса для бейджа роли.
   *
   * @PreConditions:
   * - role валидная строка роли
   *
   * @PostConditions:
   * - возвращается CSS класс для отображения роли
   *
   * @Invariants:
   * - для каждой роли возвращается уникальный класс
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить соответствие ролей и классов без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить новые роли
   */
  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'OWNER': return 'badge-owner';
      case 'EDIT': return 'badge-edit';
      case 'VIEW': return 'badge-view';
      default: return '';
    }
  }
  // [END_TRACK_DETAIL_GET_ROLE_BADGE_CLASS]

  // [START_TRACK_DETAIL_GET_ROLE_LABEL]
  /*
   * ANCHOR: TRACK_DETAIL_GET_ROLE_LABEL
   * PURPOSE: Получение текстовой метки для роли.
   *
   * @PreConditions:
   * - role валидная строка роли
   *
   * @PostConditions:
   * - возвращается текстовая метка для отображения роли
   *
   * @Invariants:
   * - для каждой роли возвращается уникальная метка
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить соответствие ролей и меток без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить новые роли
   */
  getRoleLabel(role: string): string {
    switch (role) {
      case 'OWNER': return 'Владелец';
      case 'EDIT': return 'Редактор';
      case 'VIEW': return 'Читатель';
      default: return role;
    }
  }
  // [END_TRACK_DETAIL_GET_ROLE_LABEL]

  // [START_TRACK_DETAIL_FORMAT_DATE]
  /*
   * ANCHOR: TRACK_DETAIL_FORMAT_DATE
   * PURPOSE: Форматирование даты в локальный формат.
   *
   * @PreConditions:
   * - date валидная строка даты в ISO формате
   *
   * @PostConditions:
   * - возвращается отформатированная строка даты
   *
   * @Invariants:
   * - формат даты всегда соответствует локали ru-RU
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить формат даты без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить поддержку других локалей
   */
  formatDate(date: string): string {
    const d = new Date(date);
    return d.toLocaleDateString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }
  // [END_TRACK_DETAIL_FORMAT_DATE]

  // [START_TRACK_DETAIL_GO_BACK]
  /*
   * ANCHOR: TRACK_DETAIL_GO_BACK
   * PURPOSE: Возврат на страницу dashboard.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - выполнен переход на /dashboard
   *
   * @Invariants:
   * - всегда выполняется навигация
   *
   * @SideEffects:
   * - навигация на dashboard
   *
   * @ForbiddenChanges:
   * - нельзя изменить маршрут без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку несохранённых изменений
   */
  goBack(): void {
    logLine('ui', 'DEBUG', 'goBack', 'TRACK_DETAIL_GO_BACK', 'ENTRY', {});

    this.router.navigate(['/dashboard']);

    logLine('ui', 'DEBUG', 'goBack', 'TRACK_DETAIL_GO_BACK', 'EXIT', {
      navigated_to: '/dashboard'
    });
  }
  // [END_TRACK_DETAIL_GO_BACK]
}
// [END_TRACK_DETAIL_COMPONENT]
// === END_CHUNK: TRACK_DETAIL_COMPONENT ===
