package com.zero.xera.parcelable.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.zero.xera.parcelable.stream.ParcelableInputStream
import com.zero.xera.parcelable.stream.ParcelableOutputStream
import com.zero.xera.parcelable.stream.ParcelableStreamPipe
import com.zero.xera.parcelable.stream.read

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.text_view_package).text = packageName

        val parcelTypeSwitch = findViewById<SwitchCompat>(R.id.switch_pacel_type)
        var isParcelable = parcelTypeSwitch.isChecked

        parcelTypeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            parcelTypeSwitch.isChecked = isChecked
            isParcelable = isChecked
        }

        findViewById<Button>(R.id.button_go_list).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.create(ParcelableLargeData.instance)
                    else -> TargetActivity.create(SerializableLargeData.instance)
                }
            )
        }

        findViewById<Button>(R.id.button_go_slice).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.createSlice(ParcelableLargeData.instance)
                    else -> TargetActivity.createSlice(SerializableLargeData.instance)
                }
            )
        }

        findViewById<Button>(R.id.button_go_stream).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.createStream(ParcelableLargeData.instance)
                    else -> TargetActivity.createStream(SerializableLargeData.instance)
                }
            )
        }

        val pipeButton = findViewById<Button>(R.id.button_go_pipe)
        pipeButton.setOnClickListener {
            pipeButton.text = "Sending via pipe..."
            val intent = when {
                isParcelable -> TargetActivity.createPipe(ParcelableLargeData.instance) {
                    pipeButton.post { pipeButton.text = "Pipe" }
                }

                else -> TargetActivity.createPipe(SerializableLargeData.instance) {
                    pipeButton.post { pipeButton.text = "Pipe" }
                }
            }
            goToTarget(intent)
        }

        val pipeForResultButton = findViewById<Button>(R.id.button_go_pipe_for_result)
        val resultTextView = findViewById<TextView>(R.id.text_view_result)
        val resultTimeTextView = findViewById<TextView>(R.id.text_view_result_time)

        pipeForResultButton.setOnClickListener {
            resultTimeTextView.text = "..."
            pipeForResultButton.text = "Receiving via pipe..."
            resultTextView.text = ""
            when {
                isParcelable -> {
                    val intent = TargetActivity.createParcelablePipeForResult(ResultReceiver { bundle ->
                        val reader = bundle?.run {
                            classLoader = ParcelableInputStream::class.java.classLoader
                            getParcelable<ParcelableInputStream<ParcelableLargeData>>("pipe")
                        }

                        if (reader != null) {
                            Thread {
                                val start = System.currentTimeMillis()
                                val data = reader.read()
                                val end = System.currentTimeMillis()
                                resultTextView.post {
                                    pipeForResultButton.text = "Pipe for result"
                                    resultTextView.text = data.hashCode().toString()
                                    resultTimeTextView.text = (end - start).toString()
                                }
                            }.start()
                        }
                    })
                    goToTarget(intent)
                }
                else -> {
                    val intent = TargetActivity.createParcelablePipeForResult(ResultReceiver { bundle ->
                        val reader = bundle?.run {
                            classLoader = ParcelableInputStream::class.java.classLoader
                            getParcelable<ParcelableInputStream<ParcelableLargeData>>("pipe")
                        }

                        if (reader != null) {
                            Thread {
                                val start = System.currentTimeMillis()
                                val data = reader.read()
                                val end = System.currentTimeMillis()
                                resultTextView.post {
                                    pipeForResultButton.text = "Pipe for result"
                                    resultTextView.text = data.hashCode().toString()
                                    resultTimeTextView.text = (end - start).toString()
                                }
                            }.start()
                        }
                    })
                    goToTarget(intent)
                }
            }
        }
    }

    private fun goToTarget(intent: Intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        startActivity(intent)
    }

    private fun ResultReceiver(handler: Handler = Handler(Looper.getMainLooper()), action: (resultData: Bundle?) -> Unit): ResultReceiver {
        return object : ResultReceiver(handler) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) = action(resultData)
        }
    }
}