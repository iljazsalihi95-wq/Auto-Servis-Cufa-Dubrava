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
        val remote: BluetoothDevice = adapter?.getRemoteDevice(device.address)
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

    override suspend fun readVin(): Result<String?> = runCatching {
        val raw=command("0902")
        raw.takeIf { it.isNotBlank() && !it.contains("NO DATA",true) }
    }

    override suspend fun readDtcs(): Result<List<Dtc>> = runCatching {
        val raw=command("03")
        if(raw.contains("NO DATA",true)) emptyList() else listOf(Dtc(raw, "Raw OBD-II response"))
    }

    override suspend fun readLiveData(pids: List<String>): Result<List<LivePid>> = runCatching {
        pids.map { pid -> LivePid(pid, command(pid), "") }
    }

    override fun isConnected() = connected
}
