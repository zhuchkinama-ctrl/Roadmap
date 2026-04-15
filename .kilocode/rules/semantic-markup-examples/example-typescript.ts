/**
 * Канонический пример семантической разметки для TypeScript.
 * Файл вне `tsconfig` include — в реальном коде под `src/` используй
 * `import { logLine } from "../lib/log.js";`.
 */

function logLine(
  module: string,
  level: "DEBUG" | "INFO" | "WARN" | "ERROR",
  functionName: string,
  anchor: string,
  point: string,
  data: Record<string, unknown>
): void {
  /* заглушка: в проекте бери из src/lib/log.ts */
}

// === CHUNK: SEMANTIC_EXAMPLE [REFERENCE] ===
// Описание: Показ полного каркаса функции с контрактом и AI-friendly логированием.
// Dependencies: (none)

// [START_REGISTER_USER]
/*
 * ANCHOR: REGISTER_USER
 * PURPOSE: Регистрация нового пользователя в системе.
 *
 * @PreConditions:
 * - username: непустая строка после trim, min 3 символа
 * - password: непустая строка, min 8 символов
 * - пользователь с таким username не существует
 *
 * @PostConditions:
 * - при успехе: возвращается { success: true, userId: string }
 * - при ошибке валидации: возвращается { success: false, error: "VALIDATION_ERROR" }
 * - при ошибке дубликата: возвращается { success: false, error: "USER_EXISTS" }
 *
 * @Invariants:
 * - база данных остаётся в консистентном состоянии
 * - пароль никогда не хранится в открытом виде
 *
 * @SideEffects:
 * - создаёт запись пользователя в БД
 * - отправляет приветственное письмо
 * - пишет лог регистрации в audit log
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку длины пароля (требование безопасности)
 * - нельзя убрать отправку приветственного письма (бизнес-требование)
 * - нельзя возвращать пароль в ответе (безопасность)
 *
 * @AllowedRefactorZone:
 * - можно менять алгоритм хеширования пароля
 * - можно вынести валидацию в отдельную функцию
 * - можно изменить формат логирования
 */
async function registerUser(
  username: string,
  password: string,
  email: string
): Promise<{ success: boolean; userId?: string; error?: string }> {
  logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "ENTRY", {
    username_len: username.trim().length,
    password_len: password.length,
    email,
  });

  // Валидация имени пользователя
  const trimmedUsername = username.trim();
  if (trimmedUsername.length < 3) {
    logLine("auth", "WARN", "registerUser", "REGISTER_USER", "DECISION", {
      decision: "reject_short_username",
      branch: "validation_failed",
      username_len: trimmedUsername.length,
      min_required: 3,
    });
    logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "EXIT", {
      result: "rejected",
      error: "VALIDATION_ERROR",
    });
    return { success: false, error: "VALIDATION_ERROR" };
  }

  // Валидация пароля
  if (password.length < 8) {
    logLine("auth", "WARN", "registerUser", "REGISTER_USER", "DECISION", {
      decision: "reject_short_password",
      branch: "validation_failed",
      password_len: password.length,
      min_required: 8,
    });
    logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "EXIT", {
      result: "rejected",
      error: "VALIDATION_ERROR",
    });
    return { success: false, error: "VALIDATION_ERROR" };
  }

  // Проверка существования пользователя
  const existingUser = await findUserByUsername(trimmedUsername);
  logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "CHECK", {
    check: "user_exists",
    result: existingUser !== null,
    username: trimmedUsername,
  });

  if (existingUser) {
    logLine("auth", "WARN", "registerUser", "REGISTER_USER", "DECISION", {
      decision: "reject_duplicate_user",
      branch: "user_exists",
      username: trimmedUsername,
    });
    logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "EXIT", {
      result: "rejected",
      error: "USER_EXISTS",
    });
    return { success: false, error: "USER_EXISTS" };
  }

  // Создание пользователя
  const hashedPassword = await hashPassword(password);
  const userId = await createUser(trimmedUsername, hashedPassword, email);

  logLine("auth", "INFO", "registerUser", "REGISTER_USER", "STATE_CHANGE", {
    entity: "user",
    id: userId,
    action: "created",
    username: trimmedUsername,
  });

  // Отправка приветственного письма
  await sendWelcomeEmail(email, trimmedUsername);
  logLine("auth", "INFO", "registerUser", "REGISTER_USER", "STATE_CHANGE", {
    entity: "email",
    action: "sent",
    type: "welcome",
    to: email,
  });

  logLine("auth", "DEBUG", "registerUser", "REGISTER_USER", "EXIT", {
    result: "success",
    userId,
  });
  return { success: true, userId };
}
// [END_REGISTER_USER]

// === END_CHUNK: SEMANTIC_EXAMPLE ===

// Заглушки для примера
async function findUserByUsername(_username: string): Promise<unknown> {
  return null;
}
async function hashPassword(_password: string): Promise<string> {
  return "hashed";
}
async function createUser(
  _username: string,
  _hashedPassword: string,
  _email: string
): Promise<string> {
  return "user-123";
}
async function sendWelcomeEmail(_email: string, _username: string): Promise<void> {}
