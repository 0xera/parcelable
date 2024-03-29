package com.zero.xera.parcelable.stream

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Closeable

sealed interface ParcelableStream<T>

sealed interface ParcelableOutputStream<T> : ParcelableStream<T>, Parcelable, Closeable {
    val isFinished: Boolean
}

sealed interface ParcelableInputStream<T> : ParcelableStream<T>, Parcelable, Closeable

@Parcelize
internal class ParcelableOutputStreamImpl<T>(
    internal var parceledDescriptor: ParceledFileDescriptor?,
    override var isFinished: Boolean = false,
) : ParcelableOutputStream<T> {

    internal fun finish() {
        isFinished = true
    }

    override fun close() {
        parceledDescriptor?.descriptor?.close()
        parceledDescriptor = null
    }
}

@Parcelize
internal class ParcelableInputStreamImpl<T>(
    internal var parceledDescriptor: ParceledFileDescriptor?,
) : ParcelableInputStream<T> {

    override fun close() {
        parceledDescriptor?.descriptor?.close()
        parceledDescriptor = null
    }
}