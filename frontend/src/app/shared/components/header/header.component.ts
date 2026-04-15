// === CHUNK: HEADER_COMPONENT [UI] ===
// Описание: Компонент шапки приложения с логотипом и кнопкой выхода.
// Dependencies: Angular Core, Common, Router, AuthService, UserDto

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserDto } from '../../models';
import { logLine } from '../../../core/lib/log';

// [START_HEADER_COMPONENT]
/*
 * ANCHOR: HEADER_COMPONENT
 * PURPOSE: Отображение шапки приложения и управление выходом пользователя.
 *
 * @PreConditions:
 * - AuthService доступен через DI
 * - Router доступен через DI
 *
 * @PostConditions:
 * - при вызове logout пользователь перенаправляется на /login
 *
 * @Invariants:
 * - currentUser$ всегда отражает актуального пользователя
 *
 * @SideEffects:
 * - навигационный переход при logout
 *
 * @ForbiddenChanges:
 * - нельзя изменять логику logout без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser$ = this.authService.currentUser$;

  // [START_HEADER_LOGOUT]
  /*
   * ANCHOR: HEADER_LOGOUT
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
    logLine('ui', 'DEBUG', 'logout', 'HEADER_LOGOUT', 'ENTRY', {});

    this.authService.logout();
    this.router.navigate(['/login']);

    logLine('ui', 'INFO', 'logout', 'HEADER_LOGOUT', 'STATE_CHANGE', {
      entity: 'user',
      action: 'logged_out'
    });

    logLine('ui', 'DEBUG', 'logout', 'HEADER_LOGOUT', 'EXIT', {
      navigated_to: '/login'
    });
  }
  // [END_HEADER_LOGOUT]
}
// [END_HEADER_COMPONENT]
// === END_CHUNK: HEADER_COMPONENT ===
