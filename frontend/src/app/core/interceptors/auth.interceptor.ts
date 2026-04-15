// === CHUNK: AUTH_INTERCEPTOR [INTERCEPTOR] ===
// Описание: Interceptor для перехвата HTTP запросов и обработки токенов.
// Dependencies: Angular HTTP, RxJS, Router

import { HttpInterceptorFn } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { logLine } from '../lib/log';

const isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

// [START_AUTH_INTERCEPTOR]
/*
 * ANCHOR: AUTH_INTERCEPTOR
 * PURPOSE: Перехват HTTP запросов для обработки токенов и ошибок аутентификации.
 *
 * @PreConditions:
 * - токен доступа может быть в localStorage
 * - API отвечает корректно на запросы
 *
 * @PostConditions:
 * - при 401 происходит попытка обновления токена
 * - при 403 пользователь перенаправляется на страницу входа
 *
 * @Invariants:
 * - перехватчик не изменяет запросы, кроме добавления заголовка Authorization
 *
 * @SideEffects:
 * - возможные навигационные переходы
 * - обновление токенов в localStorage
 *
 * @ForbiddenChanges:
 * - нельзя убрать обработку 401/403 без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('access_token');

  logLine('interceptor', 'DEBUG', 'authInterceptor', 'AUTH_INTERCEPTOR', 'ENTRY', {
    url: req.url,
    has_token: !!token
  });

  if (token) {
    req = addToken(req, token);
    logLine('interceptor', 'DEBUG', 'authInterceptor', 'AUTH_INTERCEPTOR', 'STATE_CHANGE', {
      action: 'token_added',
      url: req.url
    });
  }

  return next(req).pipe(
    catchError((error: any) => {
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        logLine('interceptor', 'WARN', 'authInterceptor', 'AUTH_INTERCEPTOR', 'DECISION', {
          decision: 'handle_401',
          url: req.url
        });
        return handle401Error(req, next, router);
      }
      if (error.status === 403) {
        logLine('interceptor', 'WARN', 'authInterceptor', 'AUTH_INTERCEPTOR', 'DECISION', {
          decision: 'handle_403',
          url: req.url
        });
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
// [END_AUTH_INTERCEPTOR]

// [START_ADD_TOKEN]
/*
 * ANCHOR: ADD_TOKEN
 * PURPOSE: Добавление токена в заголовок Authorization запроса.
 *
 * @PreConditions:
 * - request валидный HTTP запрос
 * - token непустая строка
 *
 * @PostConditions:
 * - возвращается клон запроса с добавленным заголовком Authorization
 *
 * @Invariants:
 * - оригинальный запрос не изменяется
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить формат заголовка Authorization без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные заголовки
 */
function addToken(request: any, token: string): any {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}
// [END_ADD_TOKEN]

// [START_HANDLE_401_ERROR]
/*
 * ANCHOR: HANDLE_401_ERROR
 * PURPOSE: Обработка ошибки 401 (Unauthorized) с попыткой обновления токена.
 *
 * @PreConditions:
 * - request оригинальный запрос, который вернул 401
 * - next функция для продолжения цепочки запросов
 * - router для навигации
 *
 * @PostConditions:
 * - при успешном обновлении: оригинальный запрос повторяется с новым токеном
 * - при неудаче: пользователь разлогинивается
 *
 * @Invariants:
 * - только один запрос обновления токена выполняется одновременно
 *
 * @SideEffects:
 * - обновление токенов в localStorage
 * - навигация на /login при неудаче
 *
 * @ForbiddenChanges:
 * - нельзя убрать логику предотвращения множественных запросов обновления
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные проверки
 */
function handle401Error(request: any, next: any, router: Router): Observable<any> {
  if (!isRefreshing) {
    const refreshToken = localStorage.getItem('refresh_token');

    if (refreshToken) {
      logLine('interceptor', 'DEBUG', 'handle401Error', 'HANDLE_401_ERROR', 'DECISION', {
        decision: 'attempt_refresh',
        has_refresh_token: true
      });

      return refreshTokenCall(refreshToken).pipe(
        switchMap((response: any) => {
          localStorage.setItem('access_token', response.token);
          localStorage.setItem('refresh_token', response.refreshToken);
          refreshTokenSubject.next(response.token);

          logLine('interceptor', 'INFO', 'handle401Error', 'HANDLE_401_ERROR', 'STATE_CHANGE', {
            entity: 'tokens',
            action: 'refreshed'
          });

          return next(addToken(request, response.token));
        }),
        catchError((error) => {
          logLine('interceptor', 'ERROR', 'handle401Error', 'HANDLE_401_ERROR', 'ERROR', {
            reason: 'refresh_failed'
          });
          logout(router);
          return throwError(() => error);
        })
      );
    } else {
      logLine('interceptor', 'WARN', 'handle401Error', 'HANDLE_401_ERROR', 'DECISION', {
        decision: 'logout_no_refresh_token'
      });
      logout(router);
      return throwError(() => new Error('No refresh token available'));
    }
  } else {
    logLine('interceptor', 'DEBUG', 'handle401Error', 'HANDLE_401_ERROR', 'DECISION', {
      decision: 'wait_for_refresh'
    });
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => next(addToken(request, token)))
    );
  }
}
// [END_HANDLE_401_ERROR]

// [START_REFRESH_TOKEN_CALL]
/*
 * ANCHOR: REFRESH_TOKEN_CALL
 * PURPOSE: Вызов API для обновления токена.
 *
 * @PreConditions:
 * - refreshToken валидный refresh токен
 *
 * @PostConditions:
 * - при успехе: возвращается Observable с новыми токенами
 * - при ошибке: выбрасывается ошибка
 *
 * @Invariants:
 * - запрос всегда отправляется на /auth/refresh
 *
 * @SideEffects:
 * - HTTP POST запрос к API
 *
 * @ForbiddenChanges:
 * - нельзя изменить эндпоинт без согласования
 *
 * @AllowedRefactorZone:
 * - можно использовать HttpClient вместо fetch
 */
function refreshTokenCall(refreshToken: string): Observable<any> {
  const apiUrl = 'http://localhost:8080/api/v1';

  logLine('interceptor', 'DEBUG', 'refreshTokenCall', 'REFRESH_TOKEN_CALL', 'ENTRY', {
    has_refresh_token: !!refreshToken
  });

  return new Observable(observer => {
    fetch(`${apiUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refreshToken })
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Refresh failed');
      }
      return response.json();
    })
    .then(data => {
      logLine('interceptor', 'DEBUG', 'refreshTokenCall', 'REFRESH_TOKEN_CALL', 'EXIT', {
        success: true
      });
      observer.next(data);
      observer.complete();
    })
    .catch(error => {
      logLine('interceptor', 'ERROR', 'refreshTokenCall', 'REFRESH_TOKEN_CALL', 'ERROR', {
        error: error.message
      });
      observer.error(error);
    });
  });
}
// [END_REFRESH_TOKEN_CALL]

// [START_LOGOUT]
/*
 * ANCHOR: LOGOUT
 * PURPOSE: Выход пользователя из системы при ошибке аутентификации.
 *
 * @PreConditions:
 * - router доступен для навигации
 *
 * @PostConditions:
 * - токены удалены из localStorage
 * - пользователь перенаправлён на /login
 *
 * @Invariants:
 * - после logout пользователь считается неаутентифицированным
 *
 * @SideEffects:
 * - удаление токенов из localStorage
 * - навигация на /login
 *
 * @ForbiddenChanges:
 * - нельзя оставить токены в localStorage
 *
 * @AllowedRefactorZone:
 * - можно добавить очистку других данных
 */
function logout(router: Router): void {
  logLine('interceptor', 'DEBUG', 'logout', 'LOGOUT', 'ENTRY', {});

  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  router.navigate(['/login']);

  logLine('interceptor', 'INFO', 'logout', 'LOGOUT', 'STATE_CHANGE', {
    entity: 'tokens',
    action: 'removed'
  });

  logLine('interceptor', 'DEBUG', 'logout', 'LOGOUT', 'EXIT', {});
}
// [END_LOGOUT]
// === END_CHUNK: AUTH_INTERCEPTOR ===
