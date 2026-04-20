/**
 * Юнит-тесты для DashboardComponent.
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { TrackService } from '../../core/services';
import { TrackSummaryDto, CreateTrackRequest } from '../../shared/models';
import { logLine } from '../../core/lib/log';

// === CHUNK: DASHBOARD_COMPONENT_SPEC [TEST] ===
// Описание: Юнит-тесты для DashboardComponent.
// Dependencies: Angular Core, RouterTestingModule, shared models

// [START_DASHBOARD_COMPONENT_SPEC]
/*
 * ANCHOR: DASHBOARD_COMPONENT_SPEC
 * PURPOSE: Юнит-тесты для DashboardComponent.
 *
 * @PreConditions:
 * - DashboardComponent инициализирован с моками зависимостей
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов DashboardComponent
 *
 * @Invariants:
 * - компонент корректно управляет состоянием
 * - ошибки обрабатываются корректно
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку состояния компонента
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let trackServiceSpy: jasmine.SpyObj<TrackService>;

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

  beforeEach(async () => {
    const trackServiceMock = jasmine.createSpyObj('TrackService', [
      'getTracks',
      'createTrack'
    ]);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, RouterTestingModule],
      providers: [
        { provide: TrackService, useValue: trackServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    trackServiceSpy = TestBed.inject(TrackService) as jasmine.SpyObj<TrackService>;
  });

  // [START_DASHBOARD_COMPONENT_SPEC_CREATE_SUCCESS]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CREATE_SUCCESS
   * PURPOSE: Проверка создания компонента.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - компонент создан
   *
   * @Invariants:
   * - компонент всегда создаётся корректно
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику создания компонента
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку начального состояния
   */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CREATE_SUCCESS]

  // [START_DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS
   * PURPOSE: Проверка загрузки треков при инициализации.
   *
   * @PreConditions:
   * - компонент создан
   *
   * @PostConditions:
   * - треки загружены
   *
   * @Invariants:
   * - ngOnInit всегда вызывает loadTracks()
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
  it('should load tracks on init', () => {
    // Arrange
    trackServiceSpy.getTracks.and.returnValue(of({
      content: [mockTrackSummaryDto],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    }));

    // Act
    component.ngOnInit();

    // Assert
    expect(trackServiceSpy.getTracks).toHaveBeenCalledWith(0, 10, 'updatedAt,desc');
    expect(component.tracks).toEqual([mockTrackSummaryDto]);
    expect(component.totalElements).toBe(1);
    expect(component.loading).toBeFalse();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS]

  // [START_DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS_ERROR]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS_ERROR
   * PURPOSE: Проверка обработки ошибки при загрузке треков.
   *
   * @PreConditions:
   * - компонент создан
   * - API возвращает ошибку
   *
   * @PostConditions:
   * - errorMessage содержит описание ошибки
   *
   * @Invariants:
   * - loading сбрасывается в false
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
  it('should handle error when loading tracks', () => {
    // Arrange
    trackServiceSpy.getTracks.and.returnValue(throwError(() => new Error('Network error')));

    // Act
    component.ngOnInit();

    // Assert
    expect(component.errorMessage).toBe('Ошибка загрузки треков');
    expect(component.loading).toBeFalse();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_ON_INIT_LOAD_TRACKS_ERROR]

  // [START_DASHBOARD_COMPONENT_SPEC_ON_PAGE_CHANGE]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_ON_PAGE_CHANGE
   * PURPOSE: Проверка изменения страницы пагинации.
   *
   * @PreConditions:
   * - компонент инициализирован
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
  it('should change page and reload tracks', () => {
    // Arrange
    trackServiceSpy.getTracks.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 1,
      size: 10,
      number: 1
    }));

    // Act
    component.onPageChange(1);

    // Assert
    expect(component.page).toBe(1);
    expect(trackServiceSpy.getTracks).toHaveBeenCalledWith(1, 10, 'updatedAt,desc');
  });
  // [END_DASHBOARD_COMPONENT_SPEC_ON_PAGE_CHANGE]

  // [START_DASHBOARD_COMPONENT_SPEC_ON_SIZE_CHANGE]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_ON_SIZE_CHANGE
   * PURPOSE: Проверка изменения размера страницы.
   *
   * @PreConditions:
   * - компонент инициализирован
   *
   * @PostConditions:
   * - size обновлён, page сброшен в 0, треки перезагружены
   *
   * @Invariants:
   * - всегда вызывается loadTracks() после изменения размера
   * - page всегда сбрасывается в 0
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
  it('should change size and reload tracks', () => {
    // Arrange
    trackServiceSpy.getTracks.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 1,
      size: 20,
      number: 0
    }));

    // Act
    component.onSizeChange(20);

    // Assert
    expect(component.size).toBe(20);
    expect(component.page).toBe(0);
    expect(trackServiceSpy.getTracks).toHaveBeenCalledWith(0, 20, 'updatedAt,desc');
  });
  // [END_DASHBOARD_COMPONENT_SPEC_ON_SIZE_CHANGE]

  // [START_DASHBOARD_COMPONENT_SPEC_ON_FILTER_CHANGE]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_ON_FILTER_CHANGE
   * PURPOSE: Проверка изменения фильтра треков.
   *
   * @PreConditions:
   * - компонент инициализирован
   *
   * @PostConditions:
   * - filter обновлён, page сброшен в 0, треки перезагружены
   *
   * @Invariants:
   * - всегда вызывается loadTracks() после изменения фильтра
   * - page всегда сбрасывается в 0
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
  it('should change filter and reload tracks', () => {
    // Arrange
    trackServiceSpy.getTracks.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 1,
      size: 10,
      number: 0
    }));

    // Act
    component.onFilterChange('owned');

    // Assert
    expect(component.filter).toBe('owned');
    expect(component.page).toBe(0);
    expect(trackServiceSpy.getTracks).toHaveBeenCalledWith(0, 10, 'updatedAt,desc');
  });
  // [END_DASHBOARD_COMPONENT_SPEC_ON_FILTER_CHANGE]

  // [START_DASHBOARD_COMPONENT_SPEC_OPEN_CREATE_DIALOG]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_OPEN_CREATE_DIALOG
   * PURPOSE: Проверка открытия диалога создания трека.
   *
   * @PreConditions:
   * - компонент инициализирован
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
  it('should open create dialog and reset form', () => {
    // Arrange
    component.createForm = { title: 'Test', description: 'Test' };

    // Act
    component.openCreateDialog();

    // Assert
    expect(component.showCreateDialog).toBeTrue();
    expect(component.createForm).toEqual({ title: '', description: '' });
    expect(component.createError).toBe('');
  });
  // [END_DASHBOARD_COMPONENT_SPEC_OPEN_CREATE_DIALOG]

  // [START_DASHBOARD_COMPONENT_SPEC_CLOSE_CREATE_DIALOG]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CLOSE_CREATE_DIALOG
   * PURPOSE: Проверка закрытия диалога создания трека.
   *
   * @PreConditions:
   * - диалог открыт
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
   * - можно добавить подтверждение закрытия
   */
  it('should close create dialog and reset form', () => {
    // Arrange
    component.showCreateDialog = true;
    component.createForm = { title: 'Test', description: 'Test' };

    // Act
    component.closeCreateDialog();

    // Assert
    expect(component.showCreateDialog).toBeFalse();
    expect(component.createForm).toEqual({ title: '', description: '' });
    expect(component.createError).toBe('');
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CLOSE_CREATE_DIALOG]

  // [START_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_SUCCESS]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_SUCCESS
   * PURPOSE: Проверка создания нового трека.
   *
   * @PreConditions:
   * - диалог открыт
   * - форма заполнена валидными данными
   *
   * @PostConditions:
   * - трек создан, диалог закрыт, список треков перезагружен
   *
   * @Invariants:
   * - createLoading сбрасывается в false
   *
   * @SideEffects:
   * - HTTP POST запрос к API
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию названия
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную валидацию
   */
  it('should create track successfully', () => {
    // Arrange
    component.showCreateDialog = true;
    component.createForm = { title: 'New Track', description: 'New Description' };
    trackServiceSpy.createTrack.and.returnValue(of(mockTrackSummaryDto));
    trackServiceSpy.getTracks.and.returnValue(of({
      content: [mockTrackSummaryDto],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    }));

    // Act
    component.createTrack();

    // Assert
    expect(trackServiceSpy.createTrack).toHaveBeenCalledWith({
      title: 'New Track',
      description: 'New Description'
    });
    expect(component.showCreateDialog).toBeFalse();
    expect(component.createLoading).toBeFalse();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_SUCCESS]

  // [START_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_EMPTY_TITLE]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_EMPTY_TITLE
   * PURPOSE: Проверка создания трека с пустым названием.
   *
   * @PreConditions:
   * - диалог открыт
   * - форма имеет пустое название
   *
   * @PostConditions:
   * - createError содержит описание ошибки
   *
   * @Invariants:
   * - трек не создаётся при пустом названии
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию названия
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную валидацию
   */
  it('should not create track with empty title', () => {
    // Arrange
    component.showCreateDialog = true;
    component.createForm = { title: '', description: 'Description' };

    // Act
    component.createTrack();

    // Assert
    expect(component.createError).toBe('Название трека обязательно');
    expect(trackServiceSpy.createTrack).not.toHaveBeenCalled();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_EMPTY_TITLE]

  // [START_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_ERROR]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_ERROR
   * PURPOSE: Проверка обработки ошибки при создании трека.
   *
   * @PreConditions:
   * - диалог открыт
   * - форма заполнена валидными данными
   * - API возвращает ошибку
   *
   * @PostConditions:
   * - createError содержит описание ошибки
   *
   * @Invariants:
   * - createLoading сбрасывается в false
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
  it('should handle error when creating track', () => {
    // Arrange
    component.showCreateDialog = true;
    component.createForm = { title: 'New Track', description: 'New Description' };
    trackServiceSpy.createTrack.and.returnValue(throwError(() => new Error('Network error')));

    // Act
    component.createTrack();

    // Assert
    expect(component.createError).toBe('Ошибка создания трека');
    expect(component.createLoading).toBeFalse();
    expect(component.showCreateDialog).toBeTrue();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CREATE_TRACK_ERROR]

  // [START_DASHBOARD_COMPONENT_SPEC_CAN_EDIT_TRACK]
  /*
   * ANCHOR: DASHBOARD_COMPONENT_SPEC_CAN_EDIT_TRACK
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
  it('should return true for OWNER role', () => {
    // Arrange
    const track: TrackSummaryDto = { ...mockTrackSummaryDto, myRole: 'OWNER' };

    // Act
    const result = component.canEditTrack(track);

    // Assert
    expect(result).toBeTrue();
  });

  it('should return true for EDIT role', () => {
    // Arrange
    const track: TrackSummaryDto = { ...mockTrackSummaryDto, myRole: 'EDIT' };

    // Act
    const result = component.canEditTrack(track);

    // Assert
    expect(result).toBeTrue();
  });

  it('should return false for VIEW role', () => {
    // Arrange
    const track: TrackSummaryDto = { ...mockTrackSummaryDto, myRole: 'VIEW' };

    // Act
    const result = component.canEditTrack(track);

    // Assert
    expect(result).toBeFalse();
  });
  // [END_DASHBOARD_COMPONENT_SPEC_CAN_EDIT_TRACK]
});
// [END_DASHBOARD_COMPONENT_SPEC]
// === END_CHUNK: DASHBOARD_COMPONENT_SPEC ===
