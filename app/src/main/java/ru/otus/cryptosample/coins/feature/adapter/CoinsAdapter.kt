package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.databinding.ItemCategoryHeaderBinding
import ru.otus.cryptosample.databinding.ItemCoinBinding
import ru.otus.cryptosample.databinding.ItemHorizontalCoinsBinding

/**
 * Основной адаптер для отображения списка криптовалют.
 * Поддерживает отображение заголовков, карточек монет в сетке и горизонтальных лент.
 *
 * @param viewPool Пул вью для переиспользования холдеров монет между основным и вложенными списками.
 */
class CoinsAdapter(
    private val viewPool: RecyclerView.RecycledViewPool
) : ListAdapter<CoinsAdapterItem, RecyclerView.ViewHolder>(CoinsDiffCallback()) {

    companion object {
        /** Тип представления для заголовка категории */
        const val VIEW_TYPE_CATEGORY = 10
        /** Тип представления для карточки монеты в сетке */
        const val VIEW_TYPE_COIN_GRID = 11
        /** Тип представления для вложенного горизонтального списка */
        const val VIEW_TYPE_HORIZONTAL_LIST = 12
        /** Тип представления для карточки монеты в горизонтальном списке */
        const val VIEW_TYPE_COIN_HORIZONTAL = 13

        /** Ширина карточки монеты в горизонтальном списке в dp */
        const val COIN_ITEM_WIDTH_DP = 160
        /** Порог количества монет, после которого категория становится горизонтальной */
        const val HORIZONTAL_THRESHOLD = 10
    }

    /**
     * Преобразует данные категорий в список элементов адаптера.
     * Если в категории более [HORIZONTAL_THRESHOLD] монет, используется горизонтальный список.
     */
    fun setData(categories: List<CoinCategoryState>) {
        val adapterItems = mutableListOf<CoinsAdapterItem>()

        categories.forEach { category ->
            adapterItems.add(CoinsAdapterItem.CategoryHeader(category.name))
            if (category.coins.size > HORIZONTAL_THRESHOLD) {
                adapterItems.add(CoinsAdapterItem.HorizontalCoinsItem(category.id, category.coins))
            } else {
                category.coins.forEach { coin ->
                    adapterItems.add(CoinsAdapterItem.CoinItem(coin))
                }
            }
        }

        submitList(adapterItems)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> VIEW_TYPE_CATEGORY
            is CoinsAdapterItem.CoinItem -> VIEW_TYPE_COIN_GRID
            is CoinsAdapterItem.HorizontalCoinsItem -> VIEW_TYPE_HORIZONTAL_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryHeaderViewHolder(
                ItemCategoryHeaderBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_COIN_GRID -> CoinViewHolder(
                ItemCoinBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_HORIZONTAL_LIST -> HorizontalCoinsViewHolder(
                ItemHorizontalCoinsBinding.inflate(inflater, parent, false),
                viewPool
            )
            else -> throw IllegalArgumentException("Неизвестный тип вью: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> (holder as CategoryHeaderViewHolder).bind(item.categoryName)
            is CoinsAdapterItem.CoinItem -> (holder as CoinViewHolder).bind(item.coin)
            is CoinsAdapterItem.HorizontalCoinsItem -> (holder as HorizontalCoinsViewHolder).bind(item.coins)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = getItem(position)
            if (item is CoinsAdapterItem.CoinItem && holder is CoinViewHolder) {
                payloads.forEach { payload ->
                    if (payload == HorizontalCoinsAdapter.PAYLOAD_HIGHLIGHT) {
                        holder.updateHighlight(item.coin.highlight)
                    }
                }
            } else {
                onBindViewHolder(holder, position)
            }
        }
    }

    /**
     * Callback для вычисления разницы между элементами списка.
     */
    class CoinsDiffCallback : DiffUtil.ItemCallback<CoinsAdapterItem>() {
        override fun areItemsTheSame(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Boolean {
            return when {
                oldItem is CoinsAdapterItem.CategoryHeader && newItem is CoinsAdapterItem.CategoryHeader ->
                    oldItem.categoryName == newItem.categoryName
                oldItem is CoinsAdapterItem.CoinItem && newItem is CoinsAdapterItem.CoinItem ->
                    oldItem.coin.id == newItem.coin.id
                oldItem is CoinsAdapterItem.HorizontalCoinsItem && newItem is CoinsAdapterItem.HorizontalCoinsItem ->
                    oldItem.categoryId == newItem.categoryId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Any? {
            if (oldItem is CoinsAdapterItem.CoinItem && newItem is CoinsAdapterItem.CoinItem) {
                if (oldItem.coin.highlight != newItem.coin.highlight) {
                    return HorizontalCoinsAdapter.PAYLOAD_HIGHLIGHT
                }
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}