package ru.apokali4.aptechnayasmena

enum class ShiftStage { DELIVERY, SHELF, CHECKOUT, COMPLETE }

data class ShiftTask(
    val stage: ShiftStage,
    val title: String,
    val scene: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val rewardXp: Int
)

object ShiftFlow {
    val steps = listOf(
        ShiftTask(
            ShiftStage.DELIVERY,
            "Приёмка поставки",
            "Курьер привёз коробку жаропонижающего. В накладной одна серия, на упаковке другая. Что делаешь?",
            listOf("Принимаю, потом разберусь с документами", "Останавливаю приёмку, сверяю серию и документы, сообщаю ответственному", "Меняю номер серии в журнале вручную"),
            1,
            "Несовпадение серии нельзя игнорировать или исправлять самостоятельно. Поставку нужно приостановить и передать вопрос ответственному сотруднику.",
            20
        ),
        ShiftTask(
            ShiftStage.SHELF,
            "Размещение товара",
            "На полке уже есть упаковки того же препарата. Новая партия пришла с более поздним сроком годности. Как разместить товар?",
            listOf("Поставить новую партию впереди старой", "Смешать упаковки, чтобы полка выглядела аккуратно", "Разместить с учётом FEFO: раньше истекающий срок должен отпускаться раньше"),
            2,
            "FEFO означает First Expired, First Out: первым используется товар с ближайшим сроком годности. Серии и сроки при этом сохраняют прослеживаемость.",
            20
        ),
        ShiftTask(
            ShiftStage.CHECKOUT,
            "Работа на кассе",
            "Покупатель просит антибиотик без рецепта, потому что «в прошлый раз помогло». Как действовать?",
            listOf("Продать маленькую упаковку", "Отпустить аналогичный препарат", "Проверить рецептурный статус и действовать по правилам отпуска, при необходимости пригласить фармацевта"),
            2,
            "Нельзя подбирать антибиотик по прошлому опыту или заменять назначение. Нужно соблюдать правила отпуска и направить покупателя к специалисту.",
            30
        )
    )
}

data class ShiftChoiceResult(
    val correct: Boolean,
    val nextStage: ShiftStage,
    val completedCount: Int,
    val explanation: String,
    val rewardXp: Int
)

class ShiftProgress {
    private var index = 0
    val stage: ShiftStage get() = ShiftFlow.steps.getOrNull(index)?.stage ?: ShiftStage.COMPLETE
    val completedCount: Int get() = index

    fun choose(optionIndex: Int): ShiftChoiceResult {
        val task = ShiftFlow.steps.getOrNull(index)
        if (task == null) return ShiftChoiceResult(false, ShiftStage.COMPLETE, index, "Смена уже завершена.", 0)
        val isCorrect = optionIndex in task.options.indices && optionIndex == task.correctIndex
        if (isCorrect) index++
        return ShiftChoiceResult(
            isCorrect,
            stage,
            index,
            if (isCorrect) task.explanation else "Действие небезопасно или нарушает порядок работы. Попробуй ещё раз.",
            if (isCorrect) task.rewardXp else 0
        )
    }
}
