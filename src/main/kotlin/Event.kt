package org.example

import org.example.model.request.Request

sealed class Event {
    class RequestProduced(val request: Request) : Event() {
        fun getSourceId(): Int = request.getSourceId()
        override fun toString(): String =
            "Request from source ${request.id.first} with id ${request.id.second} was produced at ${Statistic.getTime()}"
    }

    class RequestEnded(val request: Request) : Event() {
        fun getSourceId(): Int = request.getSourceId()
        override fun toString(): String =
            "Request from source ${request.id.first} with id ${request.id.second} stop processing ${Statistic.getTime()}"
    }

    class RequestWasRemoved(val request: Request, val position: Int) : Event() {
        fun getSourceId(): Int = request.getSourceId()
        override fun toString(): String =
            "Request from source ${request.id.first} with id ${request.id.second} was removed from buffer at $position at ${Statistic.getTime()}"

    }

    class RequestStartProcessingOnDevice(val request: Request, private val deviceId: Int) : Event() {
        fun getSourceId(): Int = request.getSourceId()
        override fun toString(): String =
            "Request from source ${request.id.first} with id ${request.id.second} started processing in device with id $deviceId at ${Statistic.getTime()}"
    }

    class RequestReplacedByNew(
        private val oldRequest: Request, private val newRequest: Request, private val position: Int
    ) : Event() {
        fun getSourceId(): Int = oldRequest.getSourceId()
        override fun toString(): String =
            "Request from source ${oldRequest.id.first} with id ${oldRequest.id.second} was replaced by Request from source ${newRequest.id.first} with id ${newRequest.id.second} at $position position at ${Statistic.getTime()}"
    }

    class RequestPlacedInBuffer(val request: Request, private val position: Int) : Event() {
        fun getSourceId(): Int = request.getSourceId()
        override fun toString(): String =
            "Request from source ${request.id.first} with id ${request.id.second} was placed at $position position at ${Statistic.getTime()}"

    }

    class DeviceFree(val deviceId: Int, val workingTime: Long) : Event() {
        override fun toString(): String =
            "Device with id $deviceId free at ${Statistic.getTime()}"

    }
}