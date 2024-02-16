package org.example.model.request

import kotlinx.coroutines.delay
import org.example.Event
import org.example.EventBus
import org.example.Probability
import org.example.Statistic

class RequestSource(private val id: Int) {
    suspend fun startGeneratingRequests() {
        var i = 1
        while (Statistic.needsContinue()) {
            Statistic.increment()
            val request = Request(id to i++, Statistic.getTime())
            EventBus.produceEvent(Event.RequestProduced(request))
            delay(Probability.uniformDistribution())
        }
    }
}