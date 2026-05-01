package ru.otus.cryptosample.coins.feature

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.otus.cryptosample.CoinsSampleApp
import ru.otus.cryptosample.coins.feature.adapter.CoinsAdapter
import ru.otus.cryptosample.coins.feature.animator.CoinsItemAnimator
import ru.otus.cryptosample.coins.feature.di.DaggerCoinListComponent
import ru.otus.cryptosample.databinding.FragmentCoinListBinding
import javax.inject.Inject

/**
 * Фрагмент для отображения списка криптовалют.
 * 
 * Реализует отображение категорий с монетами, поддерживая сеточное отображение
 * и вложенные горизонтальные списки для категорий с большим количеством элементов.
 */
class CoinListFragment : Fragment() {

    companion object {
        /** Количество колонок в основной сетке */
        private const val SPAN_COUNT = 2
        /** Размер элемента на всю ширину сетки */
        private const val FULL_SPAN = 2
        /** Размер элемента в одну колонку */
        private const val SINGLE_SPAN = 1
    }

    private var _binding: FragmentCoinListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var factory: CoinListViewModelFactory

    private val viewModel: CoinListViewModel by viewModels { factory }

    private lateinit var coinsAdapter: CoinsAdapter
    
    /**
     * Общий пул для переиспользования холдеров между основным списком и горизонтальными лентами.
     * Позволяет значительно экономить память и сглаживать прокрутку.
     */
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val appComponent = (activity?.applicationContext as CoinsSampleApp).appComponent

        DaggerCoinListComponent.factory()
            .create(appComponent)
            .inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChipToggle()
        subscribeUI()
    }

    /**
     * Настраивает основной RecyclerView: устанавливает GridLayoutManager, 
     * адаптирует SpanSizeLookup под типы данных, подключает кастомный аниматор и общий ViewPool.
     */
    private fun setupRecyclerView() {
        coinsAdapter = CoinsAdapter(sharedViewPool)

        val gridLayoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (coinsAdapter.getItemViewType(position)) {
                    CoinsAdapter.VIEW_TYPE_CATEGORY,
                    CoinsAdapter.VIEW_TYPE_HORIZONTAL_LIST -> FULL_SPAN
                    CoinsAdapter.VIEW_TYPE_COIN_GRID -> SINGLE_SPAN
                    else -> SINGLE_SPAN
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = coinsAdapter
            // Установка кастомного аниматора для обработки добавлений и удалений
            itemAnimator = CoinsItemAnimator()
            // Установка общего пула для всех вложенных списков
            setRecycledViewPool(sharedViewPool)
        }
    }

    private fun setupChipToggle() {
        binding.highlightChip.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onHighlightMoversToggled(isChecked)
        }

        binding.showAllChip.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onShowAllToggled(isChecked)
        }
    }

    /**
     * Подписывается на поток состояний из ViewModel для обновления списка.
     */
    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    coinsAdapter.setData(state.categories)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
