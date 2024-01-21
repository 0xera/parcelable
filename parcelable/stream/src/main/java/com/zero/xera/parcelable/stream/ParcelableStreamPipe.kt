package com.zero.xera.parcelable.stream

class ParcelableStreamPipe<T> internal constructor(
    val reader: ParcelableInputStream<T>,
    val writer: ParcelableOutputStream<T>
) {
    operator fun component1(): ParcelableInputStream<T> = reader
    operator fun component2(): ParcelableOutputStream<T> = writer
}