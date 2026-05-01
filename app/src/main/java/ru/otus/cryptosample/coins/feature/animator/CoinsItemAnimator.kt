package ru.otus.cryptosample.coins.feature.animator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * Кастомный аниматор для элементов списка криптовалют.
 *
 * Данный класс расширяет [DefaultItemAnimator], переопределяя логику добавления и удаления элементов.
 * Это позволяет добавить визуальные эффекты (смещение и прозрачность), сохраняя при этом
 * стандартное поведение для перемещения (Move) и изменения (Change) элементов.
 *
 * Основные эффекты:
 * - Добавление: Элемент "выплывает" справа (смещение по X) и плавно проявляется (изменение alpha).
 * - Удаление: Элемент плавно исчезает, "уплывая" влево.
 */
class CoinsItemAnimator : DefaultItemAnimator() {

    companion object {
        /** Смещение по горизонтали в dp для анимаций */
        private const val TRANSLATION_X_DP = 100f

        /** Значение смещения в состоянии покоя */
        private const val IDLE_TRANSLATION_X = 0f

        /** Значение прозрачности для невидимого элемента */
        private const val ALPHA_INVISIBLE = 0f

        /** Значение прозрачности для полностью видимого элемента */
        private const val ALPHA_VISIBLE = 1f
    }

    /**
     * Список ViewHolder-ов, для которых была запрошена анимация добавления,
     * но которые еще не начали анимироваться.
     */
    private val pendingAdditions = mutableListOf<RecyclerView.ViewHolder>()

    /**
     * Список ViewHolder-ов, для которых была запрошена анимация удаления,
     * но которые еще не начали анимироваться.
     */
    private val pendingRemovals = mutableListOf<RecyclerView.ViewHolder>()

    /**
     * Список ViewHolder-ов, которые в данный момент активно анимируются на добавление.
     */
    private val addAnimations = mutableListOf<RecyclerView.ViewHolder>()

    /**
     * Список ViewHolder-ов, которые в данный момент активно анимируются на удаление.
     */
    private val removeAnimations = mutableListOf<RecyclerView.ViewHolder>()

    /**
     * Вспомогательный метод для перевода dp в пиксели для текущего устройства.
     */
    private fun getTranslationX(holder: RecyclerView.ViewHolder): Float {
        return TRANSLATION_X_DP * holder.itemView.context.resources.displayMetrics.density
    }

    /**
     * Вызывается RecyclerView, когда в адаптер добавляется новый элемент.
     * Здесь мы не запускаем анимацию сразу, а только подготавливаем начальное состояние вью.
     *
     * @param holder ViewHolder нового элемента.
     * @return true, чтобы сообщить RecyclerView, что мы берем на себя выполнение анимации.
     */
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        // Сначала сбрасываем все текущие анимации для этого холдера
        resetAnimation(holder)
        
        // Устанавливаем начальное состояние: полностью прозрачный и смещен вправо (в пикселях, расчет через DP)
        holder.itemView.alpha = ALPHA_INVISIBLE
        holder.itemView.translationX = getTranslationX(holder)
        
        // Добавляем в очередь на запуск
        pendingAdditions.add(holder)
        return true
    }

    /**
     * Внутренний метод для запуска фактической анимации добавления.
     * Использует [android.view.ViewPropertyAnimator] для плавного изменения свойств.
     */
    private fun performAnimateAdd(holder: RecyclerView.ViewHolder) {
        addAnimations.add(holder)
        val view = holder.itemView
        val animation = view.animate()

        animation.alpha(ALPHA_VISIBLE)
            .translationX(IDLE_TRANSLATION_X)
            .setDuration(addDuration) // Используем длительность из настроек базового класса
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    // Уведомляем систему о начале анимации (важно для корректной работы RV)
                    dispatchAddStarting(holder)
                }

                override fun onAnimationCancel(animator: Animator) {
                    // В случае отмены принудительно устанавливаем финальное состояние
                    view.alpha = ALPHA_VISIBLE
                    view.translationX = IDLE_TRANSLATION_X
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    // Уведомляем систему, что анимация добавления для этого холдера завершена
                    dispatchAddFinished(holder)
                    addAnimations.remove(holder)
                    // Проверяем, не была ли это последняя активная анимация в системе
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    /**
     * Вызывается RecyclerView, когда элемент удаляется из списка.
     *
     * @param holder ViewHolder удаляемого элемента.
     * @return true, так как мы будем анимировать удаление самостоятельно.
     */
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        pendingRemovals.add(holder)
        return true
    }

    /**
     * Внутренний метод для запуска фактической анимации удаления.
     */
    private fun performAnimateRemove(holder: RecyclerView.ViewHolder) {
        removeAnimations.add(holder)
        val view = holder.itemView
        val animation = view.animate()

        // Смещение влево при исчезновении (в пикселях, расчет через DP)
        animation.alpha(ALPHA_INVISIBLE)
            .translationX(-getTranslationX(holder))
            .setDuration(removeDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationCancel(animator: Animator) {
                    view.alpha = ALPHA_VISIBLE
                    view.translationX = IDLE_TRANSLATION_X
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    // ПЕРЕД завершением возвращаем прозрачность в 1,
                    // чтобы при следующем использовании этого холдера (переиспользовании) он не был невидимым.
                    view.alpha = ALPHA_VISIBLE
                    view.translationX = IDLE_TRANSLATION_X
                    
                    dispatchRemoveFinished(holder)
                    removeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    /**
     * Метод, который запускает выполнение всех отложенных анимаций.
     * RecyclerView вызывает его один раз за цикл отрисовки.
     */
    override fun runPendingAnimations() {
        val removalsNotEmpty = pendingRemovals.isNotEmpty()
        val additionsNotEmpty = pendingAdditions.isNotEmpty()

        // Сначала запускаем базовые анимации (перемещения, изменения), если они были запланированы
        super.runPendingAnimations()

        if (!removalsNotEmpty && !additionsNotEmpty) return

        // Логика запуска: сначала удаляем старые элементы, потом добавляем новые
        
        // Обработка удалений
        if (removalsNotEmpty) {
            val removals = ArrayList(pendingRemovals)
            pendingRemovals.clear()
            removals.forEach { performAnimateRemove(it) }
        }

        // Обработка добавлений
        if (additionsNotEmpty) {
            val additions = ArrayList(pendingAdditions)
            pendingAdditions.clear()
            additions.forEach { performAnimateAdd(it) }
        }
    }

    /**
     * Прерывает все активные и отложенные анимации для конкретного ViewHolder.
     * Вызывается, если RV нужно срочно переиспользовать вью.
     */
    override fun endAnimation(item: RecyclerView.ViewHolder) {
        item.itemView.animate().cancel()
        
        if (pendingRemovals.remove(item)) {
            resetAnimation(item)
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            resetAnimation(item)
            dispatchAddFinished(item)
        }
        if (removeAnimations.remove(item)) {
            resetAnimation(item)
            dispatchRemoveFinished(item)
        }
        if (addAnimations.remove(item)) {
            resetAnimation(item)
            dispatchAddFinished(item)
        }
        super.endAnimation(item)
    }

    /**
     * Проверяет, занят ли аниматор какими-либо задачами в данный момент.
     * Важно для RV, чтобы понимать, когда можно считать список стабильным.
     */
    override fun isRunning(): Boolean {
        return pendingAdditions.isNotEmpty() ||
                pendingRemovals.isNotEmpty() ||
                addAnimations.isNotEmpty() ||
                removeAnimations.isNotEmpty() ||
                super.isRunning()
    }

    /**
     * Мгновенно завершает вообще все текущие и запланированные анимации.
     */
    override fun endAnimations() {
        // Проходим по спискам в обратном порядке и сбрасываем состояние
        for (i in pendingRemovals.indices.reversed()) {
            val item = pendingRemovals[i]
            resetAnimation(item)
            dispatchRemoveFinished(item)
            pendingRemovals.removeAt(i)
        }
        for (i in pendingAdditions.indices.reversed()) {
            val item = pendingAdditions[i]
            resetAnimation(item)
            dispatchAddFinished(item)
            pendingAdditions.removeAt(i)
        }
        for (i in removeAnimations.indices.reversed()) {
            val item = removeAnimations[i]
            item.itemView.animate().cancel()
            removeAnimations.removeAt(i)
        }
        for (i in addAnimations.indices.reversed()) {
            val item = addAnimations[i]
            item.itemView.animate().cancel()
            addAnimations.removeAt(i)
        }
        super.endAnimations()
    }

    /**
     * Вспомогательный метод для сброса трансформаций вью к стандартным значениям.
     */
    private fun resetAnimation(holder: RecyclerView.ViewHolder) {
        holder.itemView.alpha = ALPHA_VISIBLE
        holder.itemView.translationX = IDLE_TRANSLATION_X
    }

    /**
     * Финальное уведомление RecyclerView, вызывается когда все списки анимаций пусты.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }
}
