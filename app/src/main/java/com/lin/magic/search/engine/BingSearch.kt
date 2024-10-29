package com.lin.magic.search.engine

import com.lin.magic.R

/**
 * The Bing search engine.
 *
 * See http://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Bing_logo_%282013%29.svg/500px-Bing_logo_%282013%29.svg.png
 * for the icon.
 */
class BingSearch : BaseSearchEngine(
    "file:///android_asset/bing.png",
    "https://www.bing.com/search?q=",
    R.string.search_engine_bing
)
