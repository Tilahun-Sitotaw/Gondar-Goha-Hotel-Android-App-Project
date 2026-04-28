package com.gohahotel.connect.ui.loyalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.LoyaltyRepository
import com.gohahotel.connect.domain.model.LoyaltyInfo
import com.gohahotel.connect.domain.model.Reward
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoyaltyUiState(
    val info: LoyaltyInfo = LoyaltyInfo(),
    val availableRewards: List<Reward> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    private val repository: LoyaltyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoyaltyUiState())
    val uiState = _uiState.asStateFlow()

    fun loadLoyaltyData(guestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val info = repository.getLoyaltyInfo(guestId)
            val rewards = repository.getAvailableRewards()
            _uiState.update { it.copy(info = info, availableRewards = rewards, isLoading = false) }
        }
    }

    fun redeemReward(reward: Reward) {
        // Implementation for redemption logic
    }
}
