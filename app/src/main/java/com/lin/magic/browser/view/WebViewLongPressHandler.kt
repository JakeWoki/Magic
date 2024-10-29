package com.lin.magic.browser.view

import com.lin.magic.browser.view.targetUrl.LongPress
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import io.reactivex.rxjava3.functions.Cancellable
import javax.inject.Inject

/**
 * Handles long presses on a [WebView] and converts them into [LongPress] events.
 */
class WebViewLongPressHandler @Inject constructor(private val activity: Activity) {

    /**
     * Configure the provided [webView] for listening to long press events and invoke [onLongClick]
     * whenever a long press is detected.
     */
    fun configure(
        webView: WebView,
        onLongClick: (LongPress) -> Unit
    ): Cancellable {
        webView.setCompositeTouchListener(
            "long_press", GestureTriggeringTouchListener(
                GestureDetector(
                    activity,
                    CustomGestureListener(
                        messageHandler = MessageHandler {
                            val hitTestResult = webView.hitTestResult
                            val longPress = LongPress(
                                targetUrl = it ?: hitTestResult.extra,
                                hitUrl = hitTestResult.extra,
                                hitCategory = hitTestResult.type.asLongPressCategory()
                            )
                            onLongClick(longPress)
                        },
                        webView = webView
                    )
                )
            )
        )

        return Cancellable {
            webView.setCompositeTouchListener("long_press", null)
        }
    }

    private fun Int.asLongPressCategory(): LongPress.Category = when (this) {
        WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE,
        WebView.HitTestResult.IMAGE_TYPE -> LongPress.Category.IMAGE
        WebView.HitTestResult.UNKNOWN_TYPE -> LongPress.Category.UNKNOWN
        else -> LongPress.Category.LINK
    }

    private class GestureTriggeringTouchListener(
        private val gestureDetector: GestureDetector
    ) : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)
            return false
        }
    }

    private class MessageHandler(
        private val onUrlLongPress: (String?) -> Unit
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) = onUrlLongPress(msg.data.getString(KEY_URL))

        companion object {
            private const val KEY_URL = "url"
        }
    }

    private class CustomGestureListener(
        private val messageHandler: MessageHandler,
        private val webView: WebView
    ) : GestureDetector.SimpleOnGestureListener() {

        /**
         * Without this, onLongPress is not called when user is zooming using two fingers, but is
         * when using only one.
         *
         * The required behaviour is to avoid triggering this when the user is zooming, it shouldn't
         * matter how many fingers the user is using.
         */
        private var canTriggerLongPress = true

        override fun onLongPress(e: MotionEvent) {
            if (canTriggerLongPress) {
                val msg = messageHandler.obtainMessage()
                msg.target = messageHandler
                webView.requestFocusNodeHref(msg)
            }
        }

        /**
         * Is called when the user is swiping after the doubl tap, which in our case means that they
         * are zooming.
         */
        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            canTriggerLongPress = false
            return false
        }

        /**
         * Is called when something is starting being pressed, always before onLongPress.
         */
        override fun onShowPress(e: MotionEvent) {
            canTriggerLongPress = true
        }
    }
}
