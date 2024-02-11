package org.example.model

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import org.example.Event
import org.example.EventBus

class InputDispatcher(private val buffer: Buffer) {
    suspend fun startProcessingRequest() {
        EventBus.events.filterIsInstance<Event.RequestProduced>().collect { value ->
            EventBus.produceEvent(Event.RequestTakenByDispatcher(value.request))
            buffer.putRequest(value.request)
        }
    }
}