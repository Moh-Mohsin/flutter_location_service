package com.innovent.background_location_service

import io.flutter.plugin.common.EventChannel

object ChannelHolder {
    private val sinkList= mutableListOf<EventChannel.EventSink>()

    fun add(sink:EventChannel.EventSink){
        sinkList.add(sink)
    }

    fun brodcast(any: Any){
        sinkList.forEach{it->
            it.success(any)
        }
    }
}