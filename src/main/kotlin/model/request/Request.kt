package org.example.model.request

data class Request(val id: Pair<Int, Int>, val timeOfCreation: Long) {
    fun getSourceId(): Int = id.first
    var timeOfBuffered: Long = 0
    var timeOfRemoved: Long = 0
    var timeOfProcessing: Long = 0
    var timeOfFinish: Long = 0
    override fun toString(): String {
        return "${id.first} + ${id.second}"
    }
}