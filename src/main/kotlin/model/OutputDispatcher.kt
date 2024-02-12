package org.example.model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.Statistic.needsContinue
import org.example.model.device.DeviceList

class OutputDispatcher(private val buffer: Buffer, private val deviceList: DeviceList) {
    val mutex = Mutex()
    suspend fun startProcessing() = coroutineScope {
        try {
            EventBus.events.filter { event ->
                event is Event.DeviceFree
            }.collect {
                //mutex.withLock {
                    while (buffer.isEmpty()) {
                        delay(Probability.wait())
                        if (!needsContinue() && deviceList.isAllFree() && buffer.isEmpty()) {
                            break
                        }
                    }
                    val request = buffer.getNewestRequest()
                    val device = deviceList.getNewestDevice()
                    launch { device.setBusy(request) }
                //}
            }
        } catch (e: Exception) {
            println("Simulation end! Results: ")
        }
    }
}