package org.example

import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val events = MutableSharedFlow<Event>(replay = 500, extraBufferCapacity = 500)

    suspend fun produceEvent(event: Event) {
        Statistic.makeStep(event)
        events.emit(event)
    }
}