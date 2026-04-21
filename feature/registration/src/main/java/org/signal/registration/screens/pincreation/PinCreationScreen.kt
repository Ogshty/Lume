/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.pincreation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.SignalIcons
import org.signal.registration.R
import org.signal.registration.screens.RegistrationScreen

/**
 * PIN creation screen for the registration flow.
 * Allows users to create a new PIN for their account.
 */
@Composable
fun PinCreationScreen(
  state: PinCreationState,
  onEvent: (PinCreationScreenEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  var pin by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val scrollState = rememberScrollState()

  val animationState = remember {
    MutableTransitionState(false).apply {
      targetState = true
    }
  }

  RegistrationScreen(
    modifier = modifier.fillMaxSize(),
    content = {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
      ) {
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
          visibleState = animationState,
          enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
            slideInVertically(spring(stiffness = Spring.StiffnessLow)) { -it / 2 }
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(56.dp)
                .background(
                  MaterialTheme.colorScheme.primaryContainer,
                  RoundedCornerShape(12.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = SignalIcons.Lock.painter,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
              text = "Create your PIN",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
              ),
              textAlign = TextAlign.Start,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
          visibleState = animationState,
          enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
            slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
        ) {
          val descriptionText = buildAnnotatedString {
            append("PINs can help you restore your account if you lose your phone. ")
            pushStringAnnotation(tag = "LEARN_MORE", annotation = "learn_more")
            withStyle(
              style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold
              )
            ) {
              append("Learn more")
            }
            pop()
          }

          ClickableText(
            text = descriptionText,
            style = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Start
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = { offset ->
              descriptionText.getStringAnnotations(tag = "LEARN_MORE", start = offset, end = offset)
                .firstOrNull()?.let {
                  onEvent(PinCreationScreenEvents.LearnMore)
                }
            }
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
          visibleState = animationState,
          enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
            slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
        ) {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              TextField(
                value = pin,
                onValueChange = { pin = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                  textAlign = TextAlign.Center,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 8.sp
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                  unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                  disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                  keyboardType = if (state.isAlphanumericKeyboard) KeyboardType.Password else KeyboardType.NumberPassword,
                  imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                  onDone = {
                    if (pin.length >= 4) {
                      onEvent(PinCreationScreenEvents.PinSubmitted(pin))
                    }
                  }
                )
              )

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = state.inputLabel ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(24.dp))

              Buttons.LargeTonal(
                onClick = { onEvent(PinCreationScreenEvents.ToggleKeyboard) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
              ) {
                Icon(
                  painter = SignalIcons.Keyboard.painter,
                  contentDescription = null,
                  modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                  text = if (state.isAlphanumericKeyboard) "Switch to numeric" else "Switch to alphanumeric",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))
      }
    },
    footer = {
      AnimatedVisibility(
        visibleState = animationState,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
          slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it }
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          Buttons.LargeTonal(
            onClick = { onEvent(PinCreationScreenEvents.PinSubmitted(pin)) },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              "Next",
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
private fun PinCreationScreenPreview() {
  Previews.Preview {
    PinCreationScreen(
      state = PinCreationState(
        inputLabel = "PIN must be at least 4 digits"
      ),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun PinCreationScreenAlphanumericPreview() {
  Previews.Preview {
    PinCreationScreen(
      state = PinCreationState(
        isAlphanumericKeyboard = false,
        inputLabel = "PIN must be at least 4 characters"
      ),
      onEvent = {}
    )
  }
}
