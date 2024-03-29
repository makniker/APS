package org.example

import org.example.model.Buffer
import org.example.model.device.DeviceList
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

object Statistic {
    private val start: Long = System.nanoTime()
    private var end: Long = 0
    private const val t = 1.643
    private const val b = 0.1
    private var numOfRequests: Long = 100
    private var isFirstRun: Boolean = true
    private var probabilityOfRejection: Double = 0.0
    private var probabilityOfRejectionOld: Double = 0.0
    private var producedRequestsForAll: AtomicLong = AtomicLong()
    private var numOfSources: Int = 1
    private var numOfDevices: Int = 10
    private var bufferCapacity: Int = 3
    private var mode = AppMode.AUTO
    private var i = 0
    private lateinit var buffer: Buffer
    private lateinit var deviceList: DeviceList
    private val requestProducedPerSource = mutableMapOf<Int, Long>()
    private val timeOfBufferPerSource = mutableMapOf<Int, MutableList<Long>>()
    private val timeOfProcessingPerSource = mutableMapOf<Int, MutableList<Long>>()
    private val timeOfProcessingPerDevice = mutableMapOf<Int, Long>()
    private var requestProcessedPerSource = mutableMapOf<Int, Long>()
    private var rejectionPerSource = mutableMapOf<Int, Long>()

    private fun calculateProbabilityOfRejection(): Double {
        return if (rejectionPerSource.values.sum() == 0L) 0.0 else rejectionPerSource.values.sum() / requestProducedPerSource.values.sum()
            .toDouble()
    }

    private fun calculateN(pNew: Double): Long = ((t * t) * (1 - pNew) / (pNew * b * b)).toLong()

    fun needsContinue(): Boolean = producedRequestsForAll.get() < numOfRequests
    fun increment() = producedRequestsForAll.getAndIncrement()

    fun makeStep(event: Event) {
        when (event) {
            is Event.RequestProduced -> {
                requestProducedPerSource[event.getSourceId()] =
                    requestProducedPerSource.getOrDefault(event.getSourceId(), 0) + 1
            }

            is Event.RequestStartProcessingOnDevice -> {}
            is Event.RequestWasRemoved -> {
                timeOfBufferPerSource[event.getSourceId()]!!.add(event.request.timeOfRemoved - event.request.timeOfBuffered)
            }

            is Event.RequestReplacedByNew -> {
                rejectionPerSource[event.getSourceId()] = rejectionPerSource.getOrDefault(event.getSourceId(), 0) + 1
            }

            is Event.RequestPlacedInBuffer -> {}
            is Event.DeviceFree -> {
                timeOfProcessingPerDevice[event.deviceId] =
                    timeOfProcessingPerDevice.getOrDefault(event.deviceId, 0) + event.workingTime
            }

            is Event.RequestEnded -> {
                timeOfProcessingPerSource.get(event.getSourceId())!!.add(event.request.timeOfProcessing)
            }
        }
        i++
        if (numOfRequests <= requestProducedPerSource.values.sum() && numOfRequests < 1000) {
            if (isFirstRun) {
                probabilityOfRejection = calculateProbabilityOfRejection()
                if (probabilityOfRejection != 0.0) {
                    numOfRequests = calculateN(probabilityOfRejection)
                }
                isFirstRun = false
            } else {
                val probability = calculateProbabilityOfRejection()
                if (probabilityOfRejection != 0.0) {
                    val newN = calculateN(probability)
                    if (abs(probability - probabilityOfRejection) >= probabilityOfRejection * 0.1) {
                        numOfRequests = newN
                        probabilityOfRejectionOld = probability
                    }
                }
            }
        }
        println("action $i - $event")
        if (mode == AppMode.STEP) {
            println(getState())
            println(deviceList.getState())
            println(buffer.getState())
            println("produced - ${producedRequestsForAll.get()}\nrejected - ${rejectionPerSource.values.sum()}")
            println("Press 'Enter' to resume")
            readlnOrNull()
        }
    }

    fun getTime(): Long = System.nanoTime() - start

    fun setModelConfiguration(
        deviceList: DeviceList, buffer: Buffer, numOfSources: Int, numOfDevices: Int, bufferCapacity: Int, mode: AppMode
    ) {
        this.numOfSources = numOfSources
        this.numOfDevices = numOfDevices
        this.bufferCapacity = bufferCapacity
        this.mode = mode
        this.buffer = buffer
        this.deviceList = deviceList
        for (i in 1..numOfSources) {
            requestProducedPerSource[i] = 0
            requestProcessedPerSource[i] = 0
            rejectionPerSource[i] = 0
            timeOfProcessingPerSource[i] = mutableListOf()
            timeOfBufferPerSource[i] = mutableListOf()
        }
    }

    fun printAutoStatistic() {
        println(getState())
        println("produced - ${producedRequestsForAll.get()}\nrejected - ${rejectionPerSource.values.sum()}")
    }

    private fun getState(): String {
        val str = StringBuilder()
        str.append("n\t produced\t Prej\t Tbeing \t\t Tbuff \t\t\t Tproc \t\t\t Dbuff \t\t\t\t\t\t Dproc\n")
        for (source in 1..numOfSources) {
            val produced = requestProducedPerSource.getOrDefault(source, 0)
            var reject = 0.0
            var Tbuff = 0.0
            var Tproc = 0.0
            var Dbuff = 0.0
            var Dproc = 0.0
            if (produced != 0L) {
                reject = rejectionPerSource.getOrDefault(
                    source, 0
                ) / produced.toDouble()
                if (timeOfBufferPerSource[source]!!.size > 1) {
                    Tbuff = timeOfBufferPerSource[source]!!.sum() / timeOfBufferPerSource[source]!!.size.toDouble()
                    for (v in timeOfBufferPerSource[source]!!) {
                        Dbuff += (v - Tbuff) * (v - Tbuff)
                    }
                    Dbuff /= (timeOfBufferPerSource[source]!!.size - 1)
                }
                if (timeOfProcessingPerSource[source]!!.size > 1) {
                    Tproc =
                        timeOfProcessingPerSource[source]!!.sum() / timeOfProcessingPerSource[source]!!.size.toDouble()
                    for (v in timeOfProcessingPerSource[source]!!) {
                        Dproc += (v - Tproc) * (v - Tproc)
                    }
                    Dproc /= (timeOfProcessingPerSource[source]!!.size - 1)
                }
            }
            str.append(
                "$source \t \t  $produced  \t${
                    "%.4f".format(reject)
                } \t${
                    "%.2f".format(Tbuff + Tproc)
                } \t${
                    "%.2f".format(Tbuff)
                } \t${
                    "%.2f".format(Tproc)
                } \t ${
                    "%.2f".format(Dbuff)
                } \t ${
                    "%.2f".format(Dproc)
                }\n"
            )
        }
        str.append("\nn \t K\n")
        end = System.nanoTime() - start
        for (device in 1..numOfDevices) {
            str.append(
                "$device \t ${
                    "%.4f".format(
                        timeOfProcessingPerDevice.getOrDefault(
                            device, 0
                        ) / end.toDouble()
                    )
                }\n"
            )
        }
        str.appendLine("Probability of rejection - ${calculateProbabilityOfRejection()}")
        return str.toString()
    }
}