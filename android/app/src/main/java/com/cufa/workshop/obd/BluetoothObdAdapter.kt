package com.cufa.workshop.obd

import android.bluetooth.BluetoothManager
import android.content.Context

/**
 * Hardware boundary for the future CUFA OBD device.
 * Device-specific SDK/protocol code belongs ONLY here (or in another ObdAdapter implementation).
 * This keeps the rest of CUFA Workshop independent of the adapter vendor.
 */
class BluetoothObdAdapter(context: Context) : ObdAdapter {
    private val manager = context.getSystemService(BluetoothManager::class.java)
    private var connected = false

    override suspend fun scan(): List<ObdDevice> {
        // Final BLE / Bluetooth Classic discovery is implemented after the exact certified adapter is selected.
        return emptyList()
    }
    override suspend fun connect(device: ObdDevice): Result<Unit> =
        Result.failure(UnsupportedOperationException("Physical OBD adapter not selected yet"))
    override suspend fun disconnect() { connected = false }
    override suspend fun readVin(): Result<String?> = Result.failure(UnsupportedOperationException("OBD adapter required"))
    override suspend fun readDtcs(): Result<List<Dtc>> = Result.failure(UnsupportedOperationException("OBD adapter required"))
    override suspend fun readLiveData(pids: List<String>): Result<List<LivePid>> = Result.failure(UnsupportedOperationException("OBD adapter required"))
    override fun isConnected() = connected
}
