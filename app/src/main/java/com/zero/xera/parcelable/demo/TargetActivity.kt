
package com.zero.xera.parcelable.demo

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zero.xera.parcelable.slice.ParcelableSlice
import com.zero.xera.parcelable.slice.join
import com.zero.xera.parcelable.slice.parcelableSlice
import com.zero.xera.parcelable.stream.ParcelableInputStream
import com.zero.xera.parcelable.stream.ParcelableStreamPipe
import com.zero.xera.parcelable.stream.parcelableInputStream
import com.zero.xera.parcelable.stream.read
import com.zero.xera.parcelable.stream.write
import kotlin.system.measureTimeMillis

class TargetActivity : AppCompatActivity(R.layout.activity_target) {

    private lateinit var dataTextView: TextView
    private lateinit var dataTypeTextView: TextView
    private lateinit var timeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.text_view_package).text = packageName

        dataTextView = findViewById(R.id.text_view_data)
        dataTypeTextView = findViewById(R.id.text_view_data_type)
        timeTextView = findViewById(R.id.text_view_time)

        if (handleLargeData<ParcelableLargeData>(intent, DataType.PARCELABLE)) return
        if (handleLargeData<SerializableLargeData>(intent, DataType.SERIALIZABLE)) return
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (handleLargeData<ParcelableLargeData>(intent, DataType.PARCELABLE)) return
        if (handleLargeData<SerializableLargeData>(intent, DataType.SERIALIZABLE)) return
    }

    private inline fun <reified T> handleLargeData(intent: Intent, type: DataType): Boolean {
        val data: T? = intent.getParcelableExtra(type.keyFor(LARGE_DATA))
        val slice: ParcelableSlice<T>? = intent.getParcelableExtra(type.keyFor(LARGE_DATA_SLICE))
        val stream: ParcelableInputStream<T>? = intent.getParcelableExtra(type.keyFor(LARGE_DATA_STREAM))
        val streamFromPipe: ParcelableInputStream<T>? = intent.getParcelableExtra(type.keyFor(LARGE_DATA_PIPE))

        if (data != null) {
            timeTextView.text = "..."
            dataTypeTextView.text = "$type data"
            dataTextView.text = "Reading data..."
            timeTextView.text = measureTimeMillis {
                dataTextView.text = data.hashCode().toString()
            }.toString()
            return true
        }

        if (slice != null) {
            timeTextView.text = "..."
            dataTypeTextView.text = "$type slice"
            dataTextView.text = "Reading data..."
            Thread {
                val start = System.currentTimeMillis()
                val result = slice.join()
                val end = System.currentTimeMillis()
                dataTextView.post {
                    dataTextView.text = result.hashCode().toString()
                    timeTextView.text = (end - start).toString()
                }
            }.start()
            return true
        }

        if (stream != null) {
            timeTextView.text = "..."
            dataTypeTextView.text = "$type stream"
            dataTextView.text = "Reading data..."
            Thread {
                val start = System.currentTimeMillis()
                val result = stream.read(T::class.java)
                val end = System.currentTimeMillis()
                dataTextView.post {
                    dataTextView.text = result.hashCode().toString()
                    timeTextView.text = (end - start).toString()
                }
            }.start()
            return true
        }

        if (streamFromPipe != null) {
            timeTextView.text = "..."
            dataTypeTextView.text = "$type stream from pipe"
            dataTextView.text = "Reading data..."
            Thread {
                val start = System.currentTimeMillis()
                val result = streamFromPipe.read(T::class.java)
                val end = System.currentTimeMillis()
                dataTextView.post {
                    dataTextView.text = result.hashCode().toString()
                    timeTextView.text = (end - start).toString()
                }
            }.start()
            return true
        }

        return false
    }

    private enum class DataType {
        PARCELABLE,
        SERIALIZABLE;

        fun keyFor(type: String): String = "${name}_DATA_{$type}_KEY"
    }

    companion object {

        private const val LARGE_DATA = "LARGE_DATA"
        private const val LARGE_DATA_SLICE = "LARGE_DATA_SLICE"
        private const val LARGE_DATA_STREAM = "LARGE_DATA_STREAM"
        private const val LARGE_DATA_PIPE = "LARGE_DATA_PIPE"

        fun create(largeData: ParcelableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.PARCELABLE.keyFor(LARGE_DATA), largeData)
            }

        fun create(largeData: SerializableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.SERIALIZABLE.keyFor(LARGE_DATA), largeData)
            }

        fun createSlice(largeData: ParcelableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.PARCELABLE.keyFor(LARGE_DATA_SLICE), largeData.parcelableSlice())
            }

        fun createSlice(largeData: SerializableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.PARCELABLE.keyFor(LARGE_DATA_SLICE), largeData.parcelableSlice())
            }

        fun createStream(largeData: ParcelableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.PARCELABLE.keyFor(LARGE_DATA_STREAM), largeData.parcelableInputStream())
            }

        fun createStream(largeData: SerializableLargeData): Intent =
            Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.SERIALIZABLE.keyFor(LARGE_DATA_STREAM), largeData.parcelableInputStream())
            }

        fun createPipe(largeData: ParcelableLargeData, onFinish: () -> Unit): Intent {
            val (reader, writer) = ParcelableStreamPipe<ParcelableLargeData>()

            Thread {
                writer.write(largeData)
                onFinish()
            }.start()

            return Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.PARCELABLE.keyFor(LARGE_DATA_PIPE), reader)
            }
        }

        fun createPipe(largeData: SerializableLargeData, onFinish: () -> Unit): Intent {
            val (reader, writer) = ParcelableStreamPipe<SerializableLargeData>()

            Thread {
                writer.write(largeData)
                onFinish()
            }.start()

            return Intent().apply {
                setComponent(ComponentName(BuildConfig.MIRROR_PACKAGE, TargetActivity::class.java.name))
                putExtra(DataType.SERIALIZABLE.keyFor(LARGE_DATA_PIPE), reader)
            }
        }
    }
}