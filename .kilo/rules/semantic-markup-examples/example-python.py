# Канонический пример семантической разметки для Python.
# Это справочник, не исполнять как модуль.

from typing import Dict, Optional, Any
import logging

def log_line(
    module: str,
    level: str,
    function_name: str,
    anchor: str,
    point: str,
    data: Dict[str, Any]
) -> None:
    """Заглушка для логирования. В проекте использовать из core/logging.py"""
    pass


# === CHUNK: SEMANTIC_EXAMPLE [REFERENCE] ===
# Описание: Показ полного каркаса функции с контрактом и AI-friendly логированием.
# Dependencies: (none)

# [START_PROCESS_TRANSACTION]
"""
ANCHOR: PROCESS_TRANSACTION
PURPOSE: Обработка финансовой транзакции между счетами.

@PreConditions:
- amount: положительное число > 0
- source_account: существует и активен
- target_account: существует и активен
- source_account.balance >= amount

@PostConditions:
- при успехе: возвращается { success: true, transaction_id: str }
- при нехватке средств: возвращается { success: false, error: "INSUFFICIENT_FUNDS" }
- при неактивном счёте: возвращается { success: false, error: "ACCOUNT_INACTIVE" }

@Invariants:
- общая сумма денег в системе не изменяется (только перераспределение)
- баланс никогда не становится отрицательным
- каждая транзакция имеет уникальный ID

@SideEffects:
- списывает amount с source_account.balance
- зачисляет amount на target_account.balance
- создаёт запись transaction в БД
- отправляет уведомления обоим владельцам счетов

@ForbiddenChanges:
- нельзя убрать проверку баланса ДО списания (критично!)
- нельзя допускать овердрафт (бизнес-ограничение)
- нельзя проводить транзакцию с неактивным счётом (безопасность)
- нельзя менять сумму транзакции после начала обработки

@AllowedRefactorZone:
- можно вынести валидацию в отдельные методы
- можно изменить формат уведомлений
- можно добавить логирование дополнительных полей
"""
def process_transaction(
    source_account_id: str,
    target_account_id: str,
    amount: float,
    description: str = ""
) -> Dict[str, Any]:
    log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "ENTRY", {
        "source_account_id": source_account_id,
        "target_account_id": target_account_id,
        "amount": amount,
        "description": description,
    })

    # Валидация суммы
    if amount <= 0:
        log_line("transactions", "WARN", "process_transaction", "PROCESS_TRANSACTION", "ERROR", {
            "reason": "invalid_amount",
            "amount": amount,
            "constraint": "amount_must_be_positive",
        })
        log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
            "result": "rejected",
            "error": "INVALID_AMOUNT",
        })
        return {"success": False, "error": "INVALID_AMOUNT"}

    # Проверка исходного счёта
    source_account = get_account(source_account_id)
    log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "CHECK", {
        "check": "source_account_exists",
        "result": source_account is not None,
        "account_id": source_account_id,
    })

    if not source_account:
        log_line("transactions", "WARN", "process_transaction", "PROCESS_TRANSACTION", "DECISION", {
            "decision": "reject_missing_source",
            "branch": "account_not_found",
            "account_id": source_account_id,
        })
        log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
            "result": "rejected",
            "error": "ACCOUNT_NOT_FOUND",
        })
        return {"success": False, "error": "ACCOUNT_NOT_FOUND"}

    # Проверка активности счёта
    if not source_account.get("is_active", False):
        log_line("transactions", "WARN", "process_transaction", "PROCESS_TRANSACTION", "DECISION", {
            "decision": "reject_inactive_source",
            "branch": "account_inactive",
            "account_id": source_account_id,
        })
        log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
            "result": "rejected",
            "error": "ACCOUNT_INACTIVE",
        })
        return {"success": False, "error": "ACCOUNT_INACTIVE"}

    # Проверка достаточности средств (КРИТИЧНО!)
    balance = source_account.get("balance", 0)
    log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "CHECK", {
        "check": "sufficient_funds",
        "balance": balance,
        "amount": amount,
        "sufficient": balance >= amount,
    })

    if balance < amount:
        log_line("transactions", "WARN", "process_transaction", "PROCESS_TRANSACTION", "DECISION", {
            "decision": "reject_insufficient_funds",
            "branch": "insufficient_balance",
            "balance": balance,
            "amount": amount,
            "shortfall": amount - balance,
        })
        log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
            "result": "rejected",
            "error": "INSUFFICIENT_FUNDS",
        })
        return {"success": False, "error": "INSUFFICIENT_FUNDS"}

    # Получение целевого счёта
    target_account = get_account(target_account_id)
    if not target_account or not target_account.get("is_active", False):
        log_line("transactions", "WARN", "process_transaction", "PROCESS_TRANSACTION", "DECISION", {
            "decision": "reject_invalid_target",
            "branch": "target_account_invalid",
            "account_id": target_account_id,
        })
        log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
            "result": "rejected",
            "error": "TARGET_ACCOUNT_INVALID",
        })
        return {"success": False, "error": "TARGET_ACCOUNT_INVALID"}

    # Выполнение транзакции (атомарно)
    transaction_id = create_transaction(
        source_account_id,
        target_account_id,
        amount,
        description
    )

    log_line("transactions", "INFO", "process_transaction", "PROCESS_TRANSACTION", "STATE_CHANGE", {
        "entity": "transaction",
        "id": transaction_id,
        "action": "created",
        "source_account_id": source_account_id,
        "target_account_id": target_account_id,
        "amount": amount,
    })

    # Обновление балансов
    update_balance(source_account_id, -amount)
    update_balance(target_account_id, amount)

    log_line("transactions", "INFO", "process_transaction", "PROCESS_TRANSACTION", "STATE_CHANGE", {
        "entity": "account_balance",
        "source_account_id": source_account_id,
        "change": -amount,
        "new_balance": balance - amount,
    })

    log_line("transactions", "INFO", "process_transaction", "PROCESS_TRANSACTION", "STATE_CHANGE", {
        "entity": "account_balance",
        "target_account_id": target_account_id,
        "change": amount,
    })

    # Отправка уведомлений
    send_notification(source_account_id, "transaction_out", amount, transaction_id)
    send_notification(target_account_id, "transaction_in", amount, transaction_id)

    log_line("transactions", "DEBUG", "process_transaction", "PROCESS_TRANSACTION", "EXIT", {
        "result": "success",
        "transaction_id": transaction_id,
    })
    return {"success": True, "transaction_id": transaction_id}

# [END_PROCESS_TRANSACTION]

# === END_CHUNK: SEMANTIC_EXAMPLE ===


# --- Пример legacy-кода с реконструированным контрактом ---

# [START_LEGACY_DISCOUNT_CALC]
# @LEGACY_CONTRACT_RECONSTRUCTED:
# PURPOSE: Расчёт скидки для пользователя по категории и истории покупок.
# 
# @PreConditions:
# - price: положительное число
# - user_id: существующий пользователь
# - category: одна из ["regular", "silver", "gold", "platinum"]
#
# @PostConditions:
# - возвращаемое значение в диапазоне [0, price]
# - для gold и platinum скидка >= 10%
#
# @Invariants:
# - итоговая цена никогда не становится отрицательной
#
# @Ambiguity:
# - Что происходит при неактивном пользователе? (наблюдается возврат 0, но не документировано)
# - Есть ли максимальный размер скидки? (код не показывает лимита)
#
# @OriginalBehavior: legacy/discount.py commit abc123
# @SideEffects:
# - читает историю покупок из БД
# - пишет в audit log при скидке > 1000
#
# @ForbiddenChanges:
# - нельзя убрать скидку для gold/platinum без согласования с бизнесом
#
# @AllowedRefactorZone:
# - можно вынести расчёт коэффициентов в конфиг
# - можно добавить кэширование истории покупок

def calculate_discount(price: float, user_id: str, category: str) -> float:
    # PRESERVED_BEHAVIOR: Оригинальная логика сохранена как есть
    if category == "gold":
        return price * 0.9
    elif category == "platinum":
        return price * 0.85
    elif category == "silver":
        # @SEMANTIC_AMBIGUITY: Почему silver получает 5%? Проверить бизнес-правила
        return price * 0.95
    return price

# [END_LEGACY_DISCOUNT_CALC]


# Заглушки для примера
def get_account(account_id: str) -> Optional[Dict[str, Any]]:
    return {"id": account_id, "balance": 1000.0, "is_active": True}

def create_transaction(source: str, target: str, amount: float, desc: str) -> str:
    return "txn-123"

def update_balance(account_id: str, delta: float) -> None:
    pass

def send_notification(account_id: str, event_type: str, amount: float, txn_id: str) -> None:
    pass
