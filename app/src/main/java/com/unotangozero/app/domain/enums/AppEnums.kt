package com.unotangozero.app.domain.enums

enum class TaskCategory(val displayName: String, val colorArgb: Long) {
    PERSONAL("Pessoal", 0xFF6200EE),
    WORK("Trabalho", 0xFF03DAC6),
    HEALTH("Saúde", 0xFFCF6679),
    SHOPPING("Compras", 0xFFBB86FC),
    FINANCE("Finanças", 0xFF018786),
    OTHER("Outros", 0xFF666666)
}

enum class Priority(val level: Int, val displayName: String) {
    HIGH(3, "Alta"),
    MEDIUM(2, "Média"),
    LOW(1, "Baixa")
}

enum class RecurrenceType(val displayName: String) {
    NONE("Nenhuma"),
    DAILY("Diária"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    YEARLY("Anual"),
    CUSTOM("Personalizada")
}

enum class ReminderType(val displayName: String) {
    ONE_TIME("Único"),
    RECURRING("Recorrente"),
    LOCATION("Localização"),
    VOICE("Voz")
}

enum class ExpenseCategory(val displayName: String, val colorArgb: Long) {
    FOOD("Alimentação", 0xFFFF6B6B),
    TRANSPORT("Transporte", 0xFF4ECDC4),
    HEALTH("Saúde", 0xFFFFE66D),
    ENTERTAINMENT("Lazer", 0xFF95E1D3),
    UTILITIES("Contas", 0xFFC7CEEA),
    SHOPPING("Compras", 0xFFFFB6B9),
    EDUCATION("Educação", 0xFF8EC5FC),
    PERSONAL_CARE("Cuidados pessoais", 0xFFFF8B94),
    OTHER("Outros", 0xFFBDBDBD)
}

enum class HabitFrequency(val displayName: String) {
    DAILY("Diária"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal")
}

enum class DebtStatus(val displayName: String) {
    PENDING("Pendente"),
    PARTIALLY_PAID("Parcialmente paga"),
    PAID("Paga")
}

enum class DateRange {
    TODAY,
    TOMORROW,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    CUSTOM
}
