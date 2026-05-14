package com.gohahotel.connect.domain.utils

import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.RoomAvailability
import com.gohahotel.connect.domain.model.RoomStatus
import java.text.SimpleDateFormat
import java.util.*

object AvailabilityCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Calculate room availability based on bookings
     */
    fun calculateRoomAvailability(
        roomId: String,
        roomName: String,
        bookings: List<Booking>
    ): RoomAvailability {
        val today = dateFormat.format(Date())
        val roomBookings = bookings.filter { it.roomId == roomId }
            .sortedBy { it.checkInDate }

        // Find active booking (current date is between check-in and check-out)
        val activeBooking = roomBookings.find { booking ->
            isDateInRange(today, booking.checkInDate, booking.checkOutDate)
        }

        // Find next booking
        val nextBooking = roomBookings.find { it.checkInDate > today }

        val status = when {
            activeBooking != null -> RoomStatus.OCCUPIED
            nextBooking != null -> RoomStatus.RESERVED
            else -> RoomStatus.FREE
        }

        // Calculate days until available
        val daysUntilAvailable = when {
            activeBooking != null -> calculateNights(today, activeBooking.checkOutDate)
            nextBooking != null -> calculateNights(today, nextBooking.checkInDate)
            else -> 0
        }

        return RoomAvailability(
            roomId = roomId,
            roomName = roomName,
            status = status,
            currentGuest = activeBooking?.guestName ?: "",
            checkInDate = activeBooking?.checkInDate ?: "",
            checkOutDate = activeBooking?.checkOutDate ?: "",
            nextAvailableDate = nextBooking?.checkInDate ?: "",
            occupancyPercentage = if (activeBooking != null) 100 else 0,
            daysUntilAvailable = daysUntilAvailable
        )
    }

    /**
     * Check if a room is available for given dates
     */
    fun isRoomAvailable(
        checkInDate: String,
        checkOutDate: String,
        bookings: List<Booking>
    ): Boolean {
        return bookings.none { booking ->
            // Check if there's any overlap
            val bookingCheckIn = booking.checkInDate
            val bookingCheckOut = booking.checkOutDate

            // Overlap occurs if: checkIn < bookingCheckOut AND checkOut > bookingCheckIn
            checkInDate < bookingCheckOut && checkOutDate > bookingCheckIn
        }
    }

    /**
     * Validate guest count against room capacity
     */
    fun validateGuestCount(guestCount: Int, roomCapacity: Int): ValidationResult {
        return when {
            guestCount <= 0 -> ValidationResult(
                isValid = false,
                message = "Guest count must be at least 1"
            )
            guestCount > roomCapacity -> ValidationResult(
                isValid = false,
                message = "Guest count ($guestCount) exceeds room capacity ($roomCapacity)"
            )
            else -> ValidationResult(
                isValid = true,
                message = "Valid guest count"
            )
        }
    }

    /**
     * Calculate number of nights between check-in and check-out
     */
    fun calculateNights(checkInDate: String, checkOutDate: String): Int {
        return try {
            val checkIn = dateFormat.parse(checkInDate) ?: return 1
            val checkOut = dateFormat.parse(checkOutDate) ?: return 1
            val diffMillis = checkOut.time - checkIn.time
            val nights = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            maxOf(1, nights)
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Get next available date for a room
     */
    fun getNextAvailableDate(bookings: List<Booking>): String {
        val today = dateFormat.format(Date())
        val sortedBookings = bookings.sortedBy { it.checkOutDate }

        return if (sortedBookings.isEmpty()) {
            today
        } else {
            sortedBookings.lastOrNull()?.checkOutDate ?: today
        }
    }

    /**
     * Check if date is within range (inclusive)
     */
    private fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
        return date >= startDate && date < endDate
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}
