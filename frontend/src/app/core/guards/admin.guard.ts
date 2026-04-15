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
