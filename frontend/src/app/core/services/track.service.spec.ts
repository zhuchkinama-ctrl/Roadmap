/**
 * Юнит-тесты для TrackService.
 */

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TrackService } from './track.service';
import { TrackSummaryDto, TrackDto, CreateTrackRequest, UpdateTrackRequest, TrackPageResponse } from '../../shared/models';
import { logLine } from '../lib/log';

// === CHUNK: TRACK_SERVICE_SPEC [TEST] ===
// Описание: Юнит-тесты для TrackService.
// Dependencies: Angular Core, HttpClientTestingModule, shared models

// [START_TRACK_SERVICE_SPEC]
/*
 * ANCHOR: TRACK_SERVICE_SPEC
 * PURPOSE: Юнит-тесты для TrackService.
 *
 * @PreConditions:
 * - TrackService инициализирован с HttpTestingController
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов TrackService
 *
 * @Invariants:
 * - все HTTP запросы отправляются с правильными заголовками
 * - ошибки обрабатываются корректно
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку HTTP ответов
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
describe('TrackService', () => {
  let service: TrackService;
  let httpMock: HttpTestingController;

  const mockTrackSummaryDto: TrackSummaryDto = {
    id: 1,
    title: 'Test Track',
    description: 'Test Description',
    owner: {
      id: 1,
      username: 'testuser',
      role: 'USER',
      enabled: true,
      createdAt: new Date()
    },
    myRole: 'OWNER',
    updatedAt: new Date()
  };

  const mockTrackDto: TrackDto = {
    id: 1,
    title: 'Test Track',
    description: 'Test Description',
    owner: {
      id: 1,
      username: 'testuser',
      role: 'USER',
      enabled: true,
      createdAt: new Date()
    },
    myRole: 'OWNER',
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockTrackPageResponse: TrackPageResponse = {
    content: [mockTrackSummaryDto],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TrackService]
    });

    service = TestBed.inject(TrackService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // [START_TRACK_SERVICE_SPEC_GET_TRACKS_SUCCESS]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_GET_TRACKS_SUCCESS
   * PURPOSE: Проверка получения списка треков.
   *
   * @PreConditions:
   * - сервис инициализирован
   *
   * @PostConditions:
   * - возвращается Observable с TrackPageResponse
   *
   * @Invariants:
   * - HTTP GET запрос отправляется на правильный эндпоинт
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку параметров запроса
   */
  it('getTracks should return TrackPageResponse', (done) => {
    // Arrange
    const page = 0;
    const size = 10;
    const sort = 'updatedAt,desc';

    // Act
    service.getTracks(page, size, sort).subscribe((response) => {
      // Assert
      expect(response).toEqual(mockTrackPageResponse);
      expect(response.content).toHaveLength(1);
      expect(response.content[0].id).toBe(1);
      expect(response.content[0].title).toBe('Test Track');
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne((request) => {
      return request.url === '/api/tracks' &&
             request.params.get('page') === page.toString() &&
             request.params.get('size') === size.toString() &&
             request.params.get('sort') === sort;
    });
    expect(req.request.method).toBe('GET');
    req.flush(mockTrackPageResponse);
  });
  // [END_TRACK_SERVICE_SPEC_GET_TRACKS_SUCCESS]

  // [START_TRACK_SERVICE_SPEC_GET_TRACKS_EMPTY]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_GET_TRACKS_EMPTY
   * PURPOSE: Проверка получения пустого списка треков.
   *
   * @PreConditions:
   * - сервис инициализирован
   *
   * @PostConditions:
   * - возвращается Observable с пустым TrackPageResponse
   *
   * @Invariants:
   * - пустой результат корректен
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку параметров запроса
   */
  it('getTracks should return empty TrackPageResponse', (done) => {
    // Arrange
    const emptyResponse: TrackPageResponse = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0
    };

    // Act
    service.getTracks(0, 10, 'updatedAt,desc').subscribe((response) => {
      // Assert
      expect(response).toEqual(emptyResponse);
      expect(response.content).toHaveLength(0);
      expect(response.totalElements).toBe(0);
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne('/api/tracks?page=0&size=10&sort=updatedAt,desc');
    expect(req.request.method).toBe('GET');
    req.flush(emptyResponse);
  });
  // [END_TRACK_SERVICE_SPEC_GET_TRACKS_EMPTY]

  // [START_TRACK_SERVICE_SPEC_GET_TRACK_SUCCESS]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_GET_TRACK_SUCCESS
   * PURPOSE: Проверка получения трека по ID.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек существует
   *
   * @PostConditions:
   * - возвращается Observable с TrackDto
   *
   * @Invariants:
   * - HTTP GET запрос отправляется на правильный эндпоинт
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку дополнительных полей
   */
  it('getTrack should return TrackDto', (done) => {
    // Arrange
    const trackId = 1;

    // Act
    service.getTrack(trackId).subscribe((track) => {
      // Assert
      expect(track).toEqual(mockTrackDto);
      expect(track.id).toBe(1);
      expect(track.title).toBe('Test Track');
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTrackDto);
  });
  // [END_TRACK_SERVICE_SPEC_GET_TRACK_SUCCESS]

  // [START_TRACK_SERVICE_SPEC_GET_TRACK_NOT_FOUND]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_GET_TRACK_NOT_FOUND
   * PURPOSE: Проверка получения несуществующего трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек не существует
   *
   * @PostConditions:
   * - выбрасывается ошибка
   *
   * @Invariants:
   * - несуществующий трек не может быть получен
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя убрать обработку ошибки
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку сообщения об ошибке
   */
  it('getTrack should handle 404 error', (done) => {
    // Arrange
    const trackId = 999;

    // Act
    service.getTrack(trackId).subscribe({
      next: () => {
        fail('Expected error to be thrown');
      },
      error: (error) => {
        // Assert
        expect(error).toBeTruthy();
        done();
      }
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('GET');
    req.flush('Track not found', { status: 404, statusText: 'Not Found' });
  });
  // [END_TRACK_SERVICE_SPEC_GET_TRACK_NOT_FOUND]

  // [START_TRACK_SERVICE_SPEC_CREATE_TRACK_SUCCESS]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_CREATE_TRACK_SUCCESS
   * PURPOSE: Проверка создания нового трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - запрос валиден
   *
   * @PostConditions:
   * - возвращается Observable с TrackDto
   *
   * @Invariants:
   * - HTTP POST запрос отправляется на правильный эндпоинт
   *
   * @SideEffects:
   * - трек создаётся на сервере
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию запроса
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку дополнительных полей
   */
  it('createTrack should return TrackDto', (done) => {
    // Arrange
    const createRequest: CreateTrackRequest = {
      title: 'New Track',
      description: 'New Description'
    };

    // Act
    service.createTrack(createRequest).subscribe((track) => {
      // Assert
      expect(track).toEqual(mockTrackDto);
      expect(track.id).toBe(1);
      expect(track.title).toBe('Test Track');
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne('/api/tracks');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(createRequest);
    req.flush(mockTrackDto);
  });
  // [END_TRACK_SERVICE_SPEC_CREATE_TRACK_SUCCESS]

  // [START_TRACK_SERVICE_SPEC_CREATE_TRACK_INVALID_REQUEST]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_CREATE_TRACK_INVALID_REQUEST
   * PURPOSE: Проверка создания трека с невалидным запросом.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - запрос невалиден
   *
   * @PostConditions:
   * - выбрасывается ошибка
   *
   * @Invariants:
   * - валидация обязательна
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя убрать обработку ошибки
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку сообщения об ошибке
   */
  it('createTrack should handle 400 error', (done) => {
    // Arrange
    const invalidRequest: CreateTrackRequest = {
      title: '',
      description: ''
    };

    // Act
    service.createTrack(invalidRequest).subscribe({
      next: () => {
        fail('Expected error to be thrown');
      },
      error: (error) => {
        // Assert
        expect(error).toBeTruthy();
        done();
      }
    });

    // Assert HTTP request
    const req = httpMock.expectOne('/api/tracks');
    expect(req.request.method).toBe('POST');
    req.flush('Invalid request', { status: 400, statusText: 'Bad Request' });
  });
  // [END_TRACK_SERVICE_SPEC_CREATE_TRACK_INVALID_REQUEST]

  // [START_TRACK_SERVICE_SPEC_UPDATE_TRACK_SUCCESS]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_UPDATE_TRACK_SUCCESS
   * PURPOSE: Проверка обновления трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек существует
   * - запрос валиден
   *
   * @PostConditions:
   * - возвращается Observable с TrackDto
   *
   * @Invariants:
   * - HTTP PUT запрос отправляется на правильный эндпоинт
   *
   * @SideEffects:
   * - трек обновляется на сервере
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию запроса
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку дополнительных полей
   */
  it('updateTrack should return TrackDto', (done) => {
    // Arrange
    const trackId = 1;
    const updateRequest: UpdateTrackRequest = {
      title: 'Updated Track',
      description: 'Updated Description'
    };

    const updatedTrack: TrackDto = {
      ...mockTrackDto,
      title: 'Updated Track',
      description: 'Updated Description'
    };

    // Act
    service.updateTrack(trackId, updateRequest).subscribe((track) => {
      // Assert
      expect(track).toEqual(updatedTrack);
      expect(track.id).toBe(1);
      expect(track.title).toBe('Updated Track');
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(updatedTrack);
  });
  // [END_TRACK_SERVICE_SPEC_UPDATE_TRACK_SUCCESS]

  // [START_TRACK_SERVICE_SPEC_UPDATE_TRACK_NOT_FOUND]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_UPDATE_TRACK_NOT_FOUND
   * PURPOSE: Проверка обновления несуществующего трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек не существует
   *
   * @PostConditions:
   * - выбрасывается ошибка
   *
   * @Invariants:
   * - несуществующий трек не может быть обновлён
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя убрать обработку ошибки
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку сообщения об ошибке
   */
  it('updateTrack should handle 404 error', (done) => {
    // Arrange
    const trackId = 999;
    const updateRequest: UpdateTrackRequest = {
      title: 'Updated Track',
      description: 'Updated Description'
    };

    // Act
    service.updateTrack(trackId, updateRequest).subscribe({
      next: () => {
        fail('Expected error to be thrown');
      },
      error: (error) => {
        // Assert
        expect(error).toBeTruthy();
        done();
      }
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('PUT');
    req.flush('Track not found', { status: 404, statusText: 'Not Found' });
  });
  // [END_TRACK_SERVICE_SPEC_UPDATE_TRACK_NOT_FOUND]

  // [START_TRACK_SERVICE_SPEC_DELETE_TRACK_SUCCESS]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_DELETE_TRACK_SUCCESS
   * PURPOSE: Проверка удаления трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек существует
   *
   * @PostConditions:
   * - возвращается Observable с void
   *
   * @Invariants:
   * - HTTP DELETE запрос отправляется на правильный эндпоинт
   *
   * @SideEffects:
   * - трек удаляется с сервера
   *
   * @ForbiddenChanges:
   * - нельзя убрать проверку прав доступа
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку сообщения об ошибке
   */
  it('deleteTrack should return void', (done) => {
    // Arrange
    const trackId = 1;

    // Act
    service.deleteTrack(trackId).subscribe(() => {
      // Assert
      done();
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
  // [END_TRACK_SERVICE_SPEC_DELETE_TRACK_SUCCESS]

  // [START_TRACK_SERVICE_SPEC_DELETE_TRACK_NOT_FOUND]
  /*
   * ANCHOR: TRACK_SERVICE_SPEC_DELETE_TRACK_NOT_FOUND
   * PURPOSE: Проверка удаления несуществующего трека.
   *
   * @PreConditions:
   * - сервис инициализирован
   * - трек не существует
   *
   * @PostConditions:
   * - выбрасывается ошибка
   *
   * @Invariants:
   * - несуществующий трек не может быть удалён
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя убрать обработку ошибки
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку сообщения об ошибке
   */
  it('deleteTrack should handle 404 error', (done) => {
    // Arrange
    const trackId = 999;

    // Act
    service.deleteTrack(trackId).subscribe({
      next: () => {
        fail('Expected error to be thrown');
      },
      error: (error) => {
        // Assert
        expect(error).toBeTruthy();
        done();
      }
    });

    // Assert HTTP request
    const req = httpMock.expectOne(`/api/tracks/${trackId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush('Track not found', { status: 404, statusText: 'Not Found' });
  });
  // [END_TRACK_SERVICE_SPEC_DELETE_TRACK_NOT_FOUND]
});
// [END_TRACK_SERVICE_SPEC]
// === END_CHUNK: TRACK_SERVICE_SPEC ===
