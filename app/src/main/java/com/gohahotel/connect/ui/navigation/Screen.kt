package com.gohahotel.connect.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash  : Screen("splash")
    object Login   : Screen("login")

    // Main
    object Home    : Screen("home")

    // Rooms
    object RoomList   : Screen("rooms")
    object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: String) = "room_detail/$roomId"
    }
    object InRoomRequest : Screen("in_room_request")

    // Food
    object Menu          : Screen("menu")
    object Cart          : Screen("cart")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }

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
    object AdminPromotions : Screen("admin_promotions")
}
