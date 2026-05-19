package com.example.promisclamping.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.promisclamping.R
import com.example.promisclamping.ui.common.PromisFooter

import com.example.promisclamping.ui.theme.NavyHeader
import com.example.promisclamping.ui.theme.PageBackground
import com.example.promisclamping.ui.theme.SecondaryBlue
import com.example.promisclamping.ui.theme.TextDark
import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.ContentScale
import com.example.promisclamping.BuildConfig

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var username by remember {
        mutableStateOf(
            if (BuildConfig.DEBUG) "971103025908" else ""
        )
    }

    var password by remember {
        mutableStateOf(
            if (BuildConfig.DEBUG) "Promis@112233" else ""
        )
    }

    DecoratedLoginContent(
        username = username,
        password = password,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onLoginClick = { onLogin(username, password) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoratedLoginContent(
    username: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {

        if (isLandscape) {
            // 🔹 Landscape: logo LEFT, card RIGHT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side – logo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.landing_page_title_h_s),
                        contentDescription = "ProMIS Logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)   // keeps the logo nice, adjust if needed
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Right side – login card
                LoginCard(
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    onLoginClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        } else {
            // 🔹 Portrait: logo on top, card below (your original style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(88.dp))

                Image(
                    painter = painterResource(id = R.drawable.landing_page_title_h_s),
                    contentDescription = "ProMIS Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(horizontal = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(84.dp))

                LoginCard(
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    onLoginClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp) // leave space above footer
                )
            }
        }

        // 🟢 footer stays at bottom in both orientations
        PromisFooter(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LoginCard(
    username: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {

            // 🔵 Top navy header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyHeader)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Datang!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sistem Pengurusan Maklumat Hartanah (ProMIS)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E7FF)
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            // ◯ White circle with logo, overlapping header
            Box(
                modifier = Modifier
                    .offset(y = (-28).dp, x = 28.dp)
                    .fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = NavyHeader
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(NavyHeader),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_promis_home),
                            contentDescription = "ProMIS",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // 📋 Form fields
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    "ID Pengguna",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("ID Pengguna…") }
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Kata Laluan",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Kata Laluan…") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onLoginClick() }
                    )
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(if (isLoading) "Memproses..." else "Log Masuk")
                }
            }
        }
    }
}
