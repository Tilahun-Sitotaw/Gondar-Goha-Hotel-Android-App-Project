package com.gohahotel.connect.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val uiState      by viewModel.uiState.collectAsState()
    val context      = androidx.compose.ui.platform.LocalContext.current
    val scope        = rememberCoroutineScope()
    val credManager  = remember { CredentialManager.create(context) }

    // ── Form state ────────────────────────────────────────────────────────────
    var email                   by remember { mutableStateOf("") }
    var password                by remember { mutableStateOf("") }
    var confirmPassword         by remember { mutableStateOf("") }
    var passwordVisible         by remember { mutableStateOf(false) }
    var confirmPasswordVisible  by remember { mutableStateOf(false) }
    var isRegisterMode          by remember { mutableStateOf(false) }
    var displayName             by remember { mutableStateOf("") }
    var phoneNumber             by remember { mutableStateOf("") }
    var address                 by remember { mutableStateOf("") }
    var idDocumentUri           by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedIdType          by remember { mutableStateOf("Passport") }
    var idTypeExpanded          by remember { mutableStateOf(false) }
    var showForgotDialog        by remember { mutableStateOf(false) }

    // Reset everything when screen first appears (handles logout → re-login)
    LaunchedEffect(Unit) {
        viewModel.resetState()
        email = ""; password = ""; confirmPassword = ""
        displayName = ""; phoneNumber = ""; address = ""
        idDocumentUri = null
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess(uiState.userRole)
    }

    // ── Email OTP verification step — shown after registration ────────────────
    if (uiState.isOtpSent) {
        OtpVerificationScreen(
            email      = email,
            uiState    = uiState,
            onResend   = { viewModel.resendOtp() },
            onVerifyOtp = { otp -> viewModel.verifyOtpAndRegister(otp) },
            onBack     = { viewModel.resetState() }
        )
        return
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            uiState   = uiState,
            onDismiss = { showForgotDialog = false },
            onResetPasswordRequest = { viewModel.resetPassword(it) }
        )
    }

    // ── ID picker launcher ────────────────────────────────────────────────────
    val idLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) idDocumentUri = uri }

    // ── Shared field colors ───────────────────────────────────────────────────
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = OnSurfaceDark,
        unfocusedTextColor      = OnSurfaceDark.copy(0.9f),
        focusedBorderColor      = GoldPrimary,
        unfocusedBorderColor    = Color.White.copy(0.12f),
        focusedLabelColor       = GoldPrimary,
        unfocusedLabelColor     = OnSurfaceDark.copy(0.4f),
        focusedLeadingIconColor    = GoldPrimary,
        unfocusedLeadingIconColor  = GoldPrimary.copy(0.4f),
        focusedTrailingIconColor   = GoldPrimary.copy(0.7f),
        unfocusedTrailingIconColor = OnSurfaceDark.copy(0.4f),
        cursorColor             = GoldPrimary,
        focusedContainerColor   = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF071520), Color(0xFF050D18), Color.Black)))
    ) {
        // Ambient glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(GoldPrimary.copy(0.06f), Color.Transparent), radius = 1000f)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Brush.linearGradient(listOf(GoldLight, GoldPrimary)), RoundedCornerShape(20.dp))
                    .border(1.dp, GoldLight.copy(0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = Color(0xFF050D18), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(14.dp))
            Text("GOHA HOTEL", style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Text("GONDAR · ETHIOPIA", style = MaterialTheme.typography.labelSmall,
                color = GoldLight.copy(0.5f), letterSpacing = 3.sp)

            Spacer(Modifier.height(28.dp))

            // ── Tab switcher ──────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.06f))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("Sign In" to false, "Register" to true).forEach { (label, isReg) ->
                        val selected = isRegisterMode == isReg
                        Surface(
                            onClick = { isRegisterMode = isReg },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) GoldPrimary else Color.Transparent
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color(0xFF050D18) else OnSurfaceDark.copy(0.5f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Form card ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.06f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ── Register-only: Full Name ──────────────────────────────
                    AnimatedVisibility(isRegisterMode,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = fieldColors
                        )
                    }

                    // ── Email ─────────────────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors
                    )

                    // ── Register-only: Phone + Address ────────────────────────
                    AnimatedVisibility(isRegisterMode,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = fieldColors
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Physical Address") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = fieldColors
                            )
                        }
                    }

                    // ── Password ──────────────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )

                    // ── Forgot password (sign-in only) ────────────────────────
                    AnimatedVisibility(!isRegisterMode) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(
                                onClick = { showForgotDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Forgot Password?",
                                    color = GoldLight.copy(0.7f),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // ── Confirm Password (register only) ──────────────────────
                    AnimatedVisibility(isRegisterMode,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                                      else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) "Hide" else "Show",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = fieldColors
                        )
                    }

                    // ── ID Document (register only) ───────────────────────────
                    AnimatedVisibility(isRegisterMode,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = Color.White.copy(0.06f))

                            Text("Identity Verification",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary.copy(0.9f),
                                fontWeight = FontWeight.Bold)
                            Text("Upload one of the following (required for first registration):",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.45f))

                            // ID type dropdown
                            val idTypes = listOf("Passport", "National ID", "Driver's License", "Kebele ID")
                            Box {
                                Surface(
                                    onClick = { idTypeExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Badge, null,
                                            tint = GoldPrimary.copy(0.7f), modifier = Modifier.size(18.dp))
                                        Text(selectedIdType, modifier = Modifier.weight(1f),
                                            color = OnSurfaceDark, style = MaterialTheme.typography.bodyMedium)
                                        Icon(Icons.Default.ArrowDropDown, null, tint = OnSurfaceDark.copy(0.5f))
                                    }
                                }
                                DropdownMenu(
                                    expanded = idTypeExpanded,
                                    onDismissRequest = { idTypeExpanded = false },
                                    modifier = Modifier
                                        .background(Color(0xFF0E1B2A))
                                        .border(1.dp, GoldPrimary.copy(0.2f), RoundedCornerShape(8.dp))
                                ) {
                                    idTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type, color = if (type == selectedIdType) GoldPrimary else OnSurfaceDark) },
                                            onClick = { selectedIdType = type; idTypeExpanded = false }
                                        )
                                    }
                                }
                            }

                            // Upload button
                            Surface(
                                onClick = { idLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                color = if (idDocumentUri != null) SuccessGreen.copy(0.08f) else GoldPrimary.copy(0.05f),
                                border = BorderStroke(1.dp,
                                    if (idDocumentUri != null) SuccessGreen.copy(0.4f) else GoldPrimary.copy(0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        if (idDocumentUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                                        null,
                                        tint = if (idDocumentUri != null) SuccessGreen else GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            if (idDocumentUri != null) "Document uploaded ✓" else "Upload $selectedIdType",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (idDocumentUri != null) SuccessGreen else GoldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            if (idDocumentUri != null) "Tap to change" else "Tap to select image",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnSurfaceDark.copy(0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Error banner ──────────────────────────────────────────
                    uiState.error?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRed.copy(0.1f),
                            border = BorderStroke(1.dp, ErrorRed.copy(0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.ErrorOutline, null,
                                    tint = ErrorRed, modifier = Modifier.size(16.dp))
                                Text(err, color = ErrorRed,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // ── Success banner ────────────────────────────────────────
                    uiState.message?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(0.1f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Text(msg, color = SuccessGreen,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Primary action button ─────────────────────────────────────────
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(
                            email          = email.trim(),
                            password       = password,
                            confirmPassword = confirmPassword,
                            displayName    = displayName.trim(),
                            phoneNumber    = phoneNumber.trim(),
                            address        = address.trim(),
                            idDocumentUri  = idDocumentUri,
                            idDocumentType = selectedIdType
                        )
                    } else {
                        viewModel.login(email.trim(), password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF050D18),
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (isRegisterMode) "Create Account" else "Sign In",
                        color = Color(0xFF050D18),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Google Sign-In ────────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credManager.getCredential(context, request)
                            val tokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                            val googleCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                            viewModel.signInWithGoogle(googleCredential)
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Icon(painterResource(R.drawable.ic_google), null,
                    tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Continue with Google", color = Color(0xFF1F1F1F),
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(10.dp))

            // ── Continue as Guest ─────────────────────────────────────────────
            OutlinedButton(
                onClick = { viewModel.signInAsGuest() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = GoldPrimary.copy(0.05f))
            ) {
                Icon(Icons.Default.PersonOutline, null,
                    tint = GoldPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Continue as Guest", color = GoldPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Guests can browse but must sign in to book rooms or order food",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDark.copy(0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))
            Row { repeat(5) { Icon(Icons.Default.Star, null, tint = GoldPrimary.copy(0.4f), modifier = Modifier.size(10.dp)) } }
            Spacer(Modifier.height(4.dp))
            Text("High Above the Historic City of Gondar",
                style = MaterialTheme.typography.labelSmall,
                color = GoldLight.copy(0.3f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Forgot Password Dialog ────────────────────────────────────────────────────
@Composable
fun ForgotPasswordDialog(
    uiState: AuthUiState,
    onDismiss: () -> Unit,
    onResetPasswordRequest: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0E1B2A),
            border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(52.dp).background(GoldPrimary.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = GoldPrimary, modifier = Modifier.size(26.dp))
                }
                Text("Reset Password", style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary, fontWeight = FontWeight.Bold)
                Text("Enter your email and we'll send a reset link.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDark.copy(0.6f), textAlign = TextAlign.Center)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = GoldPrimary.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = OnSurfaceDark,
                        unfocusedTextColor      = OnSurfaceDark,
                        focusedBorderColor      = GoldPrimary,
                        unfocusedBorderColor    = Color.White.copy(0.1f),
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                uiState.error?.let {
                    Text(it, color = ErrorRed, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }
                uiState.message?.let {
                    Text(it, color = SuccessGreen, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }

                Button(
                    onClick = { onResetPasswordRequest(email.trim()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp),
                            color = Color(0xFF050D18), strokeWidth = 2.dp)
                    } else {
                        Text("Send Reset Link", color = Color(0xFF050D18), fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.message != null) "Close" else "Cancel", color = GoldLight.copy(0.7f))
                }
            }
        }
    }
}
