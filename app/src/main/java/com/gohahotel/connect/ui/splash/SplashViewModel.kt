package com.gohahotel.connect.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.AuthRepository
import com.gohahotel.connect.data.repository.GuideRepository
import com.gohahotel.connect.data.remote.FirestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guideRepository: GuideRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow("GUEST")
    val userRole = _userRole.asStateFlow()

    init {
        viewModelScope.launch {
            // Check auth state immediately
            val signedIn = authRepository.isSignedIn()
            if (signedIn) {
                val uid = authRepository.currentUser?.uid
                if (uid != null) {
                    _userRole.value = firestoreService.getUserRole(uid)
                }
            }
            _isLoggedIn.value = signedIn

            // Initialize guide in the background after auth is handled
            launch {
                guideRepository.initializeGuide()
            }
        }
    }
}
