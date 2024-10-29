package com.lin.magic.browser.search

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

/**
 * Listens for actions on the search bar.
 *
 * @param onConfirm Invoked when the user has confirmed what they are searching for.
 * @param inputMethodManager Used to manage keyboard visibility.
 */
class SearchListener(
    private val onConfirm: () -> Unit,
    private val inputMethodManager: InputMethodManager
) : View.OnKeyListener, TextView.OnEditorActionListener {

    override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
        if (keyEvent.action != KeyEvent.ACTION_UP) {
            return false
        }
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                onConfirm()
                view.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                true
            }
            else -> false
        }
    }

    override fun onEditorAction(view: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO
            || actionId == EditorInfo.IME_ACTION_DONE
            || actionId == EditorInfo.IME_ACTION_NEXT
            || actionId == EditorInfo.IME_ACTION_SEND
            || actionId == EditorInfo.IME_ACTION_SEARCH
            || event?.action == KeyEvent.KEYCODE_ENTER
        ) {
            onConfirm()
            view.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            return true
        }
        return false
    }
}
