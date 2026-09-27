package com.cufa.workshop.obd

data class ObdDevice(val id:String,val name:String)
data class Dtc(val code:String,val description:String?=null)
data class LivePid(val pid:String,val label:String,val value:String,val unit:String?=null)

interface ObdAdapter {
    suspend fun scan(): List<ObdDevice>
    suspend fun connect(device: ObdDevice): Result<Unit>
    suspend fun disconnect()
    suspend fun readVin(): Result<String?>
    suspend fun readDtcs(): Result<List<Dtc>>
    suspend fun readLiveData(pids: List<String>): Result<List<LivePid>>
    fun isConnected(): Boolean
}
