package com.gohahotel.connect.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*
import com.gohahotel.connect.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess(uiState.userRole)
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            uiState = uiState,
            onDismiss = { showForgotPasswordDialog = false },
            onResetPasswordRequest = { viewModel.resetPassword(it) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealDark, SurfaceDark, Color.Black)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            // Professional Compact Logo
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = GoldPrimary,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", color = SurfaceDark, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("GOHA HOTEL", style = MaterialTheme.typography.titleMedium,
                color = GoldPrimary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

            Spacer(Modifier.height(16.dp))

            // Main Auth Card - Ultra Compact
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.95f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (isRegisterMode) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(12.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldLight.copy(alpha = 0.2f),
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = GoldLight.copy(alpha = 0.5f),
                        focusedLeadingIconColor = GoldPrimary,
                        unfocusedLeadingIconColor = GoldLight.copy(alpha = 0.4f),
                        cursorColor = GoldPrimary
                    )

                    // Registration Specific Fields
                    AnimatedVisibility(isRegisterMode) {
                        Column {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Full Name", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = textFieldColors
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = textFieldColors,
                        enabled = !uiState.isOtpSent // Lock email once OTP is sent
                    )

                    Spacer(Modifier.height(6.dp))

                    AnimatedVisibility(isRegisterMode) {
                        Column {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = textFieldColors
                            )
                            Spacer(Modifier.height(6.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Physical Address", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = textFieldColors
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = GoldLight.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    if (!isRegisterMode) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { showForgotPasswordDialog = true }, contentPadding = PaddingValues(0.dp)) {
                                Text("Forgot Password?", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    AnimatedVisibility(isRegisterMode) {
                        Column {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.LockClock, null, modifier = Modifier.size(18.dp)) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = textFieldColors
                            )
                        }
                    }

                    AnimatedVisibility(isRegisterMode && uiState.isOtpSent) {
                        Column {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("Enter OTP Code Here", fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, null, tint = GoldPrimary, modifier = Modifier.size(20.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = GoldPrimary.copy(alpha = 0.5f),
                                    focusedContainerColor = GoldPrimary.copy(alpha = 0.1f),
                                    unfocusedContainerColor = GoldPrimary.copy(alpha = 0.1f)
                                )
                            )
                            
                            // Resend OTP Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.startRegistration(email.trim(), displayName.trim()) },
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Didn't receive code? Resend", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    if (uiState.message != null) {
                        Text(
                            uiState.message!!,
                            color = SuccessGreen,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Buttons Area
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                if (!uiState.isOtpSent) {
                                    viewModel.startRegistration(email.trim(), displayName.trim())
                                } else {
                                    viewModel.register(email.trim(), password, confirmPassword, displayName.trim(), phoneNumber.trim(), address.trim(), otpCode.trim())
                                }
                            } else {
                                viewModel.login(email.trim(), password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        enabled = !uiState.isLoading && (
                            !isRegisterMode || (!uiState.isOtpSent) || (uiState.isOtpSent && otpCode.isNotBlank())
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = SurfaceDark, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (isRegisterMode) {
                                    if (!uiState.isOtpSent) "CONTINUE TO VERIFY" else "VERIFY & CREATE"
                                } else "LOGIN",
                                color = SurfaceDark, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Google Login
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(context.getString(R.string.default_web_client_id))
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(context, request)
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                    val googleCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                    
                                    viewModel.signInWithGoogle(googleCredential)
                                } catch (e: Exception) {
                                    // Handle or log error
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = com.gohahotel.connect.R.drawable.ic_google),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Google Sign In", color = Color(0xFF1F1F1F), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Continue as Guest Button
                    OutlinedButton(
                        onClick = { viewModel.signInAsGuest() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text("CONTINUE AS GUEST", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = { isRegisterMode = !isRegisterMode },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isRegisterMode) "Already have an account? Sign In" else "New to Goha? Create Account",
                            color = GoldLight.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "High Above the Historic City of Gondar",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldLight.copy(alpha = 0.5f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    repeat(5) { Icon(Icons.Default.Star, null, tint = GoldPrimary, modifier = Modifier.size(10.dp)) }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    uiState: AuthUiState,
    onDismiss: () -> Unit,
    onResetPasswordRequest: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter your registered email address and we will send you a secure link to reset your password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDark.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = GoldPrimary.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldLight.copy(alpha = 0.2f),
                        focusedContainerColor = GoldPrimary.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(16.dp))

                if (uiState.error != null) {
                    Text(uiState.error, color = Color.Red, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.message != null) {
                    Text(uiState.message, color = SuccessGreen, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { onResetPasswordRequest(email.trim()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceDark, strokeWidth = 2.dp)
                    } else {
                        Text("Send Reset Link", color = SurfaceDark, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.message != null) "Close" else "Cancel", color = GoldLight)
                }
            }
        }
    }
}
