/**
 * Эталонные фрагменты семантической разметки для JavaScript (справочник).
 */

// --- Навигация и контраст (как в TS, комментарии //) ---
/*
 * ANCHOR: Клиентский мост к runtime расширения
 * PURPOSE: Отправка сообщений в service worker с таймаутом.
 * @see: background.js -> onMessage
 * @see: ../shared/protocol.js -> MESSAGE_TYPES
 */
function sendToBackground(payload) {
  return chrome.runtime.sendMessage(payload);
}
// END_ANCHOR

// ANCHOR_DO:
// - Используй async/await.
// - Обрабатывай lastError от chrome.runtime.
// END_ANCHOR_DO

// ANCHOR_DONT:
// - Не полагай на глобальный singleton без явной инициализации.
// END_ANCHOR_DONT

// --- Семантический чанк ---
// === CHUNK: CONTENT_BRIDGE_V1 [MESSAGING] ===
// Описание: Обёртка над sendMessage для content script.
// Dependencies: PROTOCOL_V1
export function bridgePing() {
  return sendToBackground({ type: "PING" });
}
// === END_CHUNK: CONTENT_BRIDGE_V1 ===
