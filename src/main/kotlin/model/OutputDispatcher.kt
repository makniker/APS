package org.example.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.Statistic.needsContinue
import org.example.model.device.DeviceList

class OutputDispatcher(private val buffer: Buffer, private val deviceList: DeviceList) {
    suspend fun startProcessing() {
        try {
            EventBus.events.filter { event ->
                event is Event.DeviceFree
            }.collect {
                while (buffer.isEmpty()) {
                    delay(Probability.wait())
                    if (!needsContinue()) {
                        break
                    }
                }
                val request = buffer.getNewestRequest()
                val device = deviceList.getNewestDevice()
                device.setBusy(request)
            }
        } catch (e: Exception) {
            println("Simulation end! Results: ")
        }
    }
}