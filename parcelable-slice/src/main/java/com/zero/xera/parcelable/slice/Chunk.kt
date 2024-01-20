package com.zero.xera.parcelable.slice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface Chunk : Parcelable {
    val bytes: List<Byte>
}

@Parcelize
internal data class ParcelableChunk(override val bytes: List<Byte>) : Chunk

@Parcelize
internal data class SerializableChunk(override val bytes: List<Byte>) : Chunk