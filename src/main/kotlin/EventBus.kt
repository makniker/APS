package org.example

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _events = MutableSharedFlow<Event>(replay = 500, extraBufferCapacity = 500)
    val bus = _events.asSharedFlow()

    suspend fun produceEvent(event: Event) {
        Statistic.makeStep(event)
        _events.emit(event)
    }
}