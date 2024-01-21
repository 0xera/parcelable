package com.zero.xera.parcelable.stream

import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.os.ParcelFileDescriptor.AutoCloseOutputStream
import android.os.Parcelable
import androidx.annotation.WorkerThread
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

// region Main Functions

fun <T> ParcelableStreamPipe(): ParcelableStreamPipe<T> = createParcelableStreamPipe()

fun <T : Parcelable> T.parcelableInputStream(): ParcelableInputStream<T> = parcelableToInputStream()
fun <T : Serializable> T.parcelableInputStream(): ParcelableInputStream<T> = serializableToInputStream()

@WorkerThread
inline fun <reified T : Parcelable> ParcelableInputStream<T>.read(): T? = read(T::class.java)

@WorkerThread
inline fun <reified T : Serializable> ParcelableInputStream<T>.read(): T? = read(T::class.java)

@WorkerThread
fun <T : Parcelable> ParcelableOutputStream<T>.write(data: T): Boolean = if (isFinished) false else internalWrite(data)

@WorkerThread
fun <T : Serializable> ParcelableOutputStream<T>.write(data: T): Boolean = if (isFinished) false else internalWrite(data)

// endregion

internal fun <T> createParcelableStreamPipe(): ParcelableStreamPipe<T> {
    val (read, write) = ParcelFileDescriptor.createReliablePipe()
    return ParcelableStreamPipe(
        reader = ParcelableInputStreamImpl(ParceledFileDescriptor(read)),
        writer = ParcelableOutputStreamImpl(ParceledFileDescriptor(write))
    )
}

private fun <T : Parcelable> T.parcelableToInputStream(): ParcelableInputStreamImpl<T> {
    val parcel = Parcel.obtain()
    return try {
        parcel.writeParcelable(this, 0)
        val bytes = parcel.marshall()
        val descriptor = createParcelFileDescriptorFromData(bytes, ParcelType.Parcelable, null)
        ParcelableInputStreamImpl(ParceledFileDescriptor(descriptor))
    } finally {
        parcel.recycle()
    }
}

private fun <T : Serializable> T.serializableToInputStream(): ParcelableInputStreamImpl<T> {
    ByteArrayOutputStream().use { boas ->
        ObjectOutputStream(boas).use { oos -> oos.writeObject(this) }
        val bytes = boas.toByteArray()
        val descriptor = createParcelFileDescriptorFromData(bytes, ParcelType.Serializable, null)
        return ParcelableInputStreamImpl(ParceledFileDescriptor(descriptor))
    }
}

private fun <T> ParcelableOutputStream<T>.internalWrite(data: T): Boolean {
    if (this !is ParcelableOutputStreamImpl<T>) return false

    val descriptor = parceledDescriptor?.descriptor ?: return false
    if (isFinished) return false
    finish()

    when (data) {
        is Parcelable -> data.writeParcelableTo(descriptor)
        is Serializable -> data.writeSerializableTo(descriptor)
        else -> return false
    }

    return true
}

private fun <T : Parcelable> T.writeParcelableTo(descriptor: ParcelFileDescriptor) {
    val parcel = Parcel.obtain()
    try {
        parcel.writeParcelable(this, 0)
        val bytes = parcel.marshall()
        AutoCloseOutputStream(descriptor).use { acos ->
            acos.write(ParcelType.Parcelable.type)
            acos.write(bytes)
        }
    } finally {
        parcel.recycle()
    }
}

private fun <T : Serializable> T.writeSerializableTo(descriptor: ParcelFileDescriptor) =
    ByteArrayOutputStream().use { boas ->
        ObjectOutputStream(boas).use { oos -> oos.writeObject(this) }
        AutoCloseOutputStream(descriptor).use { acos ->
            acos.write(ParcelType.Serializable.type)
            acos.write(boas.toByteArray())
        }
    }

fun <T> ParcelableInputStream<T>.read(clazz: Class<T>): T? {
    if (Parcelable::class.java.isAssignableFrom(clazz).not() && Serializable::class.java.isAssignableFrom(clazz).not()) return null
    if (this !is ParcelableInputStreamImpl<T>) return null
    val descriptor = parceledDescriptor?.descriptor ?: return null

    AutoCloseInputStream(descriptor).use { acis ->
        val parcelType = acis.read()
        val bytes = acis.readBytes()
        return when (parcelType.toParcelType()) {
            ParcelType.Parcelable -> readParcelable(bytes, clazz)
            ParcelType.Serializable -> readSerializable(bytes)
        }
    }
}

private inline fun <reified T : Parcelable> readParcelable(
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

private fun <T : Serializable> readSerializable(bytes: ByteArray): T? =
    ByteArrayInputStream(bytes).use {
        ObjectInputStream(it).use { ois -> ois.readObject() }
    } as? T

