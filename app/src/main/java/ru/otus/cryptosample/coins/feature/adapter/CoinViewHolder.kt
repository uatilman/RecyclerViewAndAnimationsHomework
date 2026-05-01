package ru.otus.cryptosample.coins.feature.adapter

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.otus.cryptosample.R
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.databinding.ItemCoinBinding

/**
 * ViewHolder для отображения информации о конкретной монете.
 */
class CoinViewHolder(
    private val binding: ItemCoinBinding
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Привязывает данные монеты к вью.
     *
     * @param coin Состояние монеты для отображения.
     */
    fun bind(coin: CoinState) {
        with(binding) {
            coinName.text = coin.name
            coinPrice.text = coin.price
            coinChange.text = coin.discount

            val changeColor = if (coin.goesUp) {
                ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
            }
            coinChange.setTextColor(changeColor)

            coinIcon.load(coin.image) {
                placeholder(R.drawable.generic)
                error(R.drawable.generic)
            }

            fireBadge.isVisible = coin.highlight
        }
    }

    /**
     * Частичное обновление: меняет только видимость бейджика "горячей" монеты.
     * Используется при обработке payloads для избежания полной перерисовки элемента.
     *
     * @param highlight Нужно ли отображать бейджик 🔥.
     */
    fun updateHighlight(highlight: Boolean) {
        binding.fireBadge.isVisible = highlight
    }
}