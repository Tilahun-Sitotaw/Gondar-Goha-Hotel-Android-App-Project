package com.gohahotel.connect.data.repository

import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.Booking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

enum class PaymentMethod {
    TELE_BIRR, CBE_BIRR, CREDIT_CARD, CASH_AT_HOTEL
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val error: String? = null
)

@Singleton
class PaymentRepository @Inject constructor() {

    suspend fun processPayment(
        amount: Double,
        currency: String,
        method: PaymentMethod,
        referenceId: String
    ): PaymentResult {
        // Simulate network latency for payment gateway
        delay(2000)
        
        // Mocking a successful response for now
        return PaymentResult(
            success = true,
            transactionId = "GOHA-${System.currentTimeMillis()}"
        )
    }

    fun getPaymentMethods(): List<PaymentMethod> = PaymentMethod.values().toList()
}
