package ru.otus.cryptosample.coins.feature.adapter

import ru.otus.cryptosample.coins.feature.CoinState

/**
 * Типы элементов для адаптера списка монет.
 */
sealed class CoinsAdapterItem {
    /**
     * Заголовок категории.
     * @property categoryName Название категории.
     */
    data class CategoryHeader(val categoryName: String) : CoinsAdapterItem()

    /**
     * Элемент отдельной монеты для отображения в сетке.
     * @property coin Данные о монете.
     */
    data class CoinItem(val coin: CoinState) : CoinsAdapterItem()

    /**
     * Элемент для горизонтального списка монет.
     * Используется, когда в категории более 10 элементов.
     * @property categoryId Идентификатор категории.
     * @property coins Список монет в категории.
     */
    data class HorizontalCoinsItem(val categoryId: String, val coins: List<CoinState>) : CoinsAdapterItem()
}