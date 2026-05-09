package com.gohahotel.connect.data.repository

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

enum class PaymentMethod(
    val displayName: String,
    val emoji: String,
    val category: PaymentCategory,
    val description: String = ""
) {
    // ── Ethiopian Mobile Wallets ──────────────────────────────────────────────
    TELE_BIRR(    "TeleBirr",          "📱", PaymentCategory.ETHIOPIAN_MOBILE,  "Ethio Telecom wallet"),
    CBE_BIRR(     "CBE Birr",          "🏦", PaymentCategory.ETHIOPIAN_MOBILE,  "Commercial Bank of Ethiopia"),
    AMOLE(        "Amole",             "💳", PaymentCategory.ETHIOPIAN_MOBILE,  "Dashen Bank digital wallet"),
    HELLO_CASH(   "HelloCash",         "📲", PaymentCategory.ETHIOPIAN_MOBILE,  "Amhara Bank mobile money"),
    M_BIRR(       "M-Birr",            "📱", PaymentCategory.ETHIOPIAN_MOBILE,  "MOSS ICT mobile wallet"),

    // ── Ethiopian Banks ───────────────────────────────────────────────────────
    CBE(          "CBE Online",        "🏛️", PaymentCategory.ETHIOPIAN_BANK,   "Commercial Bank of Ethiopia"),
    AWASH(        "Awash Bank",        "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Awash International Bank"),
    DASHEN(       "Dashen Bank",       "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Dashen Bank S.C."),
    ABYSSINIA(    "Bank of Abyssinia", "🏛️", PaymentCategory.ETHIOPIAN_BANK,   "Bank of Abyssinia"),
    WEGAGEN(      "Wegagen Bank",      "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Wegagen Bank S.C."),
    UNITED(       "United Bank",       "🏦", PaymentCategory.ETHIOPIAN_BANK,   "United Bank S.C."),
    NIBE(         "NIB Bank",          "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Nib International Bank"),
    BERHAN(       "Berhan Bank",       "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Berhan Bank S.C."),
    ABAY(         "Abay Bank",         "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Abay Bank S.C."),
    LION(         "Lion Bank",         "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Lion International Bank"),
    OROMIA(       "Oromia Bank",       "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Cooperative Bank of Oromia"),
    ZEMEN(        "Zemen Bank",        "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Zemen Bank S.C."),
    BUNNA(        "Bunna Bank",        "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Bunna International Bank"),
    ENAT(         "Enat Bank",         "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Enat Bank S.C."),
    AMHARA(       "Amhara Bank",       "🏦", PaymentCategory.ETHIOPIAN_BANK,   "Amhara Bank S.C."),

    // ── International Cards ───────────────────────────────────────────────────
    VISA(         "Visa Card",         "💳", PaymentCategory.INTERNATIONAL,    "Visa credit / debit card"),
    MASTERCARD(   "Mastercard",        "💳", PaymentCategory.INTERNATIONAL,    "Mastercard credit / debit"),
    AMEX(         "American Express",  "💳", PaymentCategory.INTERNATIONAL,    "Amex credit card"),

    // ── International Digital ─────────────────────────────────────────────────
    PAYPAL(       "PayPal",            "🌐", PaymentCategory.INTERNATIONAL,    "PayPal online payment"),
    APPLE_PAY(    "Apple Pay",         "🍎", PaymentCategory.INTERNATIONAL,    "Apple Pay"),
    GOOGLE_PAY(   "Google Pay",        "🔵", PaymentCategory.INTERNATIONAL,    "Google Pay"),

    // ── Cash ──────────────────────────────────────────────────────────────────
    CASH_AT_HOTEL("Pay at Hotel",      "💵", PaymentCategory.CASH,             "Pay cash at reception")
}

enum class PaymentCategory(val label: String) {
    ETHIOPIAN_MOBILE("Ethiopian Mobile Wallets"),
    ETHIOPIAN_BANK("Ethiopian Banks"),
    INTERNATIONAL("International Payment"),
    CASH("Cash")
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val error: String? = null,
    val method: PaymentMethod? = null
)

@Singleton
class PaymentRepository @Inject constructor() {

    suspend fun processPayment(
        amount: Double,
        currency: String,
        method: PaymentMethod,
        referenceId: String
    ): PaymentResult {
        // Simulate realistic processing time per method type
        val delay = when (method.category) {
            PaymentCategory.ETHIOPIAN_MOBILE -> 2500L
            PaymentCategory.ETHIOPIAN_BANK   -> 3000L
            PaymentCategory.INTERNATIONAL    -> 2000L
            PaymentCategory.CASH             -> 500L
        }
        delay(delay)

        return PaymentResult(
            success       = true,
            transactionId = "GOHA-${method.name.take(4)}-${System.currentTimeMillis()}",
            method        = method
        )
    }

    fun getPaymentMethods(): List<PaymentMethod> = PaymentMethod.entries
}
