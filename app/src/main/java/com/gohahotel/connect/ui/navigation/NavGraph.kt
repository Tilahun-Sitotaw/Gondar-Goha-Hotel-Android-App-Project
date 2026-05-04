package com.gohahotel.connect.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gohahotel.connect.ui.auth.LoginScreen
import com.gohahotel.connect.ui.auth.AuthViewModel
import com.gohahotel.connect.ui.concierge.ConciergeScreen
import com.gohahotel.connect.ui.food.CartScreen
import com.gohahotel.connect.ui.food.MenuScreen
import com.gohahotel.connect.ui.food.OrderTrackingScreen
import com.gohahotel.connect.ui.food.GuestOrdersScreen
import com.gohahotel.connect.ui.guide.CulturalGuideScreen
import com.gohahotel.connect.ui.guide.GuideDetailScreen
import com.gohahotel.connect.ui.home.HomeScreen
import com.gohahotel.connect.ui.qr.QrScannerScreen
import com.gohahotel.connect.ui.rooms.InRoomRequestScreen
import com.gohahotel.connect.ui.rooms.RoomDetailScreen
import com.gohahotel.connect.ui.rooms.RoomListScreen
import com.gohahotel.connect.ui.settings.SettingsScreen
import com.gohahotel.connect.ui.splash.SplashScreen
import com.gohahotel.connect.ui.admin.AdminDashboard
import com.gohahotel.connect.ui.admin.AdminRoomManagement
import com.gohahotel.connect.ui.admin.AdminPromotionsScreen
import com.gohahotel.connect.ui.admin.AdminMenuManagement
import com.gohahotel.connect.ui.admin.AdminOrdersScreen
import com.gohahotel.connect.ui.admin.AdminUsersScreen
import com.gohahotel.connect.ui.admin.AdminContentManagement
import com.gohahotel.connect.ui.staff.StaffDashboardScreen

@Composable
fun GohaNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        enterTransition  = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
        exitTransition   = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 } },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
        popExitTransition  = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 } }
    ) {
        // ── Splash ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToStaff = {
                    navController.navigate(Screen.StaffDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth ─────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        "ADMIN" -> {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "STAFF", "KITCHEN", "RECEPTION", "HOUSEKEEPING" -> {
                            navController.navigate(Screen.StaffDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        else -> { // GUEST
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRooms      = { navController.navigate(Screen.RoomList.route) },
                onNavigateToMenu       = { navController.navigate(Screen.Menu.route) },
                onNavigateToConcierge  = { navController.navigate(Screen.Concierge.route) },
                onNavigateToGuide      = { navController.navigate(Screen.CulturalGuide.route) },
                onNavigateToQr         = { navController.navigate(Screen.QrScanner.route) },
                onNavigateToSettings   = { navController.navigate(Screen.Settings.route) },
                onNavigateToAdmin      = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToTracking   = { id -> navController.navigate(Screen.OrderTracking.createRoute(id)) },
                onNavigateToMyOrders   = { navController.navigate(Screen.GuestOrders.route) }
            )
        }

        // ── Rooms ─────────────────────────────────────────────────────────────
        composable(Screen.RoomList.route) {
            RoomListScreen(
                onRoomClick = { roomId -> navController.navigate(Screen.RoomDetail.createRoute(roomId)) },
                onBack      = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.RoomDetail.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            RoomDetailScreen(
                roomId          = it.arguments?.getString("roomId") ?: "",
                onBack          = { navController.popBackStack() },
                onInRoomRequest = { navController.navigate(Screen.InRoomRequest.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.InRoomRequest.route) {
            InRoomRequestScreen(onBack = { navController.popBackStack() })
        }

        // ── Food ──────────────────────────────────────────────────────────────
        composable(Screen.Menu.route) {
            MenuScreen(
                onBack     = { navController.popBackStack() },
                onViewCart = { navController.navigate(Screen.Cart.route) }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                onBack          = { navController.popBackStack() },
                onOrderPlaced   = { orderId ->
                    navController.navigate(Screen.OrderTracking.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(
            route     = Screen.OrderTracking.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            OrderTrackingScreen(
                orderId = it.arguments?.getString("orderId") ?: "",
                onBack  = { navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }}
            )
        }
        composable(Screen.GuestOrders.route) {
            GuestOrdersScreen(
                onBack = { navController.popBackStack() },
                onNavigateToOrder = { id -> navController.navigate(Screen.OrderTracking.createRoute(id)) }
            )
        }

        // ── Concierge ─────────────────────────────────────────────────────────
        composable(Screen.Concierge.route) {
            ConciergeScreen(onBack = { navController.popBackStack() })
        }

        // ── Cultural Guide ────────────────────────────────────────────────────
        composable(Screen.CulturalGuide.route) {
            CulturalGuideScreen(
                onEntryClick = { entryId -> navController.navigate(Screen.GuideDetail.createRoute(entryId)) },
                onBack       = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.GuideDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) {
            GuideDetailScreen(
                entryId = it.arguments?.getString("entryId") ?: "",
                onBack  = { navController.popBackStack() }
            )
        }

        // ── QR Scanner ────────────────────────────────────────────────────────
        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                onBack        = { navController.popBackStack() },
                onQrDecoded   = { route -> navController.navigate(route) }
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack            = { navController.popBackStack() },
                onLogout          = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToStaff = { navController.navigate(Screen.StaffDashboard.route) }
            )
        }

        // ── Admin ─────────────────────────────────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onNavigateToRooms      = { navController.navigate(Screen.AdminRooms.route) },
                onNavigateToMenu       = { navController.navigate(Screen.AdminMenu.route) },
                onNavigateToOrders     = { navController.navigate(Screen.AdminOrders.route) },
                onNavigateToUsers      = { navController.navigate(Screen.AdminUsers.route) },
                onNavigateToPromotions = { navController.navigate(Screen.AdminPromotions.route) },
                onNavigateToContent    = { navController.navigate(Screen.AdminContent.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.AdminRooms.route) {
            AdminRoomManagement(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminPromotions.route) {
            AdminPromotionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminMenu.route) {
            AdminMenuManagement(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminUsers.route) {
            AdminUsersScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.AdminContent.route) {
            AdminContentManagement(onBack = { navController.popBackStack() })
        }
        
        // ── Staff ─────────────────────────────────────────────────────────────
        composable(Screen.StaffDashboard.route) {
            StaffDashboardScreen(onBack = { navController.popBackStack() })
        }
    }
}
