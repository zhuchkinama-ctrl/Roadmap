// [START_APP_COMPONENT]
/*
 * ANCHOR: APP_COMPONENT
 * PURPOSE: Корневой компонент приложения.
 *
 * @PreConditions:
 * - Angular окружение инициализировано.
 *
 * @PostConditions:
 * - Компонент отображается в DOM.
 *
 * @Invariants:
 * - title всегда строка.
 *
 * @SideEffects:
 * - Нет побочных эффектов.
 *
 * @ForbiddenChanges:
 * - Нельзя удалять selector без согласования.
 *
 * @AllowedRefactorZone:
 * - Внутреннее оформление кода.
 */
// [END_APP_COMPONENT]

import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
