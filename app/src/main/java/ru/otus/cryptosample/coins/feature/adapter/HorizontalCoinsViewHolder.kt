package ru.otus.cryptosample.coins.feature.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.databinding.ItemHorizontalCoinsBinding

/**
 * ViewHolder для отображения горизонтального списка монет внутри основного вертикального списка.
 * Используется для категорий, содержащих более 10 элементов.
 *
 * @param binding Привязка макета для горизонтального списка.
 * @param viewPool Общий пул вью для оптимизации производительности вложенного списка.
 */
class HorizontalCoinsViewHolder(
    private val binding: ItemHorizontalCoinsBinding,
    private val viewPool: RecyclerView.RecycledViewPool
) : RecyclerView.ViewHolder(binding.root) {

    private val adapter = HorizontalCoinsAdapter()

    init {
        binding.horizontalRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = this@HorizontalCoinsViewHolder.adapter
            // Устанавливаем общий пул вью для переиспользования холдеров
            setRecycledViewPool(viewPool)
            // Оптимизация: фиксированный размер, так как высота списка обычно не меняется
            setHasFixedSize(true)
        }
    }

    /**
     * Обновляет данные во вложенном адаптере.
     *
     * @param coins Список монет для отображения.
     */
    fun bind(coins: List<CoinState>) {
        adapter.submitList(coins)
    }
}