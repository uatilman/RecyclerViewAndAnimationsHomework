package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.databinding.ItemCoinBinding

/**
 * Адаптер для горизонтального списка монет.
 * Используется для отображения ленты монет в категориях с количеством элементов более 10.
 */
class HorizontalCoinsAdapter : ListAdapter<CoinState, CoinViewHolder>(CoinDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        // Используем специфичный тип для горизонтального списка.
        // Это предотвращает переиспользование холдеров из сетки (match_parent) в ленте через общий ViewPool.
        return CoinsAdapter.VIEW_TYPE_COIN_HORIZONTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val binding = ItemCoinBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        // Устанавливаем фиксированную ширину из константы CoinsAdapter.COIN_ITEM_WIDTH_DP
        val density = parent.context.resources.displayMetrics.density
        val width = (CoinsAdapter.COIN_ITEM_WIDTH_DP * density).toInt()
        
        val layoutParams = binding.root.layoutParams
        layoutParams.width = width
        binding.root.layoutParams = layoutParams

        return CoinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach { payload ->
                if (payload == PAYLOAD_HIGHLIGHT) {
                    // Частичное обновление: только бейдж огня
                    holder.updateHighlight(getItem(position).highlight)
                }
            }
        }
    }

    /**
     * Сравнение элементов для оптимизации обновления списка через DiffUtil.
     */
    class CoinDiffCallback : DiffUtil.ItemCallback<CoinState>() {
        override fun areItemsTheSame(oldItem: CoinState, newItem: CoinState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoinState, newItem: CoinState): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: CoinState, newItem: CoinState): Any? {
            // Если изменилось только состояние подсветки, возвращаем соответствующий payload
            return if (oldItem.highlight != newItem.highlight) {
                PAYLOAD_HIGHLIGHT
            } else {
                null
            }
        }
    }

    companion object {
        /** Ключ для частичного обновления (подсветка монеты) */
        const val PAYLOAD_HIGHLIGHT = "PAYLOAD_HIGHLIGHT"
    }
}
