package com.sdd.marketplace.feature.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sdd.marketplace.core.ui.components.*
import com.sdd.marketplace.core.ui.theme.SddLightPink
import com.sdd.marketplace.core.ui.theme.SddPink
import com.sdd.marketplace.feature.auth.viewmodel.AuthEvent
import com.sdd.marketplace.feature.auth.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> onNavigateToHome()
                else -> {}
            }
        }
    }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(SddLightPink)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            SddLogo()
            Spacer(Modifier.height(16.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Join Sdd and start shopping", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp)) {
                    SddTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name",
                        leadingIcon = { Icon(Icons.Outlined.Person, "Name") })
                    Spacer(Modifier.height(12.dp))
                    SddTextField(value = email, onValueChange = { email = it }, label = "Email Address",
                        leadingIcon = { Icon(Icons.Outlined.Email, "Email") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email))
                    Spacer(Modifier.height(12.dp))
                    SddTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number",
                        leadingIcon = { Icon(Icons.Outlined.Phone, "Phone") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone))
                    Spacer(Modifier.height(12.dp))
                    SddTextField(value = password, onValueChange = { password = it }, label = "Password",
                        isPassword = true, leadingIcon = { Icon(Icons.Outlined.Lock, "Password") })
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = SddPink))
                        Text("I agree to the ")
                        Text("Terms & Conditions", color = SddPink, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(16.dp))
                    SddButton(
                        text = "Register",
                        onClick = { viewModel.signUp(fullName, email, phone, password) },
                        isLoading = uiState.isLoading,
                        enabled = agreedToTerms && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                    )
                    uiState.error?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("Already have an account? ")
                Text("Login", color = SddPink, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onNavigateToLogin))
            }
            Spacer(Modifier.height(12.dp))
            Text("Continue as Guest", color = SddPink, modifier = Modifier.clickable { viewModel.continueAsGuest() })
            Spacer(Modifier.height(24.dp))
        }
    }
}
