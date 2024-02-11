package org.example

import org.example.model.Buffer
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

object Statistic {
    private val start: Long = System.nanoTime()
    private var end: Long = 0

    private fun calculateProbabilityOfRejection(): Double {
        return rejectionPerSource.values.sum() / requestProducedPerSource.values.sum().toDouble()
    }

    private const val t = 1.643
    private const val b = 0.1
    private const val a = 0.9

    private fun calculateN(pNew: Double): Long = ((t * t) * (1 - pNew) / (pNew * b * b)).toLong()

    fun needsContinue(): Boolean = producedRequestsForAll.get() < numOfRequests

    var numOfRequests: Long = 10
    private var isFirstRun: Boolean = true
    private var probabilityOfRejection: Double = 0.0
    private var probabilityOfRejectionOld: Double = 0.0
    var producedRequestsForAll: AtomicLong = AtomicLong()
    private var numOfSources: Int = 1
    private var numOfDevices: Int = 10
    private var bufferCapacity: Int = 3
    private var mode = AppMode.AUTO
    private var i = 0
    private lateinit var buffer: Buffer

    private val requestProducedPerSource = mutableMapOf<Int, Long>()
    private val timeOfBeingPerSource = mutableMapOf<Int, Long>()
    private val timeOfBufferPerSource = mutableMapOf<Int, Long>()
    private val timeOfProcessingPerSource = mutableMapOf<Int, Long>()
    private var requestProcessedPerSource = mutableMapOf<Int, Long>()
    private var efficientOfUsing = mutableMapOf<Int, Long>()
    private var rejectionPerSource = mutableMapOf<Int, Long>()

    fun makeStep(event: Event) {
        when (event) {
            is Event.RequestAppearInBuffer -> {}
            is Event.RequestProcessedOnDevice -> {}
            is Event.RequestProduced -> {
                requestProducedPerSource[event.getSourceId()] =
                    requestProducedPerSource.getOrDefault(event.getSourceId(), 0) + 1
            }

            is Event.RequestStartProcessingOnDevice -> {}
            is Event.RequestTakenByDispatcher -> {}
            is Event.RequestWasRemoved -> {
                timeOfBufferPerSource[event.getSourceId()] = timeOfBufferPerSource.getOrDefault(
                    event.getSourceId(), 0
                ) + (event.request.timeOfRemoved - event.request.timeOfBuffered)
            }

            is Event.RequestReplacedByNew -> {
                rejectionPerSource[event.getSourceId()] = rejectionPerSource.getOrDefault(event.getSourceId(), 0) + 1
            }

            is Event.RequestPlacedInBuffer -> {}
            is Event.DeviceFree -> {}
            is Event.RequestEnded -> {
                timeOfProcessingPerSource[event.getSourceId()] = timeOfProcessingPerSource.getOrDefault(event.getSourceId(), 0) + event.request.timeOfProcessing
            }
        }
        i++
        if (numOfRequests <= requestProducedPerSource.values.sum()) {
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
            println(buffer.getState())
            println("Press 'Enter' to resume")
            readlnOrNull()
        }
    }

    fun getTime(): Long = System.nanoTime() - start

    fun setModelConfiguration(buffer: Buffer,
        numOfSources: Int, numOfDevices: Int, bufferCapacity: Int, mode: AppMode
    ) {
        this.numOfSources = numOfSources
        this.numOfDevices = numOfDevices
        this.bufferCapacity = bufferCapacity
        this.mode = mode
        this.buffer = buffer
        for (i in 1..numOfSources) {
            requestProducedPerSource[i] = 0
            requestProcessedPerSource[i] = 0
            rejectionPerSource[i] = 0
        }
    }

    fun printAutoStatistic() {
        end = System.currentTimeMillis()
        println(state())
        println(producedRequestsForAll)
    }

    //кол-во требований для каждого источника
    //вероятность отказа источника
    //среднее время пребывания заявок каждого источника
    //среднее время ожидания заявок
    //среднее время обслуживания
    //дисперсии
    //коэффициент использования прибора
    fun state(): String {
        val str = StringBuilder()
        str.appendLine("Event calendar:")
        str.append("n\t produced\t Prej\t Tbeing \t\t Tbuff \t\t Tproc\n")
        for (source in 1..numOfSources) {
            val produced = requestProducedPerSource.getOrDefault(source, 0)
            var reject = 0.0
            var Tbuff = 0.0
            var Tproc = 0.0
            if (produced != 0L) {
                reject = rejectionPerSource.getOrDefault(
                    source, 0
                ) / produced.toDouble()

                Tbuff = timeOfBufferPerSource.getOrDefault(source, 0) / produced.toDouble()
                Tproc = timeOfProcessingPerSource.getOrDefault(source, 0) / produced.toDouble()
            }
            str.append(
                "${source} \t   ${produced}  \t${"%.4f".format(reject)} \t${"%.2f".format(Tbuff + Tproc)} \t${"%.2f".format(Tbuff)} \t${
                    "%.2f".format(
                        Tproc
                    )
                }\n"
            )
        }
        return str.toString()
    }
}