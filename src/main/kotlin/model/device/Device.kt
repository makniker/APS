package org.example.model.device

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.Statistic
import org.example.model.request.Request

class Device(val id: Int) {

    private var workingTime: Long = 0
    var isBusy = false

    suspend fun setBusy(request: Request)  {
        isBusy = true
        EventBus.produceEvent(Event.RequestStartProcessingOnDevice(request, id))
        val delay = Probability.expDistribution()
        workingTime += delay
        delay(delay)
        request.timeOfProcessing = delay * 1000000
        request.timeOfFinish = Statistic.getTime()
        isBusy = false
        EventBus.produceEvent(Event.RequestEnded(request))
        EventBus.produceEvent(Event.DeviceFree(id))
    }
}