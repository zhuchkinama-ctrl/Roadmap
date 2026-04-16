// === CHUNK: NOTE_SERVICE [NOTE] ===
// Описание: Сервис управления заметками (CRUD операции) через API.
// Dependencies: Angular HttpClient, environment, shared models

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  NoteDto,
  NoteTreeNodeDto,
  CreateNoteRequest,
  UpdateNoteRequest,
  MoveNoteRequest
} from '../../shared/models';
import { logLine } from '../lib/log';

// [START_NOTE_SERVICE]
/*
 * ANCHOR: NOTE_SERVICE
 * PURPOSE: Управление заметками (CRUD операции) через API.
 *
 * @PreConditions:
 * - environment.apiUrl определён
 * - API поддерживает соответствующие эндпоинты
 * - пользователь аутентифицирован (токен добавляется через interceptor)
 *
 * @PostConditions:
 * - возвращаются Observable с данными или void
 *
 * @Invariants:
 * - токен аутентификации присутствует в запросах (через interceptor)
 *
 * @SideEffects:
 * - HTTP запросы к серверу
 *
 * @ForbiddenChanges:
 * - нельзя менять структуру URL без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление методов
 */
@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // [START_NOTE_GET_TREE]
  /*
   * ANCHOR: NOTE_GET_TREE
   * PURPOSE: Получение дерева заметок для указанного трека.
   *
   * @PreConditions:
   * - trackId существует и пользователь имеет доступ к треку
   *
   * @PostConditions:
   * - при успехе: возвращается дерево заметок в виде NoteTreeNodeDto[]
   * - при ошибке: выбрасывается HTTP ошибка (например, нет доступа)
   *
   * @Invariants:
   * - дерево всегда содержит все заметки трека с иерархической структурой
   *
   * @SideEffects:
   * - HTTP GET запрос к /tracks/{trackId}/notes/tree
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование дерева заметок
   */
  getNotesTree(trackId: number): Observable<NoteTreeNodeDto[]> {
    logLine('note', 'DEBUG', 'getNotesTree', 'NOTE_GET_TREE', 'ENTRY', { trackId });

    const result = this.http.get<NoteTreeNodeDto[]>(`${this.apiUrl}/tracks/${trackId}/notes/tree`);

    logLine('note', 'DEBUG', 'getNotesTree', 'NOTE_GET_TREE', 'EXIT', { trackId });
    return result;
  }
  // [END_NOTE_GET_TREE]

  // [START_NOTE_GET]
  /*
   * ANCHOR: NOTE_GET
   * PURPOSE: Получение заметки по ID.
   *
   * @PreConditions:
   * - id существует и пользователь имеет доступ к заметке
   *
   * @PostConditions:
   * - при успехе: возвращается NoteDto с данными заметки
   * - при ошибке: выбрасывается HTTP ошибка (например, заметка не найдена)
   *
   * @Invariants:
   * - возвращаемая заметка всегда имеет валидные данные
   *
   * @SideEffects:
   * - HTTP GET запрос к /notes/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование заметок
   */
  getNote(id: number): Observable<NoteDto> {
    logLine('note', 'DEBUG', 'getNote', 'NOTE_GET', 'ENTRY', { id });

    const result = this.http.get<NoteDto>(`${this.apiUrl}/notes/${id}`);

    logLine('note', 'DEBUG', 'getNote', 'NOTE_GET', 'EXIT', { id });
    return result;
  }
  // [END_NOTE_GET]

  // [START_NOTE_CREATE]
  /*
   * ANCHOR: NOTE_CREATE
   * PURPOSE: Создание новой заметки в указанном треке.
   *
   * @PreConditions:
   * - trackId существует и пользователь имеет право EDIT на трек
   * - request.title непустая строка
   * - request.parentId либо null, либо указывает на существующую заметку в том же треке
   *
   * @PostConditions:
   * - при успехе: возвращается NoteDto с данными созданной заметки
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - созданная заметка всегда принадлежит указанному треку
   *
   * @SideEffects:
   * - HTTP POST запрос к /tracks/{trackId}/notes
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  createNote(trackId: number, request: CreateNoteRequest): Observable<NoteDto> {
    logLine('note', 'DEBUG', 'createNote', 'NOTE_CREATE', 'ENTRY', {
      trackId,
      title: request.title,
      parent_id: request.parentId
    });

    const result = this.http.post<NoteDto>(`${this.apiUrl}/tracks/${trackId}/notes`, request);

    logLine('note', 'DEBUG', 'createNote', 'NOTE_CREATE', 'EXIT', { trackId });
    return result;
  }
  // [END_NOTE_CREATE]

  // [START_NOTE_UPDATE]
  /*
   * ANCHOR: NOTE_UPDATE
   * PURPOSE: Обновление существующей заметки.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право EDIT на заметку
   * - request содержит валидные данные для обновления
   *
   * @PostConditions:
   * - при успехе: возвращается NoteDto с обновлёнными данными
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - обновляются только указанные поля заметки
   *
   * @SideEffects:
   * - HTTP PUT запрос к /notes/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  updateNote(id: number, request: UpdateNoteRequest): Observable<NoteDto> {
    logLine('note', 'DEBUG', 'updateNote', 'NOTE_UPDATE', 'ENTRY', {
      id,
      title: request.title
    });

    const result = this.http.put<NoteDto>(`${this.apiUrl}/notes/${id}`, request);

    logLine('note', 'DEBUG', 'updateNote', 'NOTE_UPDATE', 'EXIT', { id });
    return result;
  }
  // [END_NOTE_UPDATE]

  // [START_NOTE_DELETE]
  /*
   * ANCHOR: NOTE_DELETE
   * PURPOSE: Удаление заметки по ID.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право EDIT на заметку
   *
   * @PostConditions:
   * - при успехе: заметка удалена из БД (включая все дочерние заметки)
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - удаление заметки приводит к каскадному удалению всех дочерних заметок
   *
   * @SideEffects:
   * - HTTP DELETE запрос к /notes/{id}
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение удаления
   */
  deleteNote(id: number): Observable<void> {
    logLine('note', 'DEBUG', 'deleteNote', 'NOTE_DELETE', 'ENTRY', { id });

    const result = this.http.delete<void>(`${this.apiUrl}/notes/${id}`);

    logLine('note', 'DEBUG', 'deleteNote', 'NOTE_DELETE', 'EXIT', { id });
    return result;
  }
  // [END_NOTE_DELETE]

  // [START_NOTE_MOVE]
  /*
   * ANCHOR: NOTE_MOVE
   * PURPOSE: Перемещение заметки в другую позицию в дереве.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право EDIT на заметку
   * - request.newParentId либо null, либо указывает на существующую заметку
   * - request.newIndex валидный индекс в целевом родителе
   *
   * @PostConditions:
   * - при успехе: заметка перемещена в новую позицию
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - заметка всегда остаётся в том же треке
   *
   * @SideEffects:
   * - HTTP PATCH запрос к /notes/{id}/move
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить валидацию полей request
   */
  moveNote(id: number, request: MoveNoteRequest): Observable<void> {
    logLine('note', 'DEBUG', 'moveNote', 'NOTE_MOVE', 'ENTRY', {
      id,
      new_parent_id: request.parentId,
      new_index: request.orderIndex
    });

    const result = this.http.patch<void>(`${this.apiUrl}/notes/${id}/move`, request);

    logLine('note', 'DEBUG', 'moveNote', 'NOTE_MOVE', 'EXIT', { id });
    return result;
  }
  // [END_NOTE_MOVE]

  // [START_NOTE_TOGGLE_COMPLETED]
  /*
   * ANCHOR: NOTE_TOGGLE_COMPLETED
   * PURPOSE: Переключение статуса выполненности заметки.
   *
   * @PreConditions:
   * - id существует и пользователь имеет право EDIT на заметку
   *
   * @PostConditions:
   * - при успехе: статус completed заметки инвертирован
   * - при ошибке: выбрасывается HTTP ошибка (например, нет прав)
   *
   * @Invariants:
   * - статус completed всегда инвертируется (true -> false, false -> true)
   *
   * @SideEffects:
   * - HTTP PATCH запрос к /notes/{id}/toggle-completed
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование статуса
   */
  toggleNoteCompleted(id: number): Observable<NoteDto> {
    logLine('note', 'DEBUG', 'toggleNoteCompleted', 'NOTE_TOGGLE_COMPLETED', 'ENTRY', { id });

    const result = this.http.patch<NoteDto>(`${this.apiUrl}/notes/${id}/toggle-completed`, {});

    logLine('note', 'DEBUG', 'toggleNoteCompleted', 'NOTE_TOGGLE_COMPLETED', 'EXIT', { id });
    return result;
  }
  // [END_NOTE_TOGGLE_COMPLETED]
}
// [END_NOTE_SERVICE]
// === END_CHUNK: NOTE_SERVICE ===
