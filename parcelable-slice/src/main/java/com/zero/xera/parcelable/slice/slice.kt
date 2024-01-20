package com.zero.xera.parcelable.slice

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.zero.xera.parcelable.slice.internal.ParceledListSlice
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable


private const val CHUNK_SIZE = 20

// region Main Functions

@WorkerThread
fun <T : Parcelable> T.parcelableSlice(chunkSizeKB: Int = CHUNK_SIZE): ParcelableSlice<T> {
    val chunkLength = chunkSizeKB.coerceAtLeast(CHUNK_SIZE) * 1000
    return parcelableToSlice(chunkLength)
}

@WorkerThread
fun <T : Serializable> T.parcelableSlice(chunkSizeKB: Int = CHUNK_SIZE): ParcelableSlice<T> {
    val chunkLength = chunkSizeKB.coerceAtLeast(CHUNK_SIZE) * 1000
    return serializableToSlice(chunkLength)
}

@WorkerThread
inline fun <reified T> ParcelableSlice<T>.join(): T? = join(T::class.java)

// endregion

private fun <T : Parcelable> T.parcelableToSlice(chunkLength: Int): ParcelableSlice<T> {
    val parcel = Parcel.obtain()
    try {
        parcel.writeParcelable(this, 0)
        val chunks = parcel.marshall().asSequence().chunked(chunkLength).map(::ParcelableChunk)
        return saveChunks(chunks.toList())
    } finally {
        parcel.recycle()
    }
}

private fun <T : Serializable> T.serializableToSlice(chunkLength: Int): ParcelableSlice<T> =
    ByteArrayOutputStream().use { boas ->
        ObjectOutputStream(boas).use { oos -> oos.writeObject(this) }
        val chunks = boas.toByteArray().asSequence().chunked(chunkLength).map(::SerializableChunk)
        saveChunks(chunks.toList())
    }


fun <T> ParcelableSlice<T>.join(clazz: Class<T>): T? {
    if (this !is ParcelableSliceImpl<T, *>) return null
    val chunks = chunks.list as? List<Chunk>
    if (chunks.isNullOrEmpty()) return null

    val bytes = chunks.flatMap { it.bytes }.toByteArray()

    return when (chunks.first()) {
        is ParcelableChunk -> joinParcelable(bytes, clazz)
        is SerializableChunk -> joinSerializable(bytes)
    }
}

private inline fun <reified T : Parcelable> joinParcelable(
    bytes: ByteArray,
    clazz: Class<*>
): T? {
    val parcel = Parcel.obtain()
    return try {
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        parcel.readParcelable(clazz.classLoader)
    } finally {
        parcel.recycle()
    }
}

private fun <T : Serializable> joinSerializable(bytes: ByteArray): T? =
    ByteArrayInputStream(bytes).use {
        ObjectInputStream(it).use { ois -> ois.readObject() }
    } as? T

private fun <T> saveChunks(chunks: List<Chunk>): ParcelableSlice<T> =
    ParcelableSliceImpl<T, Chunk>(
        ParceledListSlice(chunks)
    )