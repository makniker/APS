package org.example.model.device

import kotlinx.coroutines.delay
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.model.request.Request

class Device(private val id: Int) {
    private var isBusy = false

    override fun toString(): String = "id: $id"
    fun isBusy(): Boolean = isBusy

    suspend fun setBusy(request: Request) {
        isBusy = true
        EventBus.produceEvent(Event.RequestStartProcessingOnDevice(request, id))
        val delay = Probability.expDistribution()
        request.timeOfProcessing = delay * 1000000
        delay(delay)
        isBusy = false
        EventBus.produceEvent(Event.RequestEnded(request))
        EventBus.produceEvent(Event.DeviceFree(id, request.timeOfProcessing))
    }
}