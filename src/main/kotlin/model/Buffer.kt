package org.example.model

import org.example.Event
import org.example.EventBus
import org.example.Statistic
import org.example.model.request.Request

class Buffer(private val bufferCapacity: Int) {
    private val buffer = mutableListOf<Request>()

    fun getState(): String {
        val str = StringBuilder()
        str.append("Buff state:\n")
        str.append("pos\ttime\t\t\t(source, id)\n")
        var i = 0
        buffer.forEach { str.append("${i++}\t${it.timeOfBuffered}\t${it.id}\n") }
        for (i in buffer.size until bufferCapacity) {
            str.append("${i}\t 0 \t\t\t\tempty\n")
        }
        return str.toString()
    }

    suspend fun putRequest(request: Request) {
        EventBus.produceEvent(Event.RequestAppearInBuffer(request))
        request.timeOfBuffered = Statistic.getTime()
        if (isFull()) {
            replaceOldestRequest(request)
        } else {
            buffer.add(request)
            EventBus.produceEvent(Event.RequestPlacedInBuffer(request, buffer.indexOf(request)))
        }
    }

    private suspend fun replaceOldestRequest(request: Request) {
        val i = buffer.indexOf(buffer.minBy { it.timeOfCreation })
        EventBus.produceEvent(Event.RequestReplacedByNew(buffer[i], request, i))
        buffer[i] = request
    }

    suspend fun getNewestRequest(): Request {
        val r = buffer.maxBy { it.timeOfCreation }
        val pos = buffer.indexOf(r)
        buffer.remove(r)
        r.timeOfRemoved = Statistic.getTime()
        EventBus.produceEvent(Event.RequestWasRemoved(r, pos))
        return r
    }

    private fun isFull(): Boolean {
        return buffer.size >= bufferCapacity
    }

    fun isEmpty(): Boolean {
        return buffer.isEmpty()
    }

}