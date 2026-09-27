package com.cufa.workshop.obd

class ObdRepository(private val adapter: ObdAdapter) {
    suspend fun devices() = adapter.scan()
    suspend fun connect(device: ObdDevice) = adapter.connect(device)
    suspend fun snapshot(): Result<ObdSnapshot> {
        if (!adapter.isConnected()) return Result.failure(IllegalStateException("OBD not connected"))
        val vin = adapter.readVin().getOrNull()
        val dtcs = adapter.readDtcs().getOrElse { return Result.failure(it) }
        val live = adapter.readLiveData(listOf("010C","010D","0105","0142")).getOrElse { return Result.failure(it) }
        return Result.success(ObdSnapshot(vin, dtcs, live))
    }
}
data class ObdSnapshot(val vin:String?, val dtcs:List<Dtc>, val live:List<LivePid>)
