// [START_ADMIN_GUARD]
/*
 * ANCHOR: ADMIN_GUARD
 * PURPOSE: Защита маршрутов, доступных только администраторам.
 *
 * @PreConditions:
 * - AuthService доступен.
 * - Пользователь аутентифицирован.
 *
 * @PostConditions:
 * - При отсутствии прав происходит редирект на /dashboard.
 *
 * @Invariants:
 * - Возврат boolean указывает на статус доступа.
 *
 * @SideEffects:
 * - Возможный навигационный переход.
 *
 * @ForbiddenChanges:
 * - Нельзя менять логику редиректа без согласования.
 *
 * @AllowedRefactorZone:
 * - Внутреннее оформление кода.
 */
// [END_ADMIN_GUARD]

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getCurrentUserValue();

  if (currentUser && currentUser.role === 'ADMIN') {
    return true;
  }

  // Если пользователь не админ, перенаправляем на дашборд
  router.navigate(['/dashboard']);

  return false;
};
