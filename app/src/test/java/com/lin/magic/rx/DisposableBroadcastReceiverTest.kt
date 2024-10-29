package com.lin.magic.rx

import com.lin.magic.SDK_VERSION
import com.lin.magic.TestApplication
import android.content.BroadcastReceiver
import android.content.Context
import com.lin.magic.rx.BroadcastReceiverDisposable
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [BroadcastReceiverDisposable].
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [SDK_VERSION])
class DisposableBroadcastReceiverTest {

    private val context = mock<Context>()
    private val broadcastReceiver = mock<BroadcastReceiver>()

    @Test
    fun `isDisposed defaults to false`() {
        val disposableBroadcastReceiver = BroadcastReceiverDisposable(context, broadcastReceiver)

        assertThat(disposableBroadcastReceiver.isDisposed).isFalse()
    }

    @Test
    fun `isDisposed returns true after dispose`() {
        val disposableBroadcastReceiver = BroadcastReceiverDisposable(context, broadcastReceiver)

        disposableBroadcastReceiver.dispose()

        assertThat(disposableBroadcastReceiver.isDisposed).isTrue()
    }

    @Test
    fun `dispose unregisters receiver once`() {
        val disposableBroadcastReceiver = BroadcastReceiverDisposable(context, broadcastReceiver)

        disposableBroadcastReceiver.dispose()
        disposableBroadcastReceiver.dispose()
        disposableBroadcastReceiver.dispose()

        verify(context).unregisterReceiver(broadcastReceiver)
        verifyNoMoreInteractions(context)
    }

}
