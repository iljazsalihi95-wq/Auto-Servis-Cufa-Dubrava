package com.cufa.workshop.obd

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.util.UUID

/**
 * CUFA hardware profile: OBDLink MX+
 * Bluetooth Classic / serial command transport.
 * OBDLink publishes the STN11xx AT/ST command documentation for developers.
 * Phase 1 is intentionally read-only: VIN, DTC and live OBD-II PIDs.
 */
class BluetoothObdAdapter(context: Context) : ObdAdapter {
    private val manager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? get() = manager.adapter
    private var socket: BluetoothSocket? = null
    private var connected = false
    private val spp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override suspend fun scan(): List<ObdDevice> =
        adapter?.bondedDevices
            ?.filter { it.name?.contains("OBDLink", ignoreCase = true) == true }
            ?.map { ObdDevice(it.address, it.name ?: "OBDLink MX+") }
            ?: emptyList()

    override suspend fun connect(device: ObdDevice): Result<Unit> = runCatching {
        val remote: BluetoothDevice = adapter?.getRemoteDevice(device.id)
            ?: error("Bluetooth nuk është aktiv")
        socket?.close()
        socket = remote.createRfcommSocketToServiceRecord(spp)
        socket!!.connect()
        connected = true
        initialize()
    }

    private fun initialize() {
        command("ATZ")
        command("ATE0")
        command("ATL0")
        command("ATS0")
        command("ATH0")
        command("ATSP0")
    }

    private fun command(cmd: String): String {
        check(connected && socket != null) { "OBDLink MX+ nuk është lidhur" }
        val out = socket!!.outputStream
        val input = socket!!.inputStream
        out.write((cmd + "\r").toByteArray())
        out.flush()
        val b = StringBuilder()
        val buf = ByteArray(256)
        val until = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < until) {
            if (input.available() > 0) {
                val n = input.read(buf)
                if (n > 0) {
                    b.append(String(buf, 0, n))
                    if (b.contains(">")) break
                }
            } else Thread.sleep(20)
        }
        return b.toString().replace(">", "").trim()
    }

    override suspend fun disconnect() { runCatching { socket?.close() }; socket=null; connected=false }

    private fun hex(raw:String)=Regex("[0-9A-Fa-f]{2}").findAll(raw.replace("\r"," ").replace("\n"," ")).map{it.value.uppercase()}.toList()
    private fun dtc(a:Int,b:Int):String{
        val system="PCBU"[(a shr 6) and 3]
        val d1=(a shr 4) and 0x3
        val d2=(a and 0xF).toString(16).uppercase()
        val d3=((b shr 4) and 0xF).toString(16).uppercase()
        val d4=(b and 0xF).toString(16).uppercase()
        return "$system$d1$d2$d3$d4"
    }
    private fun payload(raw:String, mode:String, pid:String?=null):List<Int>{
        val h=hex(raw); val marker=(mode.toInt(16)+0x40).toString(16).uppercase().padStart(2,'0')
        val i=if(pid==null) h.indexOf(marker) else h.windowed(2).indexOf(listOf(marker,pid.uppercase()))
        if(i<0)return emptyList(); val start=i+1+(if(pid==null)0 else 1); return h.drop(start).map{it.toInt(16)}
    }

    override suspend fun readVin(): Result<String?> = runCatching {
        val bytes=payload(command("0902"),"09","02")
        bytes.dropWhile{it<0x20}.filter{it in 0x20..0x7E}.map{it.toChar()}.joinToString("").trim().takeIf{it.length>=11}
    }

    override suspend fun readDtcs(): Result<List<Dtc>> = runCatching {
        val b=payload(command("03"),"03"); b.chunked(2).filter{it.size==2 && !(it[0]==0&&it[1]==0)}.map{Dtc(dtc(it[0],it[1]),"OBD-II")}
    }

    override suspend fun readLiveData(pids: List<String>): Result<List<LivePid>> = runCatching {
        pids.map { cmd ->
            val pid=cmd.takeLast(2).uppercase(); val b=payload(command(cmd),"01",pid)
            when(pid){
                "0C" -> LivePid("RPM", if(b.size>=2) ((b[0]*256+b[1])/4.0).toString() else "N/A","rpm")
                "0D" -> LivePid("Shpejtësia", b.firstOrNull()?.toString()?:"N/A","km/h")
                "05" -> LivePid("Temperatura motorit", b.firstOrNull()?.let{(it-40).toString()}?:"N/A","°C")
                "42" -> LivePid("Tensioni", if(b.size>=2) String.format("%.3f",(b[0]*256+b[1])/1000.0) else "N/A","V")
                else -> LivePid(pid,b.joinToString(" "){"%02X".format(it)},"")
            }
        }
    }

    override fun isConnected() = connected
}
