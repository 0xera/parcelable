package com.zero.xera.parcelable.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.zero.xera.parcelable.stream.ParcelableStreamPipe
import com.zero.xera.parcelable.stream.UnstableApi
import com.zero.xera.parcelable.stream.read

class MainActivity : AppCompatActivity() {


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val parcelTypeSwitch = findViewById<SwitchCompat>(R.id.switch_pacel_type)
        var isParcelable = parcelTypeSwitch.isChecked

        parcelTypeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            parcelTypeSwitch.isChecked = isChecked
            isParcelable = isChecked
        }

        findViewById<Button>(R.id.button_go_list).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.create(this, ParcelableLargeData.instance)
                    else -> TargetActivity.create(this, SerializableLargeData.instance)
                }
            )
        }

        findViewById<Button>(R.id.button_go_slice).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.createSlice(this, ParcelableLargeData.instance)
                    else -> TargetActivity.createSlice(this, SerializableLargeData.instance)
                }
            )
        }

        findViewById<Button>(R.id.button_go_stream).setOnClickListener {
            goToTarget(
                when {
                    isParcelable -> TargetActivity.createStream(this, ParcelableLargeData.instance)
                    else -> TargetActivity.createStream(this, SerializableLargeData.instance)
                }
            )
        }

        val pipeButton = findViewById<Button>(R.id.button_go_pipe)
        pipeButton.setOnClickListener {
            pipeButton.text = "Sending via pipe..."
            val intent = when {
                isParcelable -> TargetActivity.createPipe(this, ParcelableLargeData.instance) {
                    pipeButton.post {
                        pipeButton.text = "Pipe"
                    }
                }

                else -> TargetActivity.createPipe(this, SerializableLargeData.instance) {
                    pipeButton.post {
                        pipeButton.text = "Pipe"
                    }
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
                    val (read, write) = ParcelableStreamPipe<ParcelableLargeData>()
                    goToTarget(TargetActivity.createParcelablePipeForResult(this, write))
                    Thread {
                        Thread.sleep(3000)
                        val start = System.currentTimeMillis()
                        println("start read")
                        val data = read.read()
                        println("end read")
                        val end = System.currentTimeMillis()
                        resultTextView.post {
                            pipeForResultButton.text = "Pipe for result"
                            resultTextView.text = data.hashCode().toString()
                            resultTimeTextView.text = (end - start).toString()
                        }
                    }.start()
                }
                else -> {
                    val (read, write) = ParcelableStreamPipe<SerializableLargeData>()
                    goToTarget(TargetActivity.createSerializablePipeForResult(this, write))
                    Thread {
                        val start = System.currentTimeMillis()
                        println("start read")
                        val data = read.read()
                        println("end read")
                        val end = System.currentTimeMillis()
                        resultTextView.post {
                            pipeForResultButton.text = "Pipe for result"
                            resultTextView.text = data.hashCode().toString()
                            resultTimeTextView.text = (end - start).toString()
                        }
                    }.start()
                }
            }
        }
    }

    private fun goToTarget(intent: Intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        startActivity(intent)
    }
}