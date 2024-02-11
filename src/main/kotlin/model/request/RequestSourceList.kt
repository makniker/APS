package org.example.model.request

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class RequestSourceList(
    numOfSources: Int
) {
    private val listOfProducer = (1..numOfSources).map { RequestSource(it) }
    suspend fun startGeneratingRequests() = coroutineScope {
        listOfProducer.map {
            launch {
                it.startGeneratingRequests()
            }
        }
    }
}