package com.example.myapplication.presentation.scanner

import java.util.concurrent.atomic.AtomicBoolean

class ScannerCameraController {
    private val captureRequested = AtomicBoolean(false)

    fun requestCapture() {
        captureRequested.set(true)
    }

    fun consumeCaptureRequest(): Boolean = captureRequested.getAndSet(false)
}
