// === CHUNK: AUTH_GUARD [GUARD] ===
// Описание: Guard для защиты маршрутов, требующих аутентификации.
// Dependencies: Angular Core, Router, AuthService

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { logLine } from '../lib/log';

// [START_AUTH_GUARD]
/*
 * ANCHOR: AUTH_GUARD
 * PURPOSE: Защита маршрутов, требующих аутентификации.
 *
 * @PreConditions:
 * - AuthService доступен через DI
 * - Router доступен через DI
 *
 * @PostConditions:
 * - при аутентификации: возвращается true, маршрут доступен
 * - при неаутентификации: происходит редирект на /login, возвращается false
 *
 * @Invariants:
 * - возврат boolean указывает на статус доступа к маршруту
 *
 * @SideEffects:
 * - навигационный переход на /login при неаутентификации
 *
 * @ForbiddenChanges:
 * - нельзя менять логику редиректа без согласования
 * - нельзя убрать сохранение returnUrl
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные проверки (например, роли)
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  logLine('guard', 'DEBUG', 'authGuard', 'AUTH_GUARD', 'ENTRY', {
    route_url: state.url
  });

  if (authService.isAuthenticated()) {
    logLine('guard', 'DEBUG', 'authGuard', 'AUTH_GUARD', 'DECISION', {
      decision: 'allow_access',
      reason: 'authenticated',
      route_url: state.url
    });

    logLine('guard', 'DEBUG', 'authGuard', 'AUTH_GUARD', 'EXIT', {
      result: true
    });

    return true;
  }

  // Сохраняем URL для редиректа после входа
  logLine('guard', 'WARN', 'authGuard', 'AUTH_GUARD', 'DECISION', {
    decision: 'deny_access',
    reason: 'not_authenticated',
    route_url: state.url,
    redirect_to: '/login'
  });

  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });

  logLine('guard', 'DEBUG', 'authGuard', 'AUTH_GUARD', 'EXIT', {
    result: false
  });

  return false;
};
// [END_AUTH_GUARD]
// === END_CHUNK: AUTH_GUARD ===
