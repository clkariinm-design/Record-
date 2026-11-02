package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()

                // Permission launcher for Notifications (Android 13+) and Audio Recording
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
                    if (audioGranted) {
                        viewModel.showToast("Microphone audio permission granted")
                    }
                }

                // MediaProjection system capture consent launcher
                val mediaProjectionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        val metrics = resources.displayMetrics
                        val width = metrics.widthPixels
                        val height = metrics.heightPixels
                        val densityDpi = metrics.densityDpi

                        viewModel.onScreenCaptureGranted(
                            resultCode = result.resultCode,
                            resultData = result.data!!,
                            width = width,
                            height = height,
                            densityDpi = densityDpi
                        )
                    } else {
                        viewModel.onScreenCaptureDenied()
                    }
                }

                // Request runtime permissions on first launch
                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                }

                // Collect trigger from ViewModel to show Android MediaProjection consent prompt
                LaunchedEffect(viewModel) {
                    viewModel.requestScreenCaptureChannel.collect {
                        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                        if (mpManager != null) {
                            mediaProjectionLauncher.launch(mpManager.createScreenCaptureIntent())
                        } else {
                            viewModel.showToast("MediaProjection service not available on this device")
                        }
                    }
                }

                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
