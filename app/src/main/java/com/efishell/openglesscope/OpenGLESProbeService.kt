package com.efishell.openglesscope

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.system.Os
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.json.JSONObject

class OpenGLESProbeService : Service() {
    companion object {
        private val PROCESS_NATIVE_PROBE_LOCK = ReentrantLock(true)
        const val EXTRA_RESULT_PATH = "result_path"
        const val EXTRA_TERMINAL_PATH = "terminal_path"
        const val EXTRA_TIMEOUT_MS = "timeout_ms"
        const val ACTION_ABORT = "com.efishell.openglesscope.action.ABORT_PROBE"
        const val EXTRA_SELF_TEST = "self_test"
        private const val MAX_RESULT_BYTES = 8L * 1024L * 1024L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val terminationClaimed = AtomicBoolean(false)
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "OpenGLESProbeWorker") }

    external fun nativeCollect(): String
    external fun nativeSelfTest(): String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ABORT) {
            terminateDedicatedProcess("Abort requested for dedicated OpenGL ES probe process")
            return START_NOT_STICKY
        }
        val resultPath = intent?.getStringExtra(EXTRA_RESULT_PATH)
        val terminalPath = intent?.getStringExtra(EXTRA_TERMINAL_PATH)
        val timeoutMs = intent?.getLongExtra(EXTRA_TIMEOUT_MS, 0L) ?: 0L
        val selfTest = intent?.getBooleanExtra(EXTRA_SELF_TEST, false) == true
        if (resultPath.isNullOrBlank() || terminalPath.isNullOrBlank() || timeoutMs !in 1_000L..60_000L) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        val probeRoot = File(cacheDir, "probe").canonicalFile
        val requestedResult = runCatching { File(resultPath).canonicalFile }.getOrNull()
        val requestedTerminal = runCatching { File(terminalPath).canonicalFile }.getOrNull()
        if (requestedResult == null || requestedTerminal == null ||
            requestedResult.parentFile != probeRoot || requestedTerminal.parentFile != probeRoot ||
            requestedTerminal.path != requestedResult.path + ".done" ||
            !requestedResult.name.startsWith("opengles-") || !requestedResult.name.endsWith(".json")) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        val hardTimeoutWatchdog = Runnable {
            terminateDedicatedProcess("Hard OpenGL ES probe deadline reached; terminating the dedicated probe process", true)
        }
        mainHandler.postDelayed(hardTimeoutWatchdog, timeoutMs + 100L)

        worker.execute {
            var published = false
            try {
                System.loadLibrary("openglesscope")
                val result = PROCESS_NATIVE_PROBE_LOCK.withLock { if (selfTest) nativeSelfTest() else nativeCollect() }
                if (result.toByteArray(Charsets.UTF_8).size > MAX_RESULT_BYTES) {
                    throw IllegalStateException("OpenGL ES probe result exceeded the 8 MiB safety limit")
                }
                published = writeResult(requestedResult, result)
                if (!published) throw IllegalStateException("OpenGL ES probe result could not be published")
            } catch (error: Throwable) {
                published = writeResult(
                    requestedResult,
                    JSONObject().put("status", "unavailable")
                        .put("reason", error.message ?: if (selfTest) "OpenGL ES self-test failed" else "OpenGL ES probe failed")
                        .toString()
                )
            } finally {
                mainHandler.removeCallbacks(hardTimeoutWatchdog)
                var terminalPayloadValid = published && validateTerminalResult(requestedResult, selfTest)
                if (published && !terminalPayloadValid) {
                    val validationIssue = runCatching {
                        OpenGLESProbeContract.terminalValidationIssue(requestedResult.readText(Charsets.UTF_8), selfTest)
                    }.getOrNull() ?: "terminal validation failed"
                    Log.e("OpenGLESProbeWork", "Native probe evidence rejected: $validationIssue")
                    published = writeResult(
                        requestedResult,
                        JSONObject().put("status", "unavailable")
                            .put("reason", "Native probe report validation failed: $validationIssue")
                            .toString()
                    )
                    terminalPayloadValid = published && validateTerminalResult(requestedResult, selfTest)
                }
                val terminalPublished = terminalPayloadValid && writeResult(requestedTerminal, "done")
                if (published && !terminalPublished) {
                    Log.e("OpenGLESProbeWork", "Unable to publish OpenGL ES probe terminal marker after terminal validation")
                }
                terminateDedicatedProcess(
                    if (terminalPublished) {
                        "Terminal OpenGL ES result and service-owned completion marker are durable; terminating the one-shot probe process"
                    } else {
                        "OpenGL ES probe worker finished without a durable completion marker; terminating the one-shot probe process"
                    }
                )
            }
        }
        return START_NOT_STICKY
    }

    private fun validateTerminalResult(file: File, selfTest: Boolean): Boolean {
        if (!file.isFile || file.length() !in 1L..MAX_RESULT_BYTES) return false
        return runCatching { OpenGLESProbeContract.isTerminalJson(file.readText(Charsets.UTF_8), selfTest) }.getOrDefault(false)
    }

    private fun writeResult(file: File, text: String): Boolean {
        return runCatching {
            val bytes = text.toByteArray(Charsets.UTF_8)
            if (bytes.size > MAX_RESULT_BYTES) throw IllegalStateException("Probe publication exceeded the 8 MiB safety limit")
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, file.name + ".tmp")
            FileOutputStream(temp, false).use { output ->
                output.write(bytes)
                output.fd.sync()
            }
            Os.rename(temp.absolutePath, file.absolutePath)
            true
        }.onFailure { error ->
            runCatching { File(file.parentFile, file.name + ".tmp").delete() }
            Log.e("OpenGLESProbeWork", "Unable to publish OpenGL ES probe result", error)
        }.getOrDefault(false)
    }

    private fun terminateDedicatedProcess(reason: String, error: Boolean = false) {
        if (!terminationClaimed.compareAndSet(false, true)) return
        if (error) Log.e("OpenGLESProbeWork", reason) else Log.i("OpenGLESProbeWork", reason)
        Process.killProcess(Process.myPid())
    }

    override fun onDestroy() {
        worker.shutdownNow()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
        terminateDedicatedProcess("Probe service teardown requested; terminating the dedicated process")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
