/**
 * Пример семантической разметки для Angular компонента.
 * Файл находится в каталоге .kilocode/rules/semantic-markup-examples и служит справочником.
 */

import { Component, OnInit } from '@angular/core';
import { logLine } from '../lib/log'; // предположительный путь к функции логирования

// === CHUNK: EXAMPLE_ANGULAR_COMPONENT [UI] ===
// Описание: Простой компонент, отображающий список задач.
// Dependencies: Angular Core, logLine

// [START_EXAMPLE_ANGULAR_COMPONENT]
/*
 * ANCHOR: EXAMPLE_ANGULAR_COMPONENT
 * PURPOSE: Демонстрация семантической разметки в Angular компоненте.
 *
 * @PreConditions:
 * - компонент инициализирован Angular фреймворком
 * - сервисы, если нужны, предоставлены через DI
 *
 * @PostConditions:
 * - после ngOnInit список задач загружен в поле tasks
 * - в консоль записан лог ENTRY и EXIT
 *
 * @Invariants:
 * - массив tasks всегда массив объектов с полем title:string
 * - компонент не меняет глобальное состояние приложения
 *
 * @SideEffects:
 * - запись в журнал через logLine (нет побочных эффектов в бизнес‑логике)
 *
 * @ForbiddenChanges:
 * - нельзя удалить вызов logLine, т.к. он фиксирует поведение
 * - нельзя изменить структуру ANCHOR, иначе нарушится связь с графом
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения задач (например, заменить setTimeout на HttpClient)
 */
@Component({
  selector: 'app-example',
  template: `<ul><li *ngFor="let task of tasks">{{ task.title }}</li></ul>`,
  styles: []
})
export class ExampleComponent implements OnInit {
  tasks: Array<{ title: string }> = [];

  ngOnInit(): void {
    logLine('ui', 'DEBUG', 'ngOnInit', 'EXAMPLE_ANGULAR_COMPONENT', 'ENTRY', {});
    // Симуляция загрузки данных
    setTimeout(() => {
      this.tasks = [{ title: 'Задача 1' }, { title: 'Задача 2' }];
      logLine('ui', 'DEBUG', 'ngOnInit', 'EXAMPLE_ANGULAR_COMPONENT', 'EXIT', { loaded: true });
    }, 0);
  }
}
// [END_EXAMPLE_ANGULAR_COMPONENT]
// === END_CHUNK: EXAMPLE_ANGULAR_COMPONENT ===
