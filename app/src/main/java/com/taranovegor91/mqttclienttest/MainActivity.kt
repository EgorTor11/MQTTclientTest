package com.taranovegor91.mqttclienttest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck
import com.taranovegor91.mqttclienttest.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import java.util.*


class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var job: Job
    lateinit var client: Mqtt3AsyncClient
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.hivemq.com")
            .serverPort(8883)
            .useSslWithDefaultConfig()
            .buildAsync()
        job = scope.launch {
            client.connectWith()
                .simpleAuth()
                .username("egorholod")
                .password("H#@nh5KGYW48Q2@".toByteArray())
                .applySimpleAuth()
                .send()
                .whenComplete { connAck: Mqtt3ConnAck?, throwable: Throwable? ->
                    if (throwable != null) {
                        // handle failure
                        Log.d("log", "феил подключения")
                    } else {
                        // setup subscribes or start publishing
                        Log.d("log", "успешное подключение")
                        client.subscribeWith()
                            .topicFilter("zadanie/topic")
                            .callback { publish ->//: Mqtt3Publish? ->
                                val string =
                                    String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8)
                                Log.d("log", string)
                                runOnUiThread {
                                    binding.tvMessage.text = string
                                    binding.llMiss.setBackgroundResource(R.drawable.card_radius_green)
                                    binding.llAnswer.visibility = View.VISIBLE
                                    binding.tvPrinylName.text = ""
                                }
                            }
                            .send()
                            .whenComplete { subAck: Mqtt3SubAck?, throwable: Throwable? ->
                                if (throwable != null) {
                                    // Handle failure to subscribe
                                } else {
                                    // Handle successful subscription, e.g. logging or incrementing a metric
                                }
                            }
                        client.subscribeWith()
                            .topicFilter("prinyli/topic")
                            .callback { publish ->//: Mqtt3Publish? ->
                                val string =
                                    String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8)
                                Log.d("log", string)
                                runOnUiThread {
                                    binding.tvPrinylName.text = string
                                    binding.llMiss.setBackgroundResource(R.drawable.card_rad_gray)
                                    binding.llAnswer.visibility = View.GONE
                                }
                            }
                            .send()
                            .whenComplete { subAck: Mqtt3SubAck?, throwable: Throwable? ->
                                if (throwable != null) {
                                    // Handle failure to subscribe
                                } else {
                                    // Handle successful subscription, e.g. logging or incrementing a metric
                                }
                            }
                    }
                }
        }
        binding.btnPrinyl.setOnClickListener {
            client.publishWith()
                .topic("prinyli/topic")
                .payload(binding.edName.text.toString().toByteArray())
                .send()
                .whenComplete { publish, throwable ->
                    if (throwable != null) {
                        // handle failure to publish
                        Log.d("log", "failure send")
                    } else {
                        // handle successful publish, e.g. logging or incrementing a metric
                        Log.d("log", "successful send")
                    }
                }
        }
        binding.btnSend.setOnClickListener {
            client.publishWith()
                .topic("zadanie/topic")
                .payload(binding.edMissian.text.toString().toByteArray())
                .send()
                .whenComplete { publish, throwable ->
                    if (throwable != null) {
                        // handle failure to publish
                        Log.d("log", "failure send")
                    } else {
                        // handle successful publish, e.g. logging or incrementing a metric
                        Log.d("log", "successful send")
                    }
                }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        client.disconnect()
    }
}