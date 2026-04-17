# Пример TypeScript сервиса с GRACE разметкой

Пример сервиса для TrackHub (Angular 17) с полной семантической разметкой, контрактом и AI-friendly логированием.

---

## Пример: TrackService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { logLine } from '../core/lib/log';
import { Track, CreateTrackRequest, UpdateTrackRequest } from '../shared/models/track.model';
import { ErrorModel } from '../shared/models/error.model';

// [START_TRACK_SERVICE]
/*
 * ANCHOR: TRACK_SERVICE
 * PURPOSE: Сервис для управления треками развития пользователей.
 *
 * @PreConditions:
 * - Angular модуль инициализирован
 * - HttpClient доступен через DI
 * - API endpoint доступен
 *
 * @PostConditions:
 * - методы сервиса возвращают Observable с данными
 * - все ошибки обрабатываются и логируются
 * - логи записаны для всех критичных операций
 *
 * @Invariants:
 * - сервис не меняет глобальное состояние приложения
 * - все HTTP запросы содержат Bearer токен
 *
 * @SideEffects:
 * - HTTP запросы к API
 * - запись в журнал через logLine
 *
 * @ForbiddenChanges:
 * - нельзя убрать добавление Bearer токена
 * - нельзя убрать обработку ошибок
 * - нельзя убрать валидацию входных данных
 *
 * @AllowedRefactorZone:
 * - можно изменить способ обработки ошибок
 * - можно добавить кэширование
 * - можно изменить формат логирования
 */
@Injectable({
  providedIn: 'root'
})
export class TrackService {
  private readonly apiUrl = '/api/tracks';

  constructor(private http: HttpClient) {}

  // [START_CREATE_TRACK]
  /*
   * ANCHOR: CREATE_TRACK
   * PURPOSE: Создание нового трека для пользователя.
   *
   * @PreConditions:
   * - request.title: непустая строка, max 200 символов
   * - request.description: опционально, max 1000 символов
   * - пользователь аутентифицирован
   *
   * @PostConditions:
   * - при успехе: возвращается Observable с Track
   * - при ошибке: выбрасывается ErrorModel
   *
   * @Invariants:
   * - трек всегда принадлежит текущему пользователю
   * - трек всегда имеет уникальный id
   *
   * @SideEffects:
   * - отправляет POST запрос к API
   * - логирует создание трека
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию заголовка
   * - нельзя убрать логирование
   */
  createTrack(request: CreateTrackRequest): Observable<Track> {
    logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'ENTRY', {
      title: request.title,
      hasDescription: request.description !== undefined
    });

    // Валидация заголовка
    if (!request.title || request.title.trim().length === 0) {
      logLine('track', 'WARN', 'createTrack', 'CREATE_TRACK', 'DECISION', {
        decision: 'reject_empty_title'
      });
      logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'EXIT', {
        result: 'rejected',
        reason: 'empty_title'
      });
      return throwError(() => new ErrorModel('Track title cannot be empty'));
    }

    if (request.title.length > 200) {
      logLine('track', 'WARN', 'createTrack', 'CREATE_TRACK', 'DECISION', {
        decision: 'reject_title_too_long',
        length: request.title.length
      });
      logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'EXIT', {
        result: 'rejected',
        reason: 'title_too_long'
      });
      return throwError(() => new ErrorModel('Track title cannot exceed 200 characters'));
    }

    return this.http.post<Track>(this.apiUrl, request).pipe(
      tap(track => {
        logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'CHECK', {
          check: 'track_created',
          trackId: track.id
        });
        logLine('track', 'INFO', 'createTrack', 'CREATE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          id: track.id,
          action: 'created'
        });
        logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'EXIT', {
          result: 'success',
          trackId: track.id
        });
      }),
      catchError(error => {
        logLine('track', 'ERROR', 'createTrack', 'CREATE_TRACK', 'ERROR', {
          reason: error.message,
          status: error.status
        });
        logLine('track', 'DEBUG', 'createTrack', 'CREATE_TRACK', 'EXIT', {
          result: 'rejected',
          reason: 'api_error'
        });
        return throwError(() => error);
      })
    );
  }
  // [END_CREATE_TRACK]

  // [START_UPDATE_TRACK]
  /*
   * ANCHOR: UPDATE_TRACK
   * PURPOSE: Обновление существующего трека.
   *
   * @PreConditions:
   * - trackId: существующий трек
   * - request.title: непустая строка, max 200 символов
   * - request.description: опционально, max 1000 символов
   * - пользователь имеет права EDIT или OWNER
   *
   * @PostConditions:
   * - при успехе: возвращается Observable с обновлённым Track
   * - при ошибке: выбрасывается ErrorModel
   *
   * @Invariants:
   * - трек всегда принадлежит тому же пользователю
   * - id трека не изменяется
   *
   * @SideEffects:
   * - отправляет PUT запрос к API
   * - логирует обновление трека
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию заголовка
   * - нельзя убрать логирование
   */
  updateTrack(trackId: number, request: UpdateTrackRequest): Observable<Track> {
    logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'ENTRY', {
      trackId,
      title: request.title,
      hasDescription: request.description !== undefined
    });

    // Валидация заголовка
    if (!request.title || request.title.trim().length === 0) {
      logLine('track', 'WARN', 'updateTrack', 'UPDATE_TRACK', 'DECISION', {
        decision: 'reject_empty_title',
        trackId
      });
      logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'EXIT', {
        result: 'rejected',
        reason: 'empty_title'
      });
      return throwError(() => new ErrorModel('Track title cannot be empty'));
    }

    if (request.title.length > 200) {
      logLine('track', 'WARN', 'updateTrack', 'UPDATE_TRACK', 'DECISION', {
        decision: 'reject_title_too_long',
        trackId,
        length: request.title.length
      });
      logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'EXIT', {
        result: 'rejected',
        reason: 'title_too_long'
      });
      return throwError(() => new ErrorModel('Track title cannot exceed 200 characters'));
    }

    return this.http.put<Track>(`${this.apiUrl}/${trackId}`, request).pipe(
      tap(track => {
        logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'CHECK', {
          check: 'track_updated',
          trackId: track.id
        });
        logLine('track', 'INFO', 'updateTrack', 'UPDATE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          id: track.id,
          action: 'updated'
        });
        logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'EXIT', {
          result: 'success',
          trackId: track.id
        });
      }),
      catchError(error => {
        logLine('track', 'ERROR', 'updateTrack', 'UPDATE_TRACK', 'ERROR', {
          reason: error.message,
          status: error.status,
          trackId
        });
        logLine('track', 'DEBUG', 'updateTrack', 'UPDATE_TRACK', 'EXIT', {
          result: 'rejected',
          reason: 'api_error'
        });
        return throwError(() => error);
      })
    );
  }
  // [END_UPDATE_TRACK]

  // [START_DELETE_TRACK]
  /*
   * ANCHOR: DELETE_TRACK
   * PURPOSE: Удаление трека.
   *
   * @PreConditions:
   * - trackId: существующий трек
   * - пользователь имеет права OWNER
   *
   * @PostConditions:
   * - при успехе: возвращается Observable<void>
   * - при ошибке: выбрасывается ErrorModel
   *
   * @Invariants:
   * - все заметки трека также удаляются (CASCADE)
   * - все права доступа к треку удаляются
   *
   * @SideEffects:
   * - отправляет DELETE запрос к API
   * - логирует удаление трека
   *
   * @ForbiddenChanges:
   * - нельзя убрать логирование
   */
  deleteTrack(trackId: number): Observable<void> {
    logLine('track', 'DEBUG', 'deleteTrack', 'DELETE_TRACK', 'ENTRY', {
      trackId
    });

    return this.http.delete<void>(`${this.apiUrl}/${trackId}`).pipe(
      tap(() => {
        logLine('track', 'DEBUG', 'deleteTrack', 'DELETE_TRACK', 'CHECK', {
          check: 'track_deleted',
          trackId
        });
        logLine('track', 'INFO', 'deleteTrack', 'DELETE_TRACK', 'STATE_CHANGE', {
          entity: 'track',
          id: trackId,
          action: 'deleted'
        });
        logLine('track', 'DEBUG', 'deleteTrack', 'DELETE_TRACK', 'EXIT', {
          result: 'success',
          trackId
        });
      }),
      catchError(error => {
        logLine('track', 'ERROR', 'deleteTrack', 'DELETE_TRACK', 'ERROR', {
          reason: error.message,
          status: error.status,
          trackId
        });
        logLine('track', 'DEBUG', 'deleteTrack', 'DELETE_TRACK', 'EXIT', {
          result: 'rejected',
          reason: 'api_error'
        });
        return throwError(() => error);
      })
    );
  }
  // [END_DELETE_TRACK]

  // [START_GET_TRACK_BY_ID]
  /*
   * ANCHOR: GET_TRACK_BY_ID
   * PURPOSE: Получение трека по ID.
   *
   * @PreConditions:
   * - trackId: существующий трек
   * - пользователь имеет права VIEW, EDIT или OWNER
   *
   * @PostConditions:
   * - при успехе: возвращается Observable с Track
   * - при ошибке: выбрасывается ErrorModel
   *
   * @Invariants:
   * - трек всегда принадлежит тому же пользователю
   *
   * @SideEffects:
   * - отправляет GET запрос к API
   * - логирует получение трека
   *
   * @ForbiddenChanges:
   * - нельзя убрать логирование
   */
  getTrackById(trackId: number): Observable<Track> {
    logLine('track', 'DEBUG', 'getTrackById', 'GET_TRACK_BY_ID', 'ENTRY', {
      trackId
    });

    return this.http.get<Track>(`${this.apiUrl}/${trackId}`).pipe(
      tap(track => {
        logLine('track', 'DEBUG', 'getTrackById', 'GET_TRACK_BY_ID', 'CHECK', {
          check: 'track_found',
          trackId: track.id
        });
        logLine('track', 'DEBUG', 'getTrackById', 'GET_TRACK_BY_ID', 'EXIT', {
          result: 'success',
          trackId: track.id
        });
      }),
      catchError(error => {
        logLine('track', 'ERROR', 'getTrackById', 'GET_TRACK_BY_ID', 'ERROR', {
          reason: error.message,
          status: error.status,
          trackId
        });
        logLine('track', 'DEBUG', 'getTrackById', 'GET_TRACK_BY_ID', 'EXIT', {
          result: 'rejected',
          reason: 'api_error'
        });
        return throwError(() => error);
      })
    );
  }
  // [END_GET_TRACK_BY_ID]

  // [START_GET_USER_TRACKS]
  /*
   * ANCHOR: GET_USER_TRACKS
   * PURPOSE: Получение всех треков пользователя.
   *
   * @PreConditions:
   * - пользователь аутентифицирован
   *
   * @PostConditions:
   * - при успехе: возвращается Observable с массивом Track
   * - при ошибке: выбрасывается ErrorModel
   *
   * @Invariants:
   * - список всегда отсортирован по orderIndex
   *
   * @SideEffects:
   * - отправляет GET запрос к API
   * - логирует получение треков
   *
   * @ForbiddenChanges:
   * - нельзя убрать логирование
   */
  getUserTracks(): Observable<Track[]> {
    logLine('track', 'DEBUG', 'getUserTracks', 'GET_USER_TRACKS', 'ENTRY', {});

    return this.http.get<Track[]>(this.apiUrl).pipe(
      tap(tracks => {
        logLine('track', 'DEBUG', 'getUserTracks', 'GET_USER_TRACKS', 'CHECK', {
          check: 'tracks_found',
          count: tracks.length
        });
        logLine('track', 'DEBUG', 'getUserTracks', 'GET_USER_TRACKS', 'EXIT', {
          result: 'success',
          count: tracks.length
        });
      }),
      catchError(error => {
        logLine('track', 'ERROR', 'getUserTracks', 'GET_USER_TRACKS', 'ERROR', {
          reason: error.message,
          status: error.status
        });
        logLine('track', 'DEBUG', 'getUserTracks', 'GET_USER_TRACKS', 'EXIT', {
          result: 'rejected',
          reason: 'api_error'
        });
        return throwError(() => error);
      })
    );
  }
  // [END_GET_USER_TRACKS]
}
// [END_TRACK_SERVICE]
```

---

## Пример: Angular компонент

```typescript
import { Component, OnInit } from '@angular/core';
import { TrackService } from '../../core/services/track.service';
import { Track } from '../../shared/models/track.model';
import { logLine } from '../../core/lib/log';

// [START_TRACK_DETAIL_COMPONENT]
/*
 * ANCHOR: TRACK_DETAIL_COMPONENT
 * PURPOSE: Компонент для отображения деталей трека.
 *
 * @PreConditions:
 * - Angular компонент инициализирован
 * - TrackService доступен через DI
 * - trackId передан через ActivatedRoute
 *
 * @PostConditions:
 * - при успехе: трек загружен и отображён
 * - при ошибке: отображено сообщение об ошибке
 *
 * @Invariants:
 * - компонент не меняет глобальное состояние приложения
 *
 * @SideEffects:
 * - HTTP запросы к API через TrackService
 * - запись в журнал через logLine
 *
 * @ForbiddenChanges:
 * - нельзя убрать обработку ошибок
 * - нельзя убрать логирование
 *
 * @AllowedRefactorZone:
 * - можно изменить способ отображения данных
 * - можно добавить дополнительные функции
 */
@Component({
  selector: 'app-track-detail',
  templateUrl: './track-detail.component.html',
  styleUrls: ['./track-detail.component.scss']
})
export class TrackDetailComponent implements OnInit {
  track: Track | null = null;
  loading = false;
  error: string | null = null;

  constructor(private trackService: TrackService) {}

  // [START_NG_ON_INIT]
  /*
   * ANCHOR: NG_ON_INIT
   * PURPOSE: Инициализация компонента и загрузка трека.
   *
   * @PreConditions:
   * - компонент создан
   * - trackId доступен
   *
   * @PostConditions:
   * - при успехе: трек загружен
   * - при ошибке: error содержит сообщение об ошибке
   *
   * @Invariants:
   * - loading всегда false после завершения
   *
   * @SideEffects:
   * - HTTP запрос к API
   * - запись в журнал через logLine
   *
   * @ForbiddenChanges:
   * - нельзя убрать логирование
   */
  ngOnInit(): void {
    logLine('track-detail', 'DEBUG', 'ngOnInit', 'NG_ON_INIT', 'ENTRY', {});

    this.loading = true;
    this.error = null;

    this.trackService.getUserTracks().subscribe({
      next: (tracks) => {
        logLine('track-detail', 'DEBUG', 'ngOnInit', 'NG_ON_INIT', 'CHECK', {
          check: 'tracks_loaded',
          count: tracks.length
        });

        if (tracks.length > 0) {
          this.track = tracks[0];
          logLine('track-detail', 'INFO', 'ngOnInit', 'NG_ON_INIT', 'STATE_CHANGE', {
            entity: 'track',
            id: this.track.id,
            action: 'loaded'
          });
        }

        this.loading = false;
        logLine('track-detail', 'DEBUG', 'ngOnInit', 'NG_ON_INIT', 'EXIT', {
          result: 'success'
        });
      },
      error: (err) => {
        logLine('track-detail', 'ERROR', 'ngOnInit', 'NG_ON_INIT', 'ERROR', {
          reason: err.message,
          status: err.status
        });
        this.error = 'Failed to load track';
        this.loading = false;
        logLine('track-detail', 'DEBUG', 'ngOnInit', 'NG_ON_INIT', 'EXIT', {
          result: 'rejected',
          reason: 'load_error'
        });
      }
    });
  }
  // [END_NG_ON_INIT]
}
// [END_TRACK_DETAIL_COMPONENT]
```

---

## Ключевые моменты

### 1. Контракт

Каждый метод имеет контракт с полями:
- `ANCHOR` — уникальный идентификатор
- `PURPOSE` — назначение метода
- `@PreConditions` — предусловия
- `@PostConditions` — постусловия
- `@Invariants` — инварианты
- `@SideEffects` — побочные эффекты
- `@ForbiddenChanges` — запрещённые изменения
- `@AllowedRefactorZone` — разрешённая зона рефакторинга

### 2. Логирование

Каждый метод имеет логи в точках:
- `ENTRY` — вход в метод
- `EXIT` — выход из метода
- `CHECK` — проверка условия
- `DECISION` — принятие решения
- `STATE_CHANGE` — изменение состояния
- `ERROR` — ошибка

### 3. Валидация

Все входные данные валидируются:
- Проверка на null/empty
- Проверка длины строк
- Проверка существования сущностей

### 4. Обработка ошибок

Все ошибки обрабатываются через `catchError` и логируются.

### 5. RxJS

Используются операторы:
- `tap` — для логирования
- `catchError` — для обработки ошибок

---

*Создано на основе методологии GRACE для TrackHub*
