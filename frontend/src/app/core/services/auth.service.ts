// === CHUNK: AUTH_SERVICE [AUTH] ===
// Описание: Сервис управления аутентификацией и авторизацией пользователя.
// Dependencies: Angular HttpClient, RxJS, environment, shared models

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  AuthRequest,
  RegisterRequest,
  AuthResponse,
  UserDto,
  RefreshTokenRequest
} from '../../shared/models';
import { logLine } from '../lib/log';

// [START_AUTH_SERVICE]
/*
 * ANCHOR: AUTH_SERVICE
 * PURPOSE: Управление аутентификацией и авторизацией пользователя.
 *
 * @PreConditions:
 * - environment.apiUrl определён и доступен
 * - сервер API реализует эндпоинты /auth/*
 *
 * @PostConditions:
 * - при успешном login сохраняются токены в localStorage
 * - при logout токены удаляются и текущий пользователь сбрасывается
 *
 * @Invariants:
 * - токены всегда синхронны между localStorage и currentUserSubject
 *
 * @SideEffects:
 * - запись/удаление токенов в localStorage
 * - HTTP запросы к API
 *
 * @ForbiddenChanges:
 * - нельзя изменить структуру токенов без согласования
 * - нельзя убрать вызов this.loadCurrentUser() в конструкторе
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление методов, рефакторинг без изменения поведения
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<UserDto | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadCurrentUser();
  }

  // [START_AUTH_LOGIN]
  /*
   * ANCHOR: AUTH_LOGIN
   * PURPOSE: Аутентификация пользователя по логину и паролю.
   *
   * @PreConditions:
   * - credentials.username непустая строка
   * - credentials.password непустая строка
   *
   * @PostConditions:
   * - при успехе: токены сохранены в localStorage, возвращается AuthResponse
   * - при ошибке: выбрасывается HTTP ошибка
   *
   * @Invariants:
   * - токены сохраняются только при успешном ответе от сервера
   *
   * @SideEffects:
   * - запись access_token и refresh_token в localStorage
   * - HTTP POST запрос к /auth/login
   *
   * @ForbiddenChanges:
   * - нельзя убрать сохранение токенов в localStorage
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную валидацию credentials
   */
  login(credentials: AuthRequest): Observable<AuthResponse> {
    logLine('auth', 'DEBUG', 'login', 'AUTH_LOGIN', 'ENTRY', {
      username: credentials.username
    });

    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      map(response => {
        localStorage.setItem('access_token', response.token);
        localStorage.setItem('refresh_token', response.refreshToken);

        logLine('auth', 'INFO', 'login', 'AUTH_LOGIN', 'STATE_CHANGE', {
          entity: 'tokens',
          action: 'saved',
          token_type: 'access_token'
        });

        logLine('auth', 'DEBUG', 'login', 'AUTH_LOGIN', 'EXIT', {
          success: true
        });

        return response;
      })
    );
  }
  // [END_AUTH_LOGIN]

  // [START_AUTH_REGISTER]
  /*
   * ANCHOR: AUTH_REGISTER
   * PURPOSE: Регистрация нового пользователя в системе.
   *
   * @PreConditions:
   * - data.username уникален и соответствует требованиям валидации
   * - data.password соответствует требованиям безопасности
   * - data.email валидный email адрес
   *
   * @PostConditions:
   * - при успехе: возвращается UserDto с данными созданного пользователя
   * - при ошибке: выбрасывается HTTP ошибка (например, пользователь уже существует)
   *
   * @Invariants:
   * - пользователь создаётся только на сервере
   *
   * @SideEffects:
   * - HTTP POST запрос к /auth/register
   *
   * @ForbiddenChanges:
   * - нельзя убрать валидацию на клиенте (если добавлена)
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную валидацию полей
   */
  register(data: RegisterRequest): Observable<UserDto> {
    logLine('auth', 'DEBUG', 'register', 'AUTH_REGISTER', 'ENTRY', {
      username: data.username,
      email: data.email
    });

    return this.http.post<UserDto>(`${this.apiUrl}/auth/register`, data);
  }
  // [END_AUTH_REGISTER]

  // [START_AUTH_LOGOUT]
  /*
   * ANCHOR: AUTH_LOGOUT
   * PURPOSE: Выход пользователя из системы.
   *
   * @PreConditions:
   * - пользователь аутентифицирован (токен существует)
   *
   * @PostConditions:
   * - токены удалены из localStorage
   * - currentUserSubject сброшен в null
   *
   * @Invariants:
   * - после logout пользователь считается неаутентифицированным
   *
   * @SideEffects:
   * - удаление access_token и refresh_token из localStorage
   * - сброс currentUserSubject
   * - HTTP POST запрос к /auth/logout
   *
   * @ForbiddenChanges:
   * - нельзя оставить токены в localStorage после logout
   * - нельзя не сбросить currentUserSubject
   *
   * @AllowedRefactorZone:
   * - можно добавить очистку других данных из localStorage
   */
  logout(): Observable<void> {
    logLine('auth', 'DEBUG', 'logout', 'AUTH_LOGOUT', 'ENTRY', {});

    return this.http.post<void>(`${this.apiUrl}/auth/logout`, {}).pipe(
      map(() => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        this.currentUserSubject.next(null);

        logLine('auth', 'INFO', 'logout', 'AUTH_LOGOUT', 'STATE_CHANGE', {
          entity: 'tokens',
          action: 'removed'
        });

        logLine('auth', 'DEBUG', 'logout', 'AUTH_LOGOUT', 'EXIT', {
          success: true
        });
      })
    );
  }
  // [END_AUTH_LOGOUT]

  // [START_AUTH_REFRESH_TOKEN]
  /*
   * ANCHOR: AUTH_REFRESH_TOKEN
   * PURPOSE: Обновление access токена с помощью refresh токена.
   *
   * @PreConditions:
   * - request.refreshToken валидный и не истёкший refresh токен
   *
   * @PostConditions:
   * - при успехе: возвращается новый AuthResponse с обновлёнными токенами
   * - при ошибке: выбрасывается HTTP ошибка
   *
   * @Invariants:
   * - старый refresh токен остаётся валидным до истечения срока
   *
   * @SideEffects:
   * - HTTP POST запрос к /auth/refresh
   *
   * @ForbiddenChanges:
   * - нельзя изменить эндпоинт без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить автоматическое сохранение новых токенов
   */
  refreshToken(request: RefreshTokenRequest): Observable<AuthResponse> {
    logLine('auth', 'DEBUG', 'refreshToken', 'AUTH_REFRESH_TOKEN', 'ENTRY', {
      has_refresh_token: !!request.refreshToken
    });

    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/refresh`, request);
  }
  // [END_AUTH_REFRESH_TOKEN]

  // [START_AUTH_GET_CURRENT_USER]
  /*
   * ANCHOR: AUTH_GET_CURRENT_USER
   * PURPOSE: Получение данных текущего аутентифицированного пользователя.
   *
   * @PreConditions:
   * - пользователь аутентифицирован (токен существует и валиден)
   *
   * @PostConditions:
   * - при успехе: currentUserSubject обновлён, возвращается UserDto
   * - при ошибке: выбрасывается HTTP ошибка (например, токен истёк)
   *
   * @Invariants:
   * - currentUserSubject всегда содержит актуальные данные пользователя
   *
   * @SideEffects:
   * - обновление currentUserSubject
   * - HTTP GET запрос к /auth/me
   *
   * @ForbiddenChanges:
   * - нельзя не обновить currentUserSubject при успешном ответе
   *
   * @AllowedRefactorZone:
   * - можно добавить кэширование данных пользователя
   */
  getCurrentUser(): Observable<UserDto> {
    logLine('auth', 'DEBUG', 'getCurrentUser', 'AUTH_GET_CURRENT_USER', 'ENTRY', {});

    return this.http.get<UserDto>(`${this.apiUrl}/auth/me`).pipe(
      map(user => {
        this.currentUserSubject.next(user);

        logLine('auth', 'INFO', 'getCurrentUser', 'AUTH_GET_CURRENT_USER', 'STATE_CHANGE', {
          entity: 'currentUser',
          action: 'updated',
          user_id: user.id
        });

        logLine('auth', 'DEBUG', 'getCurrentUser', 'AUTH_GET_CURRENT_USER', 'EXIT', {
          success: true
        });

        return user;
      })
    );
  }
  // [END_AUTH_GET_CURRENT_USER]

  // [START_AUTH_IS_LOGGED_IN]
  /*
   * ANCHOR: AUTH_IS_LOGGED_IN
   * PURPOSE: Проверка наличия access токена в localStorage.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - возвращается true если access_token существует, иначе false
   *
   * @Invariants:
   * - результат зависит только от наличия токена в localStorage
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя добавить проверку валидности токена (это делает сервер)
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку истечения срока токена (если хранится)
   */
  isLoggedIn(): boolean {
    return !!localStorage.getItem('access_token');
  }
  // [END_AUTH_IS_LOGGED_IN]

  // [START_AUTH_IS_AUTHENTICATED]
  /*
   * ANCHOR: AUTH_IS_AUTHENTICATED
   * PURPOSE: Проверка аутентификации пользователя (синоним isLoggedIn).
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - возвращается true если access_token существует, иначе false
   *
   * @Invariants:
   * - результат всегда совпадает с isLoggedIn()
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить поведение относительно isLoggedIn()
   *
   * @AllowedRefactorZone:
   * - можно добавить дополнительную логику проверки
   */
  isAuthenticated(): boolean {
    return this.isLoggedIn();
  }
  // [END_AUTH_IS_AUTHENTICATED]

  // [START_AUTH_GET_CURRENT_USER_VALUE]
  /*
   * ANCHOR: AUTH_GET_CURRENT_USER_VALUE
   * PURPOSE: Получение текущего значения пользователя из BehaviorSubject.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - возвращается текущее значение currentUserSubject или null
   *
   * @Invariants:
   * - возвращаемое значение синхронно с currentUserSubject
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя добавить асинхронные операции
   *
   * @AllowedRefactorZone:
   * - можно добавить дефолтное значение вместо null
   */
  getCurrentUserValue(): UserDto | null {
    return this.currentUserSubject.value;
  }
  // [END_AUTH_GET_CURRENT_USER_VALUE]

  // [START_AUTH_IS_ADMIN]
  /*
   * ANCHOR: AUTH_IS_ADMIN
   * PURPOSE: Проверка имеет ли текущий пользователь роль ADMIN.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - возвращается true если пользователь имеет роль ADMIN, иначе false
   *
   * @Invariants:
   * - результат зависит только от роли текущего пользователя
   *
   * @SideEffects:
   * - нет побочных эффектов
   *
   * @ForbiddenChanges:
   * - нельзя изменить логику проверки роли без согласования
   *
   * @AllowedRefactorZone:
   * - можно добавить проверку других ролей
   */
  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ADMIN';
  }
  // [END_AUTH_IS_ADMIN]

  // [START_AUTH_LOAD_CURRENT_USER]
  /*
   * ANCHOR: AUTH_LOAD_CURRENT_USER
   * PURPOSE: Загрузка данных текущего пользователя при инициализации сервиса.
   *
   * @PreConditions:
   * - вызывается только из конструктора
   *
   * @PostConditions:
   * - если пользователь аутентифицирован: данные загружены с сервера
   * - если пользователь не аутентифицирован: ничего не происходит
   *
   * @Invariants:
   * - метод вызывается автоматически при создании сервиса
   *
   * @SideEffects:
   * - HTTP GET запрос к /auth/me (если токен существует)
   *
   * @ForbiddenChanges:
   * - нельзя убрать вызов этого метода из конструктора
   *
   * @AllowedRefactorZone:
   * - можно добавить обработку ошибок загрузки
   */
  private loadCurrentUser(): void {
    if (this.isLoggedIn()) {
      this.getCurrentUser().subscribe();
    }
  }
  // [END_AUTH_LOAD_CURRENT_USER]
}
// [END_AUTH_SERVICE]
// === END_CHUNK: AUTH_SERVICE ===
