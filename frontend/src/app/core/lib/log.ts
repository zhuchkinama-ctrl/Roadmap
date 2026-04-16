/**
 * AI-friendly логирование для семантической разметки.
 */

export function logLine(
  module: string,
  level: "DEBUG" | "INFO" | "WARN" | "ERROR",
  functionName: string,
  anchor: string,
  point: string,
  data: Record<string, unknown>
): void {
  const timestamp = new Date().toISOString();
  const logEntry = {
    timestamp,
    module,
    level,
    function: functionName,
    anchor,
    point,
    ...data
  };

  // В продакшене можно отправлять на сервер или в сервис логирования
  if (level === "ERROR" || level === "WARN") {
    console.error(JSON.stringify(logEntry));
  } else {
    console.log(JSON.stringify(logEntry));
  }
}
