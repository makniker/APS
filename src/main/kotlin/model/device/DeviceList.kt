package org.example.model.device

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.Event
import org.example.EventBus

class DeviceList(private val numOfDevices: Int) {
    private val list = (1..numOfDevices).map { Device(it) }
    private var pointer: Int = 0
    val mutex = Mutex()

    suspend fun initList() {
        for (i in 1..numOfDevices) {
            EventBus.produceEvent(Event.DeviceFree(i, 0))
        }
    }

    fun getState() {
        for (i in list) {
            println(i.isBusy)
        }
    }

    fun isAllFree(): Boolean =
        list.none { it.isBusy }

    suspend fun getNewestDevice() : Device = mutex.withLock {
        var d = list.subList(pointer, list.size).firstOrNull { !it.isBusy }
        if (d == null) {
            d = list.subList(0, pointer).first { !it.isBusy }
        }
        d.isBusy = true
        pointer = list.indexOf(d)
        return d
    }
}