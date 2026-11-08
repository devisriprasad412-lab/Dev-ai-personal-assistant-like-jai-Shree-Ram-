package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.screens.JarvisMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.JarvisViewModel
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with intent per Firebase AI Logic contract
        configureAppCheck(this, intent)

        viewModel.attachActivity(this)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Audio recording permission for voice assistant
                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.refreshDeviceStatus()
                    }
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoidBlack
                ) {
                    JarvisMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceStatus()
    }
}
