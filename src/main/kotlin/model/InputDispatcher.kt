package org.example.model

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import org.example.Event
import org.example.EventBus

class InputDispatcher(private val buffer: Buffer) {
    suspend fun startProcessingRequest() {
        EventBus.bus.filterIsInstance<Event.RequestProduced>().collect { value ->
            buffer.putRequest(value.request)
        }
    }
}