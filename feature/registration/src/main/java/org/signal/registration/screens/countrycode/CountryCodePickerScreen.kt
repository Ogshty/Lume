/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.countrycode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Dividers
import org.signal.core.ui.compose.IconButtons.IconButton
import org.signal.core.ui.compose.LargeFontPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalIcons
import org.signal.registration.R

/**
 * Screen that allows someone to search and select a country code from a supported list of countries.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CountryCodePickerScreen(
  state: CountryCodeState,
  onEvent: (CountryCodePickerScreenEvents) -> Unit
) {
  val animationState = remember {
    MutableTransitionState(false).apply {
      targetState = true
    }
  }

  Scaffold(
    topBar = {
      Scaffolds.DefaultTopAppBar(
        title = stringResource(R.string.CountryCodeSelectScreen__your_country),
        titleContent = { _, title ->
          Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp
            )
          )
        },
        onNavigationClick = { onEvent(CountryCodePickerScreenEvents.Dismissed) },
        navigationIcon = SignalIcons.X.imageVector,
        navigationContentDescription = stringResource(R.string.CountryCodeSelectScreen__close)
      )
    }
  ) { padding ->
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
      state = listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      stickyHeader {
        SearchBar(
          text = state.query,
          onSearch = { onEvent(CountryCodePickerScreenEvents.Search(it)) }
        )
      }

      if (state.countryList.isEmpty()) {
        item {
          Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              modifier = Modifier.size(56.dp),
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      } else if (state.query.isEmpty()) {
        if (state.commonCountryList.isNotEmpty()) {
          itemsIndexed(state.commonCountryList) { index, country ->
            val itemAnimationState = remember {
              MutableTransitionState(false).apply {
                targetState = true
              }
            }
            AnimatedVisibility(
              visibleState = itemAnimationState,
              enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
            ) {
              CountryItem(country, onEvent)
            }
          }

          item {
            Dividers.Default(modifier = Modifier.padding(vertical = 8.dp))
          }
        }

        itemsIndexed(state.countryList) { index, country ->
          val itemAnimationState = remember {
            MutableTransitionState(false).apply {
              targetState = true
            }
          }
          AnimatedVisibility(
            visibleState = itemAnimationState,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
              slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
          ) {
            CountryItem(country, onEvent)
          }
        }
      } else {
        items(state.filteredList) { country ->
          CountryItem(country, onEvent, state.query)
        }
      }
    }

    LaunchedEffect(state.startingIndex) {
      if (state.startingIndex != 0) {
        coroutineScope.launch {
          listState.scrollToItem(index = state.startingIndex)
        }
      }
    }
  }
}

@Composable
private fun CountryItem(
  country: Country,
  onEvent: (CountryCodePickerScreenEvents) -> Unit = {},
  query: String = ""
) {
  val emoji = country.emoji
  val name = country.name
  val code = "+${country.countryCode}"
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .fillMaxWidth()
      .defaultMinSize(minHeight = 64.dp)
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .clickable { onEvent(CountryCodePickerScreenEvents.CountrySelected(country)) }
      .padding(horizontal = 16.dp)
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = emoji,
        style = MaterialTheme.typography.titleLarge
      )
    }

    if (query.isEmpty()) {
      Text(
        text = name.ifEmpty { stringResource(R.string.CountryCodeSelectScreen__unknown_country) },
        modifier = Modifier
          .padding(start = 16.dp)
          .weight(1f),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = code,
        modifier = Modifier.padding(start = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )
    } else {
      val annotatedName = buildAnnotatedString {
        val startIndex = name.indexOf(query, ignoreCase = true)

        if (startIndex >= 0) {
          append(name.substring(0, startIndex))

          withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
            append(name.substring(startIndex, startIndex + query.length))
          }

          append(name.substring(startIndex + query.length))
        } else {
          append(name)
        }
      }

      val annotatedCode = buildAnnotatedString {
        val startIndex = code.indexOf(query, ignoreCase = true)

        if (startIndex >= 0) {
          append(code.substring(0, startIndex))

          withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
            append(code.substring(startIndex, startIndex + query.length))
          }

          append(code.substring(startIndex + query.length))
        } else {
          append(code)
        }
      }

      Text(
        text = annotatedName,
        modifier = Modifier
          .padding(start = 16.dp)
          .weight(1f),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = annotatedCode,
        modifier = Modifier.padding(start = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
private fun SearchBar(
  text: String,
  modifier: Modifier = Modifier,
  hint: String = stringResource(R.string.CountryCodeSelectScreen__search_by),
  onSearch: (String) -> Unit = {}
) {
  val focusRequester = remember { FocusRequester() }
  var showKeyboard by remember { mutableStateOf(false) }

  TextField(
    value = text,
    onValueChange = { onSearch(it) },
    placeholder = { Text(hint) },
    trailingIcon = {
      if (text.isNotEmpty()) {
        IconButton(onClick = { onSearch("") }) {
          Icon(
            imageVector = SignalIcons.X.imageVector,
            contentDescription = null
          )
        }
      } else {
        IconButton(onClick = {
          showKeyboard = !showKeyboard
          focusRequester.requestFocus()
        }) {
          if (showKeyboard) {
            Icon(
              imageVector = SignalIcons.Keyboard.imageVector,
              contentDescription = null
            )
          } else {
            Icon(
              imageVector = ImageVector.vectorResource(R.drawable.symbol_number_pad_24),
              contentDescription = null
            )
          }
        }
      }
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = if (showKeyboard) {
        KeyboardType.Number
      } else {
        KeyboardType.Text
      }
    ),
    shape = RoundedCornerShape(28.dp),
    modifier = modifier
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 16.dp)
      .padding(horizontal = 16.dp)
      .fillMaxWidth()
      .defaultMinSize(minHeight = 56.dp)
      .focusRequester(focusRequester),
    visualTransformation = VisualTransformation.None,
    colors = TextFieldDefaults.colors(
      // TODO move to SignalTheme
      focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent
    )
  )
}

@AllDevicePreviews
@Composable
private fun ScreenPreview() {
  Previews.Preview {
    CountryCodePickerScreen(
      state = CountryCodeState(
        countryList = mutableListOf(
          Country("\uD83C\uDDFA\uD83C\uDDF8", "United States", 1, "US"),
          Country("\uD83C\uDDE8\uD83C\uDDE6", "Canada", 2, "CA"),
          Country("\uD83C\uDDF2\uD83C\uDDFD", "Mexico", 3, "MX")
        ),
        commonCountryList = mutableListOf(
          Country("\uD83C\uDDFA\uD83C\uDDF8", "United States", 4, "US"),
          Country("\uD83C\uDDE8\uD83C\uDDE6", "Canada", 5, "CA")
        )
      ),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun LoadingScreenPreview() {
  Previews.Preview {
    CountryCodePickerScreen(
      state = CountryCodeState(
        countryList = emptyList()
      ),
      onEvent = {}
    )
  }
}

@LargeFontPreviews
@Composable
private fun LargeFontScreenPreview() {
  Previews.Preview {
    CountryCodePickerScreen(
      state = CountryCodeState(
        countryList = mutableListOf(
          Country("\uD83C\uDDFA\uD83C\uDDF8", "United States", 1, "US"),
          Country("\uD83C\uDDE8\uD83C\uDDE6", "Canada", 2, "CA"),
          Country("\uD83C\uDDF2\uD83C\uDDFD", "Mexico", 3, "MX")
        ),
        commonCountryList = mutableListOf(
          Country("\uD83C\uDDFA\uD83C\uDDF8", "United States", 4, "US"),
          Country("\uD83C\uDDE8\uD83C\uDDE6", "Canada", 5, "CA")
        )
      ),
      onEvent = {}
    )
  }
}
