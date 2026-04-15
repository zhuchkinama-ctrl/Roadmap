// === CHUNK: DASHBOARD_COMPONENT [UI] ===
// Описание: Компонент для отображения списка треков и управления пагинацией/фильтрацией.
// Dependencies: Angular Core, Common, Forms, Router, TrackService, shared models

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TrackService } from '../../core/services/track.service';
import { TrackSummaryDto, CreateTrackRequest, TrackRole } from '../../shared/models';
import { logLine } from '../../core/lib/log';

// [START_DASHBOARD_COMPONENT]
/*
 * ANCHOR: DASHBOARD_COMPONENT
 * PURPOSE: Отображение списка треков и управление их пагинацией/фильтрацией.
 *
 * @PreConditions:
 * - TrackService доступен через DI
 * - пользователь аутентифицирован
 *
 * @PostConditions:
 * - при успешной загрузке tracks заполнены, UI обновлён
 *
 * @Invariants:
 * - tracks всегда массив TrackSummaryDto
 *
 * @SideEffects:
 * - HTTP запросы к API
 * - навигация между страницами
 *
 * @ForbiddenChanges:
 * - нельзя менять логику пагинации без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода
 */
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

  // [START_DASHBOARD_ON_INIT]
  /*
   * ANCHOR: DASHBOARD_ON_INIT
   * PURPOSE: Инициализация компонента и загрузка треков.
   *
   * @PreConditions:
   * - компонент создан Angular фреймворком
   *
   * @PostConditions:
   * - треки загружены с сервера
   *
   * @Invariants:
   * - метод вызывается автоматически при создании компонента
   *
   * @SideEffects:
   * - HTTP запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать вызов loadTracks()
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную инициализацию
   */
  ngOnInit(): void {
    logLine('ui', 'DEBUG', 'ngOnInit', 'DASHBOARD_ON_INIT', 'ENTRY', {});
    this.loadTracks();
    logLine('ui', 'DEBUG', 'ngOnInit', 'DASHBOARD_ON_INIT', 'EXIT', {});
  }
  // [END_DASHBOARD_ON_INIT]

  // [START_DASHBOARD_LOAD_TRACKS]
  /*
   * ANCHOR: DASHBOARD_LOAD_TRACKS
   * PURPOSE: Загрузка треков с сервера с учётом пагинации.
   *
   * @PreConditions:
   * - page >= 0
   * - size > 0
   *
   * @PostConditions:
   * - при успехе: tracks заполнены данными, пагинация обновлена
   * - при ошибке: errorMessage содержит описание ошибки
   *
   * @Invariants:
   * - loading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP GET запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя изменить параметры сортировки без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить фильтрацию по параметрам
   */
  loadTracks(): void {
    logLine('ui', 'DEBUG', 'loadTracks', 'DASHBOARD_LOAD_TRACKS', 'ENTRY', {
      page: this.page,
      size: this.size,
      filter: this.filter
    });

    this.loading = true;
    this.errorMessage = '';

    this.trackService.getTracks(this.page, this.size, 'updatedAt,desc').subscribe({
      next: (response) => {
        this.tracks = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;

        logLine('ui', 'DEBUG', 'loadTracks', 'DASHBOARD_LOAD_TRACKS', 'EXIT', {
          tracks_count: this.tracks.length,
          total_elements: this.totalElements
        });
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Ошибка загрузки треков';
        this.loading = false;

        logLine('ui', 'ERROR', 'loadTracks', 'DASHBOARD_LOAD_TRACKS', 'ERROR', {
          error: this.errorMessage
        });
      }
    });
  }
  // [END_DASHBOARD_LOAD_TRACKS]

  // [START_DASHBOARD_ON_PAGE_CHANGE]
  /*
   * ANCHOR: DASHBOARD_ON_PAGE_CHANGE
   * PURPOSE: Обработка изменения страницы пагинации.
   *
   * @PreConditions:
   * - newPage валидный номер страницы (0 <= newPage < totalPages)
   *
   * @PostConditions:
   * - page обновлён, треки перезагружены
   *
   * @Invariants:
   * - всегда вызывается loadTracks() после изменения страницы
   *
   * @SideEffects:
   * - HTTP запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать вызов loadTracks()
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию номера страницы
   */
  onPageChange(newPage: number): void {
    logLine('ui', 'DEBUG', 'onPageChange', 'DASHBOARD_ON_PAGE_CHANGE', 'ENTRY', {
      old_page: this.page,
      new_page: newPage
    });

    this.page = newPage;
    this.loadTracks();

    logLine('ui', 'DEBUG', 'onPageChange', 'DASHBOARD_ON_PAGE_CHANGE', 'EXIT', {
      page: this.page
    });
  }
  // [END_DASHBOARD_ON_PAGE_CHANGE]

  // [START_DASHBOARD_ON_SIZE_CHANGE]
  /*
   * ANCHOR: DASHBOARD_ON_SIZE_CHANGE
   * PURPOSE: Обработка изменения размера страницы.
   *
   * @PreConditions:
   * - newSize > 0
   *
   * @PostConditions:
   * - size обновлён, page сброшен в 0, треки перезагружены
   *
   * @Invariants:
   * - всегда вызывается loadTracks() после изменения размера
   *
   * @SideEffects:
   * - HTTP запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать сброс page в 0
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию размера страницы
   */
  onSizeChange(newSize: number): void {
    logLine('ui', 'DEBUG', 'onSizeChange', 'DASHBOARD_ON_SIZE_CHANGE', 'ENTRY', {
      old_size: this.size,
      new_size: newSize
    });

    this.size = newSize;
    this.page = 0;
    this.loadTracks();

    logLine('ui', 'DEBUG', 'onSizeChange', 'DASHBOARD_ON_SIZE_CHANGE', 'EXIT', {
      size: this.size,
      page: this.page
    });
  }
  // [END_DASHBOARD_ON_SIZE_CHANGE]

  // [START_DASHBOARD_ON_FILTER_CHANGE]
  /*
   * ANCHOR: DASHBOARD_ON_FILTER_CHANGE
   * PURPOSE: Обработка изменения фильтра треков.
   *
   * @PreConditions:
   * - newFilter одно из: 'all', 'owned', 'shared'
   *
   * @PostConditions:
   * - filter обновлён, page сброшен в 0, треки перезагружены
   *
   * @Invariants:
   * - всегда вызывается loadTracks() после изменения фильтра
   *
   * @SideEffects:
   * - HTTP запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать сброс page в 0
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительные фильтры
   */
  onFilterChange(newFilter: 'all' | 'owned' | 'shared'): void {
    logLine('ui', 'DEBUG', 'onFilterChange', 'DASHBOARD_ON_FILTER_CHANGE', 'ENTRY', {
      old_filter: this.filter,
      new_filter: newFilter
    });

    this.filter = newFilter;
    this.page = 0;
    this.loadTracks();

    logLine('ui', 'DEBUG', 'onFilterChange', 'DASHBOARD_ON_FILTER_CHANGE', 'EXIT', {
      filter: this.filter,
      page: this.page
    });
  }
  // [END_DASHBOARD_ON_FILTER_CHANGE]

  // [START_DASHBOARD_OPEN_CREATE_DIALOG]
  /*
   * ANCHOR: DASHBOARD_OPEN_CREATE_DIALOG
   * PURPOSE: Открытие диалога создания трека.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - диалог открыт, форма очищена
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
  openCreateDialog(): void {
    logLine('ui', 'DEBUG', 'openCreateDialog', 'DASHBOARD_OPEN_CREATE_DIALOG', 'ENTRY', {});

    this.showCreateDialog = true;
    this.createForm = { title: '', description: '' };
    this.createError = '';

    logLine('ui', 'DEBUG', 'openCreateDialog', 'DASHBOARD_OPEN_CREATE_DIALOG', 'EXIT', {
      dialog_open: true
    });
  }
  // [END_DASHBOARD_OPEN_CREATE_DIALOG]

  // [START_DASHBOARD_CLOSE_CREATE_DIALOG]
  /*
   * ANCHOR: DASHBOARD_CLOSE_CREATE_DIALOG
   * PURPOSE: Закрытие диалога создания трека.
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
    logLine('ui', 'DEBUG', 'closeCreateDialog', 'DASHBOARD_CLOSE_CREATE_DIALOG', 'ENTRY', {});

    this.showCreateDialog = false;
    this.createForm = { title: '', description: '' };
    this.createError = '';

    logLine('ui', 'DEBUG', 'closeCreateDialog', 'DASHBOARD_CLOSE_CREATE_DIALOG', 'EXIT', {
      dialog_open: false
    });
  }
  // [END_DASHBOARD_CLOSE_CREATE_DIALOG]

  // [START_DASHBOARD_CREATE_TRACK]
  /*
   * ANCHOR: DASHBOARD_CREATE_TRACK
   * PURPOSE: Создание нового трека.
   *
   * @PreConditions:
   * - createForm.title непустая строка после trim
   *
   * @PostConditions:
   * - при успехе: трек создан, диалог закрыт, список треков перезагружен
   * - при ошибке: createError содержит описание ошибки
   *
   * @Invariants:
   * - createLoading сбрасывается в false после завершения запроса
   *
   * @SideEffects:
   * - HTTP POST запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию названия
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную валидацию полей
   */
  createTrack(): void {
    logLine('ui', 'DEBUG', 'createTrack', 'DASHBOARD_CREATE_TRACK', 'ENTRY', {
      title: this.createForm.title
    });

    if (!this.createForm.title.trim()) {
      this.createError = 'Название трека обязательно';

      logLine('ui', 'WARN', 'createTrack', 'DASHBOARD_CREATE_TRACK', 'DECISION', {
        decision: 'reject_empty_title'
      });

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

        logLine('ui', 'INFO', 'createTrack', 'DASHBOARD_CREATE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          action: 'created',
          track_id: track.id
        });
      },
      error: (error) => {
        this.createError = error.error?.message || 'Ошибка создания трека';
        this.createLoading = false;

        logLine('ui', 'ERROR', 'createTrack', 'DASHBOARD_CREATE_TRACK', 'ERROR', {
          error: this.createError
        });
      }
    });
  }
  // [END_DASHBOARD_CREATE_TRACK]

  // [START_DASHBOARD_OPEN_TRACK]
  /*
   * ANCHOR: DASHBOARD_OPEN_TRACK
   * PURPOSE: Открытие страницы детализации трека.
   *
   * @PreConditions:
   * - trackId существует и пользователь имеет доступ к треку
   *
   * @PostConditions:
   * - выполнен переход на страницу /tracks/{trackId}
   *
   * @Invariants:
   * - всегда выполняется навигация
   *
   * @SideEffects:
   * - навигация на новую страницу
   *
   * @ForbiddenChanges:
   * - нельзя изменить маршрут без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку прав доступа
   */
  openTrack(trackId: number): void {
    logLine('ui', 'DEBUG', 'openTrack', 'DASHBOARD_OPEN_TRACK', 'ENTRY', {
      track_id: trackId
    });

    this.router.navigate(['/tracks', trackId]);

    logLine('ui', 'DEBUG', 'openTrack', 'DASHBOARD_OPEN_TRACK', 'EXIT', {
      track_id: trackId
    });
  }
  // [END_DASHBOARD_OPEN_TRACK]

  // [START_DASHBOARD_CAN_EDIT_TRACK]
  /*
   * ANCHOR: DASHBOARD_CAN_EDIT_TRACK
   * PURPOSE: Проверка прав на редактирование трека.
   *
   * @PreConditions:
   * - track валидный объект TrackSummaryDto
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
  canEditTrack(track: TrackSummaryDto): boolean {
    return track.myRole === 'OWNER' || track.myRole === 'EDIT';
  }
  // [END_DASHBOARD_CAN_EDIT_TRACK]

  // [START_DASHBOARD_CAN_DELETE_TRACK]
  /*
   * ANCHOR: DASHBOARD_CAN_DELETE_TRACK
   * PURPOSE: Проверка прав на удаление трека.
   *
   * @PreConditions:
   * - track валидный объект TrackSummaryDto
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
  canDeleteTrack(track: TrackSummaryDto): boolean {
    return track.myRole === 'OWNER';
  }
  // [END_DASHBOARD_CAN_DELETE_TRACK]

  // [START_DASHBOARD_DELETE_TRACK]
  /*
   * ANCHOR: DASHBOARD_DELETE_TRACK
   * PURPOSE: Удаление трека с подтверждением.
   *
   * @PreConditions:
   * - trackId существует и пользователь имеет право OWNER
   *
   * @PostConditions:
   * - при подтверждении: трек удалён, список перезагружен
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
  deleteTrack(trackId: number): void {
    logLine('ui', 'DEBUG', 'deleteTrack', 'DASHBOARD_DELETE_TRACK', 'ENTRY', {
      track_id: trackId
    });

    if (!confirm('Вы уверены, что хотите удалить этот трек? Все заметки будут удалены.')) {
      logLine('ui', 'DEBUG', 'deleteTrack', 'DASHBOARD_DELETE_TRACK', 'DECISION', {
        decision: 'cancel_delete'
      });
      return;
    }

    this.trackService.deleteTrack(trackId).subscribe({
      next: () => {
        this.loadTracks();

        logLine('ui', 'INFO', 'deleteTrack', 'DASHBOARD_DELETE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          action: 'deleted',
          track_id: trackId
        });
      },
      error: (error) => {
        alert(error.error?.message || 'Ошибка удаления трека');

        logLine('ui', 'ERROR', 'deleteTrack', 'DASHBOARD_DELETE_TRACK', 'ERROR', {
          track_id: trackId,
          error: error.error?.message
        });
      }
    });
  }
  // [END_DASHBOARD_DELETE_TRACK]

  // [START_DASHBOARD_GET_ROLE_BADGE_CLASS]
  /*
   * ANCHOR: DASHBOARD_GET_ROLE_BADGE_CLASS
   * PURPOSE: Получение CSS класса для бейджа роли.
   *
   * @PreConditions:
   * - role валидное значение TrackRole
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
  // [END_DASHBOARD_GET_ROLE_BADGE_CLASS]

  // [START_DASHBOARD_GET_ROLE_LABEL]
  /*
   * ANCHOR: DASHBOARD_GET_ROLE_LABEL
   * PURPOSE: Получение текстовой метки для роли.
   *
   * @PreConditions:
   * - role валидное значение TrackRole
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
  // [END_DASHBOARD_GET_ROLE_LABEL]

  // [START_DASHBOARD_FORMAT_DATE]
  /*
   * ANCHOR: DASHBOARD_FORMAT_DATE
   * PURPOSE: Форматирование даты в локальный формат.
   *
   * @PreConditions:
   * - dateString валидная строка даты в ISO формате
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
  // [END_DASHBOARD_FORMAT_DATE]
}
// [END_DASHBOARD_COMPONENT]
// === END_CHUNK: DASHBOARD_COMPONENT ===
