package com.lin.magic.html

import io.reactivex.rxjava3.core.Single

/**
 * A factory that builds an HTML page.
 */
interface HtmlPageFactory {

    /**
     * Build the HTML page and emit the URL.
     */
    fun buildPage(): Single<String>

}
