package com.fitness.tracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    savedPin: String?,
    onAuthenticated: () -> Unit,
    onSetupPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (savedPin == null) "Set up PIN" else "Enter PIN",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4) {
                    pin = it
                    error = false
                }
            },
            label = { Text("4-digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            isError = error,
            supportingText = if (error) {
                { Text("Incorrect PIN") }
            } else null,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pin.length == 4) {
                    if (savedPin == null) {
                        onSetupPin(pin)
                    } else {
                        if (pin == savedPin) {
                            onAuthenticated()
                        } else {
                            error = true
                            pin = ""
                        }
                    }
                }
            },
            enabled = pin.length == 4
        ) {
            Text(if (savedPin == null) "Set PIN" else "Unlock")
        }
    }
}
