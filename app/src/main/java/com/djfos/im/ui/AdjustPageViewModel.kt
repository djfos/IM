package com.djfos.im.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.djfos.im.filter.FilterIdentity
import com.djfos.im.filter.IFilter
import com.djfos.im.model.Draft
import org.opencv.core.Mat


private val TAG = "AdjustPageViewModel"

class AdjustPageViewModel(val draft: Draft, private val origin: Mat) : ViewModel() {
    private var currentFilter: IFilter = FilterIdentity()
    private var image: Mat
    private var history: MutableList<IFilter>

    init {
        image = origin.copy()
        origin.copyTo(image)
        history = draft.history

        if (history.size != 0) {
            currentFilter = history.last()
        }
    }

    /**
     * get the current result
     */
    fun process(): Mat {
        return currentFilter.apply(image)
    }

    /**
     * apply the filter you want to work with now.
     * the history will be maintained automatically
     */
    fun apply(filter: IFilter) {
        draft.history.add(currentFilter)
        image = currentFilter.apply(image)
        currentFilter = filter
    }

    /**
     * fallback somewhere in the history
     */
    fun fallback(index: Int): IFilter {
        require(index >= 0 && index < history.size) { "index $index out of bound, array size ${history.size}" }

        var mat = Mat()
        origin.copyTo(mat)

        var i = 0
        while (i != index) {
            mat = history[i].apply(mat)
            i++
        }

        image = mat
        return history[i]
    }
}

fun Mat.copy(): Mat {
    val copy = Mat()
    this.copyTo(copy)
    return copy
}

class Factory(val draft: Draft, private val origin: Mat) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdjustPageViewModel(draft, origin) as T
    }
}



