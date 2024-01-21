package com.zero.xera.parcelable.stream

import android.annotation.SuppressLint
import android.content.Context
import android.os.MemoryFile
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import java.lang.reflect.Method
import kotlin.concurrent.getOrSet

private val byteArrayThreadLocal = ThreadLocal<ByteArray>()

internal fun createParcelFileDescriptorFromData(
    data: ByteArray,
    parcelType: ParcelType,
    context: Context? = null
): ParcelFileDescriptor {
    val memoryFile = MemoryFile("ParcelableStreamMemoryFile${data.contentHashCode()}", data.size + 1)

    val typeArray = byteArrayThreadLocal.getOrSet { ByteArray(1) }
    typeArray[0] = parcelType.type.toByte()

    memoryFile.writeBytes(typeArray, 0, 0, 1)
    memoryFile.writeBytes(data, 0, 1, data.size)
    memoryFile.allowPurging(true)

    return createParcelFileDescriptorLegacy(memoryFile)
//    TODO Seems it is not needed
//    return if (Build.VERSION.SDK_INT < 26) {
//        createParcelFileDescriptorLegacy(memoryFile)
//    } else {
//        MemoryFileDescriptorProxy.create(context, memoryFile)
//    }
}

/** see https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/os/ParcelFileDescriptor.java */
@SuppressLint("DiscouragedPrivateApi")
private fun createParcelFileDescriptorLegacy(memoryFile: MemoryFile): ParcelFileDescriptor {
    val method: Method = MemoryFile::class.java.getDeclaredMethod("getFileDescriptor")
    val descriptor = method.invoke(memoryFile) as FileDescriptor
    return ParcelFileDescriptor.dup(descriptor)
}

/** see https://github.com/signalapp/Signal-Android/blob/main/app/src/main/java/org/thoughtcrime/securesms/util/MemoryFileUtil.java */
//@RequiresApi(api = 26)
//private object MemoryFileDescriptorProxy {
//    private const val TAG: String = "MemoryFileDescriptorProxy"
//
//    fun create(context: Context, file: MemoryFile): ParcelFileDescriptor {
//        val storageManager =  context.getSystemService(StorageManager::class.java)
//
//        val thread = HandlerThread("MemoryFile")
//        thread.start()
//
//        Log.i(TAG, "Thread started")
//        val handler = Handler(thread.getLooper())
//        val proxyCallback = ProxyCallback(file) {
//            Log.i(TAG, "Thread quitSafely ${thread.quitSafely()}")
//        }
//
//        val parcelFileDescriptor = storageManager.openProxyFileDescriptor(
//            ParcelFileDescriptor.MODE_READ_ONLY,
//            proxyCallback,
//            handler
//        )
//
//        Log.i(TAG, "Created")
//        return parcelFileDescriptor
//    }
//
//    private class ProxyCallback(
//        private val memoryFile: MemoryFile,
//        private val onClose: Runnable
//    ) : ProxyFileDescriptorCallback() {
//
//        override fun onGetSize(): Long = memoryFile.length().toLong()
//
//        override fun onRead(offset: Long, size: Int, data: ByteArray): Int {
//            return try {
//                val inputStream = memoryFile.inputStream
//                if (inputStream.skip(offset) != offset) {
//                    if (offset > memoryFile.length()) {
//                        throw ErrnoException("onRead", OsConstants.EIO)
//                    }
//                    throw AssertionError()
//                }
//                inputStream.read(data, 0, size)
//            } catch (e: IOException) {
//                throw ErrnoException("onRead", OsConstants.EBADF)
//            }
//        }
//
//        override fun onRelease() {
//            Log.i(TAG, "onRelease")
//            memoryFile.close()
//            onClose.run()
//        }
//    }
//}