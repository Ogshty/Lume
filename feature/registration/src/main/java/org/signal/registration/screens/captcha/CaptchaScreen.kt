/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.registration.screens.captcha

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.signal.core.ui.compose.AllDevicePreviews
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews

/**
 * Screen to display a captcha verification using a WebView.
 * The WebView loads the Signal captcha URL and intercepts the callback
 * when the user completes the captcha.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaScreen(
  state: CaptchaState,
  onEvent: (CaptchaScreenEvents) -> Unit,
  modifier: Modifier = Modifier
) {
  var loadState by remember { mutableStateOf(state.loadState) }
  val animationState = remember {
    MutableTransitionState(false).apply {
      targetState = true
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp)
  ) {
    Spacer(modifier = Modifier.height(32.dp))

    Text(
      text = "Verification",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      ),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    AnimatedVisibility(
      visibleState = animationState,
      enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
        scaleIn(initialScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessLow)),
      modifier = Modifier.weight(1f)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(28.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(28.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          AndroidView(
            factory = { context ->
              WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                clearCache(true)

                webViewClient = object : WebViewClient() {
                  @Deprecated("Deprecated in Java")
                  override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith(state.captchaScheme)) {
                      val token = url.substring(state.captchaScheme.length)
                      onEvent(CaptchaScreenEvents.CaptchaCompleted(token))
                      return true
                    }
                    return false
                  }

                  override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loadState = CaptchaLoadState.Loaded
                  }

                  override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                  ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    loadState = CaptchaLoadState.Error
                  }
                }

                loadUrl(state.captchaUrl)
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          if (loadState != CaptchaLoadState.Loaded) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              if (loadState == CaptchaLoadState.Loading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(48.dp),
                  color = MaterialTheme.colorScheme.primary,
                  strokeWidth = 4.dp
                )
              } else if (loadState == CaptchaLoadState.Error) {
                Text(
                  text = "Failed to load captcha",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.error
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Buttons.LargeTonal(
      onClick = { onEvent(CaptchaScreenEvents.Cancel) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 32.dp),
      shape = RoundedCornerShape(28.dp),
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Text(
        text = "Cancel",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }
  }
}

@AllDevicePreviews
@Composable
private fun CaptchaScreenLoadingPreview() {
  Previews.Preview {
    CaptchaScreen(
      state = CaptchaState(
        captchaUrl = "https://example.com/captcha",
        loadState = CaptchaLoadState.Loading
      ),
      onEvent = {}
    )
  }
}

@AllDevicePreviews
@Composable
private fun CaptchaScreenErrorPreview() {
  Previews.Preview {
    CaptchaScreen(
      state = CaptchaState(
        captchaUrl = "https://example.com/captcha",
        loadState = CaptchaLoadState.Error
      ),
      onEvent = {}
    )
  }
}
