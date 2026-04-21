/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.pinentry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.SignalIcons
import org.signal.registration.screens.RegistrationScreen

/**
 * PIN entry screen for the registration flow.
 * Allows users to enter their PIN to restore their account.
 */
@Composable
fun PinEntryScreen(
  state: PinEntryState,
  onEvent: (PinEntryScreenEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  var pin by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val scrollState = rememberScrollState()

  val headerAnimationState = remember { MutableTransitionState<Boolean>(false).apply { targetState = true } }
  val contentAnimationState = remember { MutableTransitionState<Boolean>(false).apply { targetState = true } }
  val footerAnimationState = remember { MutableTransitionState<Boolean>(false).apply { targetState = true } }

  RegistrationScreen(
    modifier = modifier.fillMaxSize(),
    content = {
      Box(
        modifier = Modifier.fillMaxSize()
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Top
        ) {
          Spacer(modifier = Modifier.height(64.dp))

          AnimatedVisibility(
            visibleState = headerAnimationState,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
              slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
          ) {
            Column {
              val titleString = remember {
                return@remember when (state.mode) {
                  PinEntryState.Mode.RegistrationLock -> "Registration Lock"
                  PinEntryState.Mode.SvrRestore,
                  PinEntryState.Mode.SmsBypass -> "Enter your PIN"
                }
              }

              Text(
                text = titleString,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "Enter the PIN you created when you first installed Signal",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(48.dp))

          AnimatedVisibility(
            visibleState = contentAnimationState,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
              slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 4 }
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              TextField(
                value = pin,
                onValueChange = { pin = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(focusRequester)
                  .clip(RoundedCornerShape(28.dp)),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                  textAlign = TextAlign.Center,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 8.sp
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                  keyboardType = if (state.isAlphanumericKeyboard) KeyboardType.Password else KeyboardType.NumberPassword,
                  imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                  onDone = {
                    if (pin.isNotEmpty()) {
                      onEvent(PinEntryScreenEvents.PinEntered(pin))
                    }
                  }
                ),
                isError = state.triesRemaining != null
              )

              if (state.triesRemaining != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Incorrect PIN. ${state.triesRemaining} attempts remaining.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.error,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth()
                )
              } else {
                Spacer(modifier = Modifier.height(12.dp))
              }

              if (state.showNeedHelp) {
                OutlinedButton(
                  onClick = { onEvent(PinEntryScreenEvents.NeedHelp) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                  shape = RoundedCornerShape(28.dp)
                ) {
                  Text(
                    "Need help?",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                  )
                }
                Spacer(modifier = Modifier.height(12.dp))
              }

              OutlinedButton(
                onClick = { onEvent(PinEntryScreenEvents.ToggleKeyboard) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
              ) {
                Icon(
                  painter = SignalIcons.Keyboard.painter,
                  contentDescription = null,
                  modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                  "Switch keyboard",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }
          }

          Spacer(modifier = Modifier.weight(1f))
        }

        // Skip button in top right
        TextButton(
          onClick = { onEvent(PinEntryScreenEvents.Skip) },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
        ) {
          Text(
            text = "Skip",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    },
    footer = {
      AnimatedVisibility(
        visibleState = footerAnimationState,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
          slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          Buttons.LargeTonal(
            onClick = {
              if (pin.isNotEmpty()) {
                onEvent(PinEntryScreenEvents.PinEntered(pin))
              }
            },
            enabled = pin.isNotEmpty(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              "Continue",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  )

  // Auto-focus PIN field on initial composition
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
}

@AllDevicePreviews
@Composable
private fun PinEntryScreenPreview() {
  Previews.Preview {
    PinEntryScreen(
      state = PinEntryState(),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun PinEntryScreenWithErrorPreview() {
  Previews.Preview {
    PinEntryScreen(
      state = PinEntryState(
        mode = PinEntryState.Mode.RegistrationLock,
        triesRemaining = 3,
        showNeedHelp = true
      ),
      onEvent = {}
    )
  }
}
