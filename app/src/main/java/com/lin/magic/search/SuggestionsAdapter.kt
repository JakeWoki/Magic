package com.lin.magic.search

import com.lin.magic.R
import com.lin.magic.database.Bookmark
import com.lin.magic.database.HistoryEntry
import com.lin.magic.database.SearchSuggestion
import com.lin.magic.database.WebPage
import com.lin.magic.database.bookmark.BookmarkRepository
import com.lin.magic.database.history.HistoryRepository
import com.lin.magic.browser.di.DatabaseScheduler
import com.lin.magic.browser.di.MainScheduler
import com.lin.magic.browser.di.NetworkScheduler
import com.lin.magic.browser.di.injector
import com.lin.magic.extensions.drawable
import com.lin.magic.preference.UserPreferences
import com.lin.magic.rx.join
import com.lin.magic.search.suggestions.NoOpSuggestionsRepository
import com.lin.magic.search.suggestions.SuggestionsRepository
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.Locale
import javax.inject.Inject

class SuggestionsAdapter(
    context: Context,
    private val isIncognito: Boolean
) : BaseAdapter(), Filterable {

    private var filteredList: List<WebPage> = emptyList()

    @Inject internal lateinit var bookmarkRepository: BookmarkRepository
    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject internal lateinit var historyRepository: HistoryRepository
    @Inject @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler
    @Inject @field:NetworkScheduler
    internal lateinit var networkScheduler: Scheduler
    @Inject @field:MainScheduler
    internal lateinit var mainScheduler: Scheduler
    @Inject internal lateinit var searchEngineProvider: SearchEngineProvider

    private var allBookmarks: List<Bookmark.Entry> = emptyList()
    private val searchFilter = SearchFilter(this)

    private val searchIcon = context.drawable(R.drawable.ic_search)
    private val webPageIcon = context.drawable(R.drawable.ic_history)
    private val bookmarkIcon = context.drawable(R.drawable.ic_bookmark)
    private var suggestionsRepository: SuggestionsRepository

    /**
     * The listener that is fired when the insert button on a [SearchSuggestion] is clicked.
     */
    var onSuggestionInsertClick: ((WebPage) -> Unit)? = null

    private val onClick = View.OnClickListener {
        onSuggestionInsertClick?.invoke(it.tag as WebPage)
    }

    private val layoutInflater = LayoutInflater.from(context)

    init {
        context.injector.inject(this)

        suggestionsRepository = if (isIncognito) {
            NoOpSuggestionsRepository()
        } else {
            searchEngineProvider.provideSearchSuggestions()
        }

        refreshBookmarks()

        searchFilter.input().results()
            .subscribeOn(databaseScheduler)
            .observeOn(mainScheduler)
            .subscribe(::publishResults)
    }

    fun refreshPreferences() {
        suggestionsRepository = if (isIncognito) {
            NoOpSuggestionsRepository()
        } else {
            searchEngineProvider.provideSearchSuggestions()
        }
    }

    fun refreshBookmarks() {
        bookmarkRepository.getAllBookmarksSorted()
            .subscribeOn(databaseScheduler)
            .subscribe { list ->
                allBookmarks = list
            }
    }

    override fun getCount(): Int = filteredList.size

    override fun getItem(position: Int): Any? {
        if (position > filteredList.size || position < 0) {
            return null
        }
        return filteredList[position]
    }

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: SuggestionViewHolder
        val finalView: View

        if (convertView == null) {
            finalView = layoutInflater.inflate(R.layout.two_line_autocomplete, parent, false)

            holder = SuggestionViewHolder(finalView)
            finalView.tag = holder
        } else {
            finalView = convertView
            holder = convertView.tag as SuggestionViewHolder
        }
        val webPage: WebPage = filteredList[position]

        holder.titleView.text = webPage.title
        holder.urlView.text = webPage.url

        val image = when (webPage) {
            is Bookmark -> bookmarkIcon
            is SearchSuggestion -> searchIcon
            is HistoryEntry -> webPageIcon
        }

        holder.imageView.setImageDrawable(image)

        holder.insertSuggestion.tag = webPage
        holder.insertSuggestion.setOnClickListener(onClick)

        return finalView
    }

    override fun getFilter(): Filter = searchFilter

    private fun publishResults(list: List<WebPage>?) {
        if (list == null) {
            notifyDataSetChanged()
            return
        }
        if (list != filteredList) {
            filteredList = list
            notifyDataSetChanged()
        }
    }

    private fun getBookmarksForQuery(query: String): Single<List<Bookmark.Entry>> =
        Single.fromCallable {
            (allBookmarks.filter {
                it.title.lowercase(Locale.getDefault()).startsWith(query)
            } + allBookmarks.filter {
                it.url.contains(query)
            }).distinct().take(MAX_SUGGESTIONS)
        }

    private fun Observable<CharSequence>.results(): Flowable<List<WebPage>> = this
        .toFlowable(BackpressureStrategy.LATEST)
        .map { it.toString().lowercase(Locale.getDefault()).trim() }
        .filter(String::isNotEmpty)
        .share()
        .compose { upstream ->
            val searchEntries = upstream
                .flatMapSingle(suggestionsRepository::resultsForSearch)
                .subscribeOn(networkScheduler)
                .startWithItem(emptyList())
                .share()

            val bookmarksEntries = upstream
                .flatMapSingle(::getBookmarksForQuery)
                .subscribeOn(databaseScheduler)
                .startWithItem(emptyList())
                .share()

            val historyEntries = upstream
                .flatMapSingle(historyRepository::findHistoryEntriesContaining)
                .subscribeOn(databaseScheduler)
                .startWithItem(emptyList())
                .share()

            // Entries priority and ideal count:
            // Bookmarks - 2
            // History - 2
            // Search - 1

            bookmarksEntries
                .join(
                    other = historyEntries,
                    selectorLeft = { bookmarksEntries },
                    selectorRight = { historyEntries },
                    join = ::Pair
                )
                .compose { bookmarksAndHistory ->
                    bookmarksAndHistory.join(
                        other = searchEntries,
                        selectorLeft = { bookmarksAndHistory },
                        selectorRight = { searchEntries }
                    ) { (bookmarks, history), t2 ->
                        Triple(bookmarks, history, t2)
                    }
                }
        }
        .map { (bookmarks, history, searches) ->
            val bookmarkCount =
                MAX_SUGGESTIONS - 2.coerceAtMost(history.size) - 1.coerceAtMost(searches.size)
            val historyCount =
                MAX_SUGGESTIONS - bookmarkCount.coerceAtMost(bookmarks.size) - 1.coerceAtMost(
                    searches.size
                )
            val searchCount =
                MAX_SUGGESTIONS - bookmarkCount.coerceAtMost(bookmarks.size) - historyCount.coerceAtMost(
                    history.size
                )

            bookmarks.take(bookmarkCount) + history.take(historyCount) + searches.take(searchCount)
        }

    companion object {
        private const val MAX_SUGGESTIONS = 5
    }

    private class SearchFilter(
        private val suggestionsAdapter: SuggestionsAdapter
    ) : Filter() {

        private val publishSubject = PublishSubject.create<CharSequence>()

        fun input(): Observable<CharSequence> = publishSubject.hide()

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint?.isBlank() != false) {
                return FilterResults()
            }
            publishSubject.onNext(constraint.trim())

            return FilterResults().apply { count = 1 }
        }

        override fun convertResultToString(resultValue: Any) = (resultValue as WebPage).url

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) =
            suggestionsAdapter.publishResults(null)
    }

}
