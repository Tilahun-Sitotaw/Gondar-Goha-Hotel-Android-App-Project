package com.gohahotel.connect.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash  : Screen("splash")
    object Login   : Screen("login")

    // Main
    object Home    : Screen("home")

    // Rooms
    object RoomList        : Screen("rooms")
    object RoomDetail      : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: String) = "room_detail/$roomId"
    }
    object InRoomRequest   : Screen("in_room_request")
    object MyReservations  : Screen("my_reservations")

    // Food
    object Menu          : Screen("menu")
    object Cart          : Screen("cart")
    object DishDetail    : Screen("dish_detail/{menuItemId}") {
        fun createRoute(menuItemId: String) = "dish_detail/$menuItemId"
    }
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object GuestOrders   : Screen("guest_orders")

    // Digital Concierge
    object Concierge : Screen("concierge")

    // Cultural Guide
    object CulturalGuide       : Screen("cultural_guide")
    object GuideDetail         : Screen("guide_detail/{entryId}") {
        fun createRoute(entryId: String) = "guide_detail/$entryId"
    }

    // QR Scanner
    object QrScanner : Screen("qr_scanner")

    // Settings
    object Settings : Screen("settings")

    // Admin
    object AdminDashboard  : Screen("admin_dashboard")
    object AdminRooms      : Screen("admin_rooms")
    object AdminMenu       : Screen("admin_menu")
    object AdminOrders     : Screen("admin_orders")
    object AdminUsers      : Screen("admin_users")
    object AdminGuestDetail : Screen("admin_guest_detail/{userId}") {
        fun createRoute(userId: String) = "admin_guest_detail/$userId"
    }
    object AdminPromotions : Screen("admin_promotions")
    object AdminContent    : Screen("admin_content")
    object AdminChat       : Screen("admin_chat")

    // Chat
    object GuestChat : Screen("guest_chat")

    // Events
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    // Staff
    object StaffDashboard  : Screen("staff_dashboard")
}
