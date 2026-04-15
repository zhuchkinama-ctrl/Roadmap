// === CHUNK: TRACK_SERVICE [TRACK] ===
// Описание: Сервис управления треками (CRUD и разрешения) через API.
// Dependencies: Angular HttpClient, HttpParams, environment, shared models

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TrackDto,
  TrackSummaryDto,
  TrackPageResponse,
  CreateTrackRequest,
  UpdateTrackRequest,
  GrantPermissionRequest,
  PermissionDto
} from '../../shared/models';
import { logLine } from '../lib/log';

// [START_TRACK_SERVICE]
/*
 * ANCHOR: TRACK_SERVICE
 * PURPOSE: Управление треками (CRUD и разрешения) через API.
 *
 * @PreConditions:
 * - environment.apiUrl определён
 * - API поддерживает эндпоинты /tracks и /permissions
 * - пользователь аутентифицирован (токен добавляется через interceptor)
 *
 * @PostConditions:
 * - возвращаются Observable с соответствующими DTO
 *
 * @Invariants:
 * - токен аутентификации передаётся через interceptor
 *
 * @SideEffects:
 * - HTTP запросы к серверу
 *
 * @ForbiddenChanges:
 * - нельзя менять URL без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление методов
 */
@Injectable({
  providedIn: 'root'
})
export class TrackService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // [START_TRACK_GET_ALL]
  /*
   * ANCHOR: TRACK_GET_ALL
   * PURPOSE: Получение списка треков с пагинацией и сортировкой.
   *
   * @PreConditions:
   * - page >= 0
   * - size > 0
   * - sort валидная строка сортировки (например, 'updatedAt,desc')
   *
   * @PostConditions:
   * - при успехе: возвращается TrackPageResponse с данными треков
   * - при ошибке: выбрасывается HTTP ошибка
   *
   * @Invariants:
   * - возвращаются только треки, к которым пользователь имеет доступ
   *
   * @SideEffects:
   * - HTTP GET запрос к /tracks с параметрами пагинации
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить фильтрацию по параметрам
   */
  getTracks(page: number = 0, size: number = 10, sort: string = 'updatedAt,desc'): Observable<TrackPageResponse> {
    logLine('track', 'DEBUG', 'getTracks', 'TRACK_GET_ALL', 'ENTRY', { page, size, sort });

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    const result = this.http.get<TrackPageResponse>(`${this.apiUrl}/tracks`, { params });

    logLine('track', 'DEBUG', 'getTracks', 'TRACK_GET_ALL', 'EXIT', { page, size });
    return result;
  }
  // [END_TRACK_GET_ALL]

  // [START_TRACK_GET]
  /*
   * ANCHOR: TRACK_GET
   * PURPOSE: Получение трека по ID.
   *
   * @PreConditions:
   * - id существует и пользователь имеет доступ к треку
   *
   * @PostConditions:
   * - при успехе: возвращается TrackDto с данными трека
   * - при ошибке: выбрасывается HTTP ошибка (например, трек не найден)
   *
   * @Invariants:
   * - возвращаемый трек всегда имеет валидные данные
   *
   * @SideEffects:
   * - HTTP GET запрос к /tracks/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование треков
   */
  getTrack(id: number): Observable<TrackDto> {
    logLine('track', 'DEBUG', 'getTrack', 'TRACK_GET', 'ENTRY', { id });

    const result = this.http.get<TrackDto>(`${this.apiUrl}/tracks/${id}`);

    logLine('track', 'DEBUG', 'getTrack', 'TRACK_GET', 'EXIT', { id });
    return result;
  }
  // [END_TRACK_GET]

  // [START_TRACK_CREATE]
  /*
   * ANCHOR: TRACK_CREATE
   * PURPOSE: Создание нового трека.
   *
   * @PreConditions:
   * - request.title непустая строка
   * - request.description может быть пустым
   *
   * @PostConditions:
   * - при успехе: возвращается TrackDto с данными созданного трека
   * - при ошибке: выбрасывается HTTP ошибка
   *
   * @Invariants:
   * - созданный трек принадлежит текущему пользователю (OWNER)
   *
   * @SideEffects:
   * - HTTP POST запрос к /tracks
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  createTrack(request: CreateTrackRequest): Observable<TrackDto> {
    logLine('track', 'DEBUG', 'createTrack', 'TRACK_CREATE', 'ENTRY', {
      title: request.title
    });

    const result = this.http.post<TrackDto>(`${this.apiUrl}/tracks`, request);

    logLine('track', 'DEBUG', 'createTrack', 'TRACK_CREATE', 'EXIT', {});
    return result;
  }
  // [END_TRACK_CREATE]

  // [START_TRACK_UPDATE]
  /*
   * ANCHOR: TRACK_UPDATE
   * PURPOSE: Обновление существующего трека.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право EDIT на трек
   * - request содержит валидные данные для обновления
   *
   * @PostConditions:
   * - при успехе: возвращается TrackDto с обновлёнными данными
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - обновляются только указанные поля трека
   *
   * @SideEffects:
   * - HTTP PUT запрос к /tracks/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  updateTrack(id: number, request: UpdateTrackRequest): Observable<TrackDto> {
    logLine('track', 'DEBUG', 'updateTrack', 'TRACK_UPDATE', 'ENTRY', {
      id,
      title: request.title
    });

    const result = this.http.put<TrackDto>(`${this.apiUrl}/tracks/${id}`, request);

    logLine('track', 'DEBUG', 'updateTrack', 'TRACK_UPDATE', 'EXIT', { id });
    return result;
  }
  // [END_TRACK_UPDATE]

  // [START_TRACK_DELETE]
  /*
   * ANCHOR: TRACK_DELETE
   * PURPOSE: Удаление трека по ID.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право OWNER на трек
   *
   * @PostConditions:
   * - при успехе: трек удалён из БД (включая все заметки и разрешения)
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - удаление трека приводит к каскадному удалению всех заметок и разрешений
   *
   * @SideEffects:
   * - HTTP DELETE запрос к /tracks/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение удаления
   */
  deleteTrack(id: number): Observable<void> {
    logLine('track', 'DEBUG', 'deleteTrack', 'TRACK_DELETE', 'ENTRY', { id });

    const result = this.http.delete<void>(`${this.apiUrl}/tracks/${id}`);

    logLine('track', 'DEBUG', 'deleteTrack', 'TRACK_DELETE', 'EXIT', { id });
    return result;
  }
  // [END_TRACK_DELETE]

  // [START_TRACK_GRANT_PERMISSION]
  /*
   * ANCHOR: TRACK_GRANT_PERMISSION
   * PURPOSE: Предоставление доступа к треку другому пользователю.
   *
   * @PreConditions:
   * - trackId существует и пользователь имеет право OWNER на трек
   * - request.username указывает на существующего пользователя
   * - request.role одно из: VIEW, EDIT
   *
   * @PostConditions:
   * - при успехе: возвращается PermissionDto с данными созданного разрешения
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав или пользователь не найден)
   *
   * @Invariants:
   * - пользователь не может иметь более одного разрешения на один трек
   *
   * @SideEffects:
   * - HTTP POST запрос к /tracks/{trackId}/permissions
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  grantPermission(trackId: number, request: GrantPermissionRequest): Observable<PermissionDto> {
    logLine('track', 'DEBUG', 'grantPermission', 'TRACK_GRANT_PERMISSION', 'ENTRY', {
      trackId,
      username: request.username,
      permission_type: request.permissionType
    });

    const result = this.http.post<PermissionDto>(`${this.apiUrl}/tracks/${trackId}/permissions`, request);

    logLine('track', 'DEBUG', 'grantPermission', 'TRACK_GRANT_PERMISSION', 'EXIT', { trackId });
    return result;
  }
  // [END_TRACK_GRANT_PERMISSION]
}
// [END_TRACK_SERVICE]
// === END_CHUNK: TRACK_SERVICE ===

