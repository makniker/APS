package org.example.model.device

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.Statistic
import org.example.model.request.Request

class Device(val id: Int) {
    var isBusy = false
    var timeOfFree = 0L

    fun getTime(): Long = timeOfFree

    suspend fun setBusy(request: Request) {

        isBusy = true
        EventBus.produceEvent(Event.RequestStartProcessingOnDevice(request, id))
        val delay = Probability.expDistribution()
        request.timeOfProcessing = delay * 1000000
        delay(delay)
        request.timeOfFinish = Statistic.getTime()
        timeOfFree = request.timeOfFinish
        isBusy = false
        EventBus.produceEvent(Event.RequestEnded(request))
        EventBus.produceEvent(Event.DeviceFree(id, delay * 1000000))

    }
}