package com.efishell.openglesscope

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.json.JSONObject

class OpenGLESProbeService : Service() {
    companion object {
        private val PROCESS_NATIVE_PROBE_LOCK = ReentrantLock(true)
        const val EXTRA_RESULT_PATH = "result_path"
        const val ACTION_ABORT = "com.efishell.openglesscope.action.ABORT_PROBE"
    }

    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "OpenGLESProbeWorker") }

    external fun nativeCollect(): String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ABORT) {
            android.os.Process.killProcess(android.os.Process.myPid())
            return START_NOT_STICKY
        }
        val resultPath = intent?.getStringExtra(EXTRA_RESULT_PATH)
        if (resultPath.isNullOrBlank()) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
        val probeRoot = File(cacheDir, "probe").canonicalFile
        val requestedResult = runCatching { File(resultPath).canonicalFile }.getOrNull()
        if (requestedResult == null || requestedResult.parentFile != probeRoot || !requestedResult.name.startsWith("opengles-") || !requestedResult.name.endsWith(".json")) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
        worker.execute {
            try {
                System.loadLibrary("openglesscope")
                val result = PROCESS_NATIVE_PROBE_LOCK.withLock { nativeCollect() }
                val encodedSize = result.toByteArray(Charsets.UTF_8).size
                if (encodedSize > 8 * 1024 * 1024) {
                    writeResult(requestedResult, JSONObject().put("status", "unavailable").put("reason", "OpenGL ES probe result exceeded the 8 MiB safety limit").toString())
                } else {
                    writeResult(requestedResult, result)
                }
            } catch (error: LinkageError) {
                writeResult(requestedResult, JSONObject().put("status", "unavailable").put("reason", error.message ?: "OpenGL ES native probe could not be loaded").toString())
            } catch (error: Exception) {
                writeResult(requestedResult, JSONObject().put("status", "unavailable").put("reason", error.message ?: "OpenGL ES probe failed").toString())
            } finally {
                stopSelfResult(startId)
            }
        }
        return START_NOT_STICKY
    }

    private fun writeResult(file: File, text: String) {
        runCatching {
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, file.name + ".tmp")
            FileOutputStream(temp, false).use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (error: java.nio.file.AtomicMoveNotSupportedException) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }.onFailure { error ->
            runCatching { File(file.parentFile, file.name + ".tmp").delete() }
            Log.e("OpenGLESProbeWork", "Unable to publish OpenGL ES probe result", error)
        }
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
