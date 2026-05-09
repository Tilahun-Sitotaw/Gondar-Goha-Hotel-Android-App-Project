package com.gohahotel.connect.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.PaymentMethod
import com.gohahotel.connect.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val selectedMethod: PaymentMethod = PaymentMethod.TELE_BIRR,
    val isProcessing: Boolean = false,
    val paymentSuccess: Boolean = false,
    val transactionId: String? = null,
    val pendingAmount: Double = 0.0,
    val pendingCurrency: String = "ETB",
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState = _uiState.asStateFlow()

    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun processPayment(amount: Double, referenceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null, pendingAmount = amount) }
            val result = paymentRepository.processPayment(
                amount      = amount,
                currency    = "ETB",
                method      = _uiState.value.selectedMethod,
                referenceId = referenceId
            )
            if (result.success) {
                _uiState.update { it.copy(
                    isProcessing  = false,
                    paymentSuccess = true,
                    transactionId = result.transactionId
                ) }
            } else {
                _uiState.update { it.copy(isProcessing = false, error = result.error) }
            }
        }
    }
    
    fun resetState() {
        _uiState.update { PaymentUiState() }
    }
}
