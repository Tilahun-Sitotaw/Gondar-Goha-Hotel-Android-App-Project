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

    suspend fun sendPasswordResetOtp(recipientEmail: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
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
