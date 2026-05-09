package com.gohahotel.connect.data.remote

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class EmailService @Inject constructor() {

    private val adminEmail = "gohahotel34@gmail.com"
    private val appPassword = "uqowibvcigupdnuh" // User provided app password

    suspend fun sendOtpEmail(recipientEmail: String, otp: String, displayName: String = "Guest"): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(adminEmail, appPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(adminEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "Welcome to Goha Hotel - Your Verification Code"
                setContent("""
                    <div style="font-family: Arial, sans-serif; color: #333;">
                        <h2 style="color: #D4AF37;">Welcome to Goha Hotel, $displayName!</h2>
                        <p>Thank you for registering with us.</p>
                        <p>To complete your registration, please use the following verification code:</p>
                        <h3 style="background-color: #f4f4f4; padding: 10px; display: inline-block; letter-spacing: 2px;">$otp</h3>
                        <p>If you did not request this, please ignore this email.</p>
                        <br/>
                        <p>Best Regards,<br/>Goha Hotel Team</p>
                    </div>
                """.trimIndent(), "text/html; charset=utf-8")
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendBookingConfirmation(
        recipientEmail: String,
        guestName: String,
        roomName: String,
        roomType: String,
        checkIn: String,
        checkOut: String,
        nights: Int,
        guests: Int,
        totalPrice: Double,
        currency: String,
        referenceId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
            }
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(adminEmail, appPassword)
            })
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(adminEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                // Also notify admin
                addRecipient(Message.RecipientType.BCC, InternetAddress(adminEmail))
                subject = "Booking Confirmed – $roomName | Goha Hotel"
                setContent("""
                    <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#0A1424;color:#FDFBF7;padding:32px;border-radius:16px;">
                      <div style="text-align:center;margin-bottom:24px;">
                        <div style="background:#D4A843;display:inline-block;padding:12px 24px;border-radius:8px;">
                          <span style="color:#050D18;font-size:22px;font-weight:900;letter-spacing:3px;">GOHA HOTEL</span>
                        </div>
                        <p style="color:#D4A843;letter-spacing:2px;margin-top:6px;">GONDAR · ETHIOPIA</p>
                      </div>
                      <h2 style="color:#D4A843;text-align:center;">🎉 Booking Confirmed!</h2>
                      <p style="text-align:center;color:#FDFBF7cc;">Dear <strong>$guestName</strong>, your reservation is confirmed.</p>
                      <div style="background:#0E1B2A;border:1px solid #D4A84340;border-radius:12px;padding:20px;margin:20px 0;">
                        <table style="width:100%;border-collapse:collapse;">
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Confirmation #</td><td style="padding:8px 0;font-weight:bold;color:#D4A843;">$referenceId</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Room</td><td style="padding:8px 0;font-weight:bold;">$roomName</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Type</td><td style="padding:8px 0;">$roomType</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Check-In</td><td style="padding:8px 0;">$checkIn</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Check-Out</td><td style="padding:8px 0;">$checkOut</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Duration</td><td style="padding:8px 0;">$nights night${if (nights > 1) "s" else ""}</td></tr>
                          <tr><td style="padding:8px 0;color:#FDFBF799;">Guests</td><td style="padding:8px 0;">$guests</td></tr>
                          <tr style="border-top:1px solid #D4A84330;"><td style="padding:12px 0;color:#D4A843;font-weight:bold;">Total Paid</td><td style="padding:12px 0;color:#D4A843;font-weight:bold;font-size:18px;">$currency ${totalPrice.toInt()}</td></tr>
                        </table>
                      </div>
                      <p style="color:#FDFBF799;font-size:13px;text-align:center;">For assistance, contact us at $adminEmail</p>
                      <p style="color:#D4A84366;font-size:11px;text-align:center;margin-top:16px;">© Goha Hotel · High Above the Historic City of Gondar</p>
                    </div>
                """.trimIndent(), "text/html; charset=utf-8")
            }
            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetOtp(recipientEmail: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(adminEmail, appPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(adminEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "Goha Hotel - Password Reset Code"
                setText("Your password reset code is: $otp\n\nIf you did not request this, please ignore this email.")
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
