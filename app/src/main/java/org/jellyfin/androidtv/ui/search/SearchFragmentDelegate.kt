package org.jellyfin.androidtv.ui.search

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Row
import org.jellyfin.androidtv.constant.QueryType
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.itemhandling.ItemRowAdapter
import org.jellyfin.androidtv.ui.presentation.CardPresenter
import org.jellyfin.androidtv.ui.presentation.CustomListRowPresenter
import org.jellyfin.androidtv.ui.presentation.MutableObjectAdapter

class SearchFragmentDelegate(
    private val context: Context,
    private val backgroundService: BackgroundService,
) {
    val rowsAdapter = MutableObjectAdapter<Row>(CustomListRowPresenter())

    protected var currentItem: BaseRowItem? = null

    private val handler: Handler = Handler(Looper.getMainLooper())

    fun showResults(searchResultGroups: Collection<SearchResultGroup>) {
        rowsAdapter.clear()
        val adapters = mutableListOf<ItemRowAdapter>()
        for ((labelRes, baseItems) in searchResultGroups) {
            val adapter = ItemRowAdapter(
                context,
                baseItems.toList(),
                CardPresenter(),
                rowsAdapter,
                QueryType.Search
            ).apply {
                setRow(ListRow(HeaderItem(context.getString(labelRes)), this))
            }
            adapters.add(adapter)
        }
        for (adapter in adapters) adapter.Retrieve()
    }

    val onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
        if (item !is BaseRowItem) return@OnItemViewClickedListener
        row as ListRow
        val adapter = row.adapter as ItemRowAdapter
        ItemLauncher.launch(item as BaseRowItem?, adapter, item.index, context)
    }

    val onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
        if (item !is BaseRowItem) {
            currentItem = null
        } else {
            currentItem = item
            val itemFinal = currentItem
            handler.postDelayed({
                if (itemFinal == currentItem)
                    backgroundService.setBackground(currentItem!!.baseItem)
            }, 500)
        }
    }
}
