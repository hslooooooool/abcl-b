package qsos.base.chat.utils

import androidx.recyclerview.widget.RecyclerView

object RecycleViewUtils {

    fun isSlideToBottom(recyclerView: RecyclerView): Boolean {
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()
    }
}
