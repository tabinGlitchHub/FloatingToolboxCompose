package com.axby.floatingtoolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axby.floatingtoolbox.ui.FloatingToolbox
import com.axby.floatingtoolbox.ui.theme.FloatingToolboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloatingToolboxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val popPosition = remember { mutableStateOf(Offset.Zero) }
                    // Show/hide state for the popup
                    val showPopup = remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .pointerInput(Unit) {
                                detectTapGestures { offsetInBox ->
                                    popPosition.value = offsetInBox
                                    showPopup.value = true
                                }
                            }
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FloatingToolbox(
                            buttonPosition = popPosition,
                            showPopup = showPopup,
                            onCenterClick = { println("center clicked") },
                            onSectionClick = { println(it) },
                            donutSize = 200.dp,
                            centerButtonIcon = null,
                            animationEnabled = true,
                            slicePopDelay = 30,
                        )

                        Text("Tap anywhere on the screen")
                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FloatingToolboxTheme {
        Greeting("Android")
    }
}