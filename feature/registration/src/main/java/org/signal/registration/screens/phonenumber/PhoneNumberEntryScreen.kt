/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.phonenumber

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Dialogs
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.SignalIcons
import org.signal.core.util.E164Util
import org.signal.registration.R
import org.signal.registration.screens.phonenumber.PhoneNumberEntryState.OneTimeEvent
import org.signal.registration.test.TestTags

/**
 * Phone number entry screen
 */
@Composable
fun PhoneNumberScreen(
  state: PhoneNumberEntryState,
  onEvent: (PhoneNumberEntryScreenEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  val resources = LocalResources.current
  var simpleErrorMessage: String? by remember { mutableStateOf(null) }

  if (state.showDialog) {
    Dialogs.SimpleAlertDialog(
      title = stringResource(R.string.RegistrationActivity_is_the_phone_number),
      body = "${E164Util.formatAsE164WithCountryCodeForDisplay(state.countryCode, state.nationalNumber)}\n\n${stringResource(R.string.RegistrationActivity_a_verification_code)}",
      confirm = stringResource(id = android.R.string.ok),
      dismiss = stringResource(R.string.RegistrationActivity_edit_number),
      onConfirm = { onEvent(PhoneNumberEntryScreenEvents.PhoneNumberSubmitted) },
      onDismiss = { onEvent(PhoneNumberEntryScreenEvents.PhoneNumberCancelled) }
    )
  }

  LaunchedEffect(state.oneTimeEvent) {
    onEvent(PhoneNumberEntryScreenEvents.ConsumeOneTimeEvent)
    when (state.oneTimeEvent) {
      OneTimeEvent.NetworkError -> simpleErrorMessage = resources.getString(R.string.VerificationCodeScreen__network_error)
      is OneTimeEvent.RateLimited -> simpleErrorMessage = resources.getString(R.string.VerificationCodeScreen__too_many_attempts_try_again_in_s, state.oneTimeEvent.retryAfter.toString())
      OneTimeEvent.UnknownError -> simpleErrorMessage = resources.getString(R.string.VerificationCodeScreen__an_unexpected_error_occurred)
      OneTimeEvent.CouldNotRequestCodeWithSelectedTransport -> simpleErrorMessage = resources.getString(R.string.VerificationCodeScreen__could_not_send_code_via_selected_method)
      OneTimeEvent.UnableToSendSms -> simpleErrorMessage = resources.getString(R.string.VerificationCodeScreen__unable_to_send_sms)
      null -> Unit
    }
  }

  simpleErrorMessage?.let { message ->
    Dialogs.SimpleMessageDialog(
      message = message,
      dismiss = stringResource(android.R.string.ok),
      onDismiss = { simpleErrorMessage = null }
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag(TestTags.PHONE_NUMBER_SCREEN)
  ) {
    ScreenContent(state, onEvent)
  }
}

@Composable
private fun ScreenContent(state: PhoneNumberEntryState, onEvent: (PhoneNumberEntryScreenEvents) -> Unit) {
  val selectedCountry = state.countryName
  val selectedCountryEmoji = state.countryEmoji
  val scrollState = rememberScrollState()

  val headerAnimationState = remember { MutableTransitionState(false).apply { targetState = true } }
  val contentAnimationState = remember { MutableTransitionState(false).apply { targetState = true } }
  val footerAnimationState = remember { MutableTransitionState(false).apply { targetState = true } }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 24.dp)
  ) {
    Spacer(modifier = Modifier.height(64.dp))

    AnimatedVisibility(
      visibleState = headerAnimationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
    ) {
      Column {
        Text(
          text = stringResource(R.string.RegistrationActivity_phone_number),
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = stringResource(R.string.RegistrationActivity_you_will_receive_a_verification_code),
          style = MaterialTheme.typography.bodyLarge,
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
      Column {
        CountryPicker(
          emoji = selectedCountryEmoji,
          country = selectedCountry,
          onClick = { onEvent(PhoneNumberEntryScreenEvents.CountryPicker) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.PHONE_NUMBER_COUNTRY_PICKER)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PhoneNumberInputFields(
          hasValidCountry = state.countryName.isNotEmpty(),
          countryCode = state.countryCode,
          formattedNumber = state.formattedNumber,
          onCountryCodeChanged = { onEvent(PhoneNumberEntryScreenEvents.CountryCodeChanged(it)) },
          onPhoneNumberChanged = { onEvent(PhoneNumberEntryScreenEvents.PhoneNumberChanged(it)) },
          onPhoneNumberEntered = { onEvent(PhoneNumberEntryScreenEvents.PhoneNumberEntered) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    AnimatedVisibility(
      visibleState = footerAnimationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (state.showSpinner) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
          )
        } else {
          Buttons.LargeTonal(
            onClick = { onEvent(PhoneNumberEntryScreenEvents.PhoneNumberEntered) },
            enabled = state.countryCode.isNotEmpty() && state.nationalNumber.isNotEmpty(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.testTag(TestTags.PHONE_NUMBER_NEXT_BUTTON)
          ) {
            Text(
              text = stringResource(R.string.RegistrationActivity_next),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CountryPicker(
  emoji: String,
  country: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(28.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .clickable(onClick = onClick)
      .height(72.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (emoji.isNotEmpty()) {
          Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
          )
        } else {
          Icon(
            painter = SignalIcons.Search.painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = country.takeIf { country.isNotEmpty() } ?: stringResource(R.string.RegistrationActivity_select_a_country),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        color = if (country.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f)
      )

      Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.symbol_drop_down_24),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

/**
 * Phone number input fields containing the country code and phone number text fields.
 */
@Composable
private fun PhoneNumberInputFields(
  hasValidCountry: Boolean,
  countryCode: String,
  formattedNumber: String,
  onCountryCodeChanged: (String) -> Unit,
  onPhoneNumberChanged: (String) -> Unit,
  onPhoneNumberEntered: () -> Unit,
  modifier: Modifier = Modifier
) {
  var phoneNumberTextFieldValue by remember { mutableStateOf(TextFieldValue(formattedNumber)) }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(formattedNumber) {
    if (phoneNumberTextFieldValue.text != formattedNumber) {
      val oldText = phoneNumberTextFieldValue.text
      val oldCursorPos = phoneNumberTextFieldValue.selection.end
      val digitsBeforeCursor = oldText.take(oldCursorPos).count { it.isDigit() }

      var digitCount = 0
      var newCursorPos = formattedNumber.length
      for (i in formattedNumber.indices) {
        if (formattedNumber[i].isDigit()) {
          digitCount++
        }
        if (digitCount >= digitsBeforeCursor) {
          newCursorPos = i + 1
          break
        }
      }

      phoneNumberTextFieldValue = TextFieldValue(
        text = formattedNumber,
        selection = TextRange(newCursorPos)
      )
    }
  }

  LaunchedEffect(hasValidCountry) {
    if (hasValidCountry) {
      focusRequester.requestFocus()
    }
  }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Bottom
  ) {
    TextField(
      value = countryCode,
      onValueChange = onCountryCodeChanged,
      modifier = Modifier
        .width(88.dp)
        .testTag(TestTags.PHONE_NUMBER_COUNTRY_CODE_FIELD),
      prefix = {
        Text(
          text = "+",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
      ),
      singleLine = true,
      shape = RoundedCornerShape(28.dp),
      textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium
      ),
      colors = TextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent
      )
    )

    Spacer(modifier = Modifier.width(12.dp))

    TextField(
      value = phoneNumberTextFieldValue,
      onValueChange = { newValue ->
        phoneNumberTextFieldValue = newValue
        onPhoneNumberChanged(newValue.text)
      },
      modifier = Modifier
        .weight(1f)
        .focusRequester(focusRequester)
        .testTag(TestTags.PHONE_NUMBER_PHONE_FIELD),
      placeholder = {
        Text(stringResource(R.string.RegistrationActivity_phone_number_description))
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(
        onDone = { onPhoneNumberEntered() }
      ),
      singleLine = true,
      shape = RoundedCornerShape(28.dp),
      textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium
      ),
      colors = TextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent
      )
    )
  }
}

@AllDevicePreviews
@Composable
private fun PhoneNumberScreenPreview() {
  Previews.Preview {
    PhoneNumberScreen(
      state = PhoneNumberEntryState(),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun PhoneNumberScreenSpinnerPreview() {
  Previews.Preview {
    PhoneNumberScreen(
      state = PhoneNumberEntryState(showSpinner = true),
      onEvent = {}
    )
  }
}
