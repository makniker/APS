package org.example

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.model.Buffer
import org.example.model.InputDispatcher
import org.example.model.OutputDispatcher
import org.example.model.device.DeviceList
import org.example.model.request.RequestSourceList

class Application(
    private val numOfSources: Int,
    private val numOfDevices: Int,
    private val bufferCapacity: Int,
    private val mode: AppMode = AppMode.AUTO,
    private val alpha: Long,
    private val beta: Long,
    private val lambda: Double
) {
    private val requestSourceList = RequestSourceList(numOfSources)
    private val buffer = Buffer(bufferCapacity)
    private val inputDispatcher = InputDispatcher(buffer)
    private val deviceList = DeviceList(numOfDevices)
    private val outputDispatcher = OutputDispatcher(buffer, deviceList)
    fun start() = runBlocking {
        Statistic.setModelConfiguration(
            deviceList, buffer, numOfSources, numOfDevices, bufferCapacity, mode
        )
        Probability.setProbability(alpha, beta, lambda)
        deviceList.initList()
        val job2 = launch { inputDispatcher.startProcessingRequest() }
        val job3 = launch { outputDispatcher.startProcessing() }
        val job = launch { requestSourceList.startGeneratingRequests() }
        job.join()
        job3.join()
        job2.cancel()
        Statistic.printAutoStatistic()
    }

}