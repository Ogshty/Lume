/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.accountlocked

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews

/**
 * Screen shown when the user's account is locked due to too many failed PIN attempts
 * and there's no SVR data available to recover.
 */
@Composable
fun AccountLockedScreen(
  state: AccountLockedState,
  onEvent: (AccountLockedScreenEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  val animationState = remember {
    MutableTransitionState(false).apply {
      targetState = true
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(64.dp))

      AnimatedVisibility(
        visibleState = animationState,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
          slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
      ) {
        Text(
          text = "Account locked",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
          ),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      AnimatedVisibility(
        visibleState = animationState,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
          slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it }
      ) {
        Text(
          text = "Your account has been locked to protect your privacy and security. After ${state.daysRemaining} days of inactivity in your account you'll be able to re-register this phone number without needing your PIN. All content will be deleted.",
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AnimatedVisibility(
        visibleState = animationState,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
          slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it }
      ) {
        Column {
          Buttons.LargeTonal(
            onClick = { onEvent(AccountLockedScreenEvents.Next) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
          ) {
            Text(
              text = "Next",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = { onEvent(AccountLockedScreenEvents.LearnMore) },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            shape = RoundedCornerShape(28.dp)
          ) {
            Text(
              text = "Learn More",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@AllDevicePreviews
@Composable
private fun AccountLockedScreenPreview() {
  Previews.Preview {
    AccountLockedScreen(
      state = AccountLockedState(daysRemaining = 7),
      onEvent = {}
    )
  }
}
