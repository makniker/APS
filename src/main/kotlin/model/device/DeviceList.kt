package org.example.model.device

import org.example.Event
import org.example.EventBus

class DeviceList(private val numOfDevices: Int) {
    private val list = (1..numOfDevices).map { Device(it) }
    private var pointer: Int = 0

    suspend fun initList() {
        for (i in 1..numOfDevices) {
            EventBus.produceEvent(Event.DeviceFree(i))
        }
    }

    fun getNewestDevice() : Device {
        var newPointer = list.indexOfFirst { !it.isBusy && list.indexOf(it) > pointer }
        if (newPointer == -1) {
            newPointer = list.indexOfFirst { !it.isBusy && list.indexOf(it) <= pointer }
        }
        pointer = newPointer
        return list[newPointer]
    }
}