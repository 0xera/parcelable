package com.zero.xera.parcelable.stream

import android.os.Parcelable
import com.zero.xera.parcelable.stream.internal.ParceledFileDescriptor
import kotlinx.parcelize.Parcelize

sealed interface ParcelableStream<T>

@UnstableApi
sealed interface ParcelableOutputStream<T> : ParcelableStream<T>, Parcelable {
    val isFinished: Boolean
}

sealed interface ParcelableInputStream<T> : ParcelableStream<T>, Parcelable

@Parcelize
@UnstableApi
internal class ParcelableOutputStreamImpl<T>(
    internal val parceledDescriptor: ParceledFileDescriptor?,
    override var isFinished: Boolean = false,
) : ParcelableOutputStream<T> {

    internal fun finish() {
        isFinished = true
    }
}

@Parcelize
internal class ParcelableInputStreamImpl<T>(
    internal val parceledDescriptor: ParceledFileDescriptor,
) : ParcelableInputStream<T>