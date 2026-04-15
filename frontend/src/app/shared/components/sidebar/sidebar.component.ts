// === CHUNK: SIDEBAR_COMPONENT [UI] ===
// Описание: Компонент боковой панели навигации приложения.
// Dependencies: Angular Core, Common, Router, AuthService

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { logLine } from '../../../core/lib/log';

// [START_SIDEBAR_COMPONENT]
/*
 * ANCHOR: SIDEBAR_COMPONENT
 * PURPOSE: Боковая панель навигации приложения.
 *
 * @PreConditions:
 * - Angular окружение инициализировано
 * - AuthService доступен через DI
 *
 * @PostConditions:
 * - компонент отображается в DOM
 *
 * @Invariants:
 * - нет динамических состояний
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя менять selector без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода
 */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser$ = this.authService.currentUser$;

  // [START_SIDEBAR_IS_ADMIN]
  /*
   * ANCHOR: SIDEBAR_IS_ADMIN
   * PURPOSE: Проверка имеет ли текущий пользователя роль ADMIN.
   *
   * @PreConditions:
   * - нет нетривиальных предусловий
   *
   * @PostConditions:
   * - возвращается true если пользователь имеет роль ADMIN
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
   * - можно добавить проверку других ролей
   */
  isAdmin(): boolean {
    const user = this.authService.getCurrentUserValue();
    return user?.role === 'ADMIN';
  }
  // [END_SIDEBAR_IS_ADMIN]

  // [START_SIDEBAR_LOGOUT]
  /*
   * ANCHOR: SIDEBAR_LOGOUT
   * PURPOSE: Выход пользователя из системы.
   *
   * @PreConditions:
   * - пользователь аутентифицирован
   *
   * @PostConditions:
   * - токены удалены
   * - пользователь перенаправлён на /login
   *
   * @Invariants:
   * - после logout пользователь считается неаутентифицированным
   *
   * @SideEffects:
   * - HTTP POST запрос к /auth/logout
   * - навигация на /login
   *
   * @ForbiddenChanges:
   * - нельзя убрать навигацию на /login
   *
   * @AllowedRefactorZone:
   * - можно добавить подтверждение выхода
   */
  logout(): void {
    logLine('ui', 'DEBUG', 'logout', 'SIDEBAR_LOGOUT', 'ENTRY', {});

    this.authService.logout();
    this.router.navigate(['/login']);

    logLine('ui', 'INFO', 'logout', 'SIDEBAR_LOGOUT', 'STATE_CHANGE', {
      entity: 'user',
      action: 'logged_out'
    });

    logLine('ui', 'DEBUG', 'logout', 'SIDEBAR_LOGOUT', 'EXIT', {
      navigated_to: '/login'
    });
  }
  // [END_SIDEBAR_LOGOUT]
}
// [END_SIDEBAR_COMPONENT]
// === END_CHUNK: SIDEBAR_COMPONENT ===
