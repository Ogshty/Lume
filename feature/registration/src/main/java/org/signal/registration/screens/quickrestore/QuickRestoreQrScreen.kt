/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.quickrestore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.QrCode
import org.signal.core.ui.compose.QrCodeData
import org.signal.core.ui.compose.SignalIcons

/**
 * Screen to display QR code for restoring from an old device.
 * The old device scans this QR code to initiate the transfer.
 */
@Composable
fun QuickRestoreQrScreen(
  state: QuickRestoreQrState,
  onEvent: (QuickRestoreQrEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val animationState = remember {
    MutableTransitionState(false).apply {
      targetState = true
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(32.dp))

    AnimatedVisibility(
      visibleState = animationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
    ) {
      Text(
        text = "Scan from old device",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp
        ),
        textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(48.dp))

    // QR Code display area
    AnimatedVisibility(
      visibleState = animationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
      ElevatedCard(
        modifier = Modifier
          .widthIn(max = 280.dp)
          .aspectRatio(1f),
        shape = RoundedCornerShape(28.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surface)
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            AnimatedContent(
              targetState = state.qrState,
              contentKey = { it::class },
              label = "qr-code-state"
            ) { qrState ->
              when (qrState) {
                is QrState.Loaded -> {
                  QrCode(
                    data = qrState.qrCodeData,
                    foregroundColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                  )
                }

                QrState.Loading -> {
                  CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                QrState.Scanned -> {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = "QR code scanned",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                      onClick = { onEvent(QuickRestoreQrEvents.RetryQrCode) },
                      shape = RoundedCornerShape(28.dp)
                    ) {
                      Text("Retry")
                    }
                  }
                }

                QrState.Failed -> {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Text(
                      text = "Failed to generate QR code",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.error,
                      textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                      onClick = { onEvent(QuickRestoreQrEvents.RetryQrCode) },
                      shape = RoundedCornerShape(28.dp)
                    ) {
                      Text("Retry")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(48.dp))

    // Instructions
    Column(
      modifier = Modifier.widthIn(max = 320.dp)
    ) {
      val instructions = listOf(
        SignalIcons.Phone.painter to "On your old phone, open Signal",
        SignalIcons.Camera.painter to "Go to Settings > Transfer account",
        SignalIcons.QrCode.painter to "Scan this QR code"
      )

      instructions.forEachIndexed { index, (icon, text) ->
        AnimatedVisibility(
          visibleState = animationState,
          enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
            slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 + index * 40 }
        ) {
          InstructionRow(
            icon = icon,
            instruction = text
          )
        }
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    AnimatedVisibility(
      visibleState = animationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it }
    ) {
      Buttons.LargeTonal(
        onClick = { onEvent(QuickRestoreQrEvents.Cancel) },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Cancel",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
  }

  // Loading dialog
  if (state.isRegistering) {
    AlertDialog(
      onDismissRequest = { },
      confirmButton = { },
      text = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(16.dp))
          Text("Registering...")
        }
      }
    )
  }

  // Error dialog
  if (state.showRegistrationError) {
    AlertDialog(
      onDismissRequest = { onEvent(QuickRestoreQrEvents.DismissError) },
      confirmButton = {
        TextButton(onClick = { onEvent(QuickRestoreQrEvents.DismissError) }) {
          Text("OK")
        }
      },
      text = {
        Text(state.errorMessage ?: "An error occurred during registration")
      }
    )
  }
}

@Composable
private fun InstructionRow(
  icon: Painter,
  instruction: String
) {
  Row(
    modifier = Modifier
      .padding(vertical = 4.dp)
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }

    Spacer(modifier = Modifier.width(16.dp))

    Text(
      text = instruction,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@AllDevicePreviews
@Composable
private fun QuickRestoreQrScreenLoadingPreview() {
  Previews.Preview {
    QuickRestoreQrScreen(
      state = QuickRestoreQrState(qrState = QrState.Loading),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun QuickRestoreQrScreenLoadedPreview() {
  Previews.Preview {
    QuickRestoreQrScreen(
      state = QuickRestoreQrState(
        qrState = QrState.Loaded(QrCodeData.forData("sgnl://rereg?uuid=test&pub_key=test", false))
      ),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun QuickRestoreQrScreenFailedPreview() {
  Previews.Preview {
    QuickRestoreQrScreen(
      state = QuickRestoreQrState(qrState = QrState.Failed),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun QuickRestoreQrScreenRegisteringPreview() {
  Previews.Preview {
    QuickRestoreQrScreen(
      state = QuickRestoreQrState(
        qrState = QrState.Scanned,
        isRegistering = true
      ),
      onEvent = {}
    )
  }
}
