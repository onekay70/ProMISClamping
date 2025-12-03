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

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground),   // light grey from your theme
        contentAlignment = Alignment.Center
    ) {
        // 🔶 TOP LOGO (your provided image)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 88.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.landing_page_title_h_s),
                contentDescription = "ProMIS Logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)  // 85% width, adjust if needed
                    .padding(horizontal = 16.dp)
            )
        }

        // 🔵 LOGIN CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
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
                        .offset(y = (-28).dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_promis_home),
                                contentDescription = "ProMIS",
                                tint = NavyHeader,
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
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
                            onDone = {
                                focusManager.clearFocus()
                                if (!isLoading) onLoginClick()
                            }
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
                        onClick = {
                            focusManager.clearFocus()
                            onLoginClick()
                        },
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

        // 🟢 Footer at the very bottom
        PromisFooter(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
