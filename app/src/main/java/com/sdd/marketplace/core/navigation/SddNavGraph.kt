package com.sdd.marketplace.core.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.sdd.marketplace.core.ui.components.SddBottomNavBar
import com.sdd.marketplace.feature.auth.ui.screens.*
import com.sdd.marketplace.feature.chat.ui.screens.*
import com.sdd.marketplace.feature.followers.ui.FollowersScreen
import com.sdd.marketplace.feature.home.ui.screens.HomeScreen
import com.sdd.marketplace.feature.kyc.ui.KycVerificationScreen
import com.sdd.marketplace.feature.notifications.ui.NotificationsScreen
import com.sdd.marketplace.feature.orders.ui.*
import com.sdd.marketplace.feature.product.ui.screens.*
import com.sdd.marketplace.feature.profile.ui.screens.ProfileScreen
import com.sdd.marketplace.feature.search.ui.screens.SearchScreen
import com.sdd.marketplace.feature.settings.ui.screens.*
import com.sdd.marketplace.feature.static.ui.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object OtpVerify : Screen("otp_verify/{phone}") { fun createRoute(phone: String) = "otp_verify/$phone" }
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object ProductDetail : Screen("product/{productId}") { fun createRoute(id: String) = "product/$id" }
    object PostProduct : Screen("post_product")
    object Inbox : Screen("inbox")
    object ChatDetail : Screen("chat/{chatId}") { fun createRoute(id: String) = "chat/$id" }
    object Profile : Screen("profile?userId={userId}") { fun createRoute(id: String? = null) = if (id != null) "profile?userId=$id" else "profile" }
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit_profile")
    object Wishlist : Screen("wishlist")
    object SellerProfile : Screen("seller/{sellerId}") { fun createRoute(id: String) = "seller/$id" }
    // Settings
    object Settings : Screen("settings")
    object ChangePassword : Screen("change_password")
    object ChangeEmail : Screen("change_email")
    object DeleteAccount : Screen("delete_account")
    object ConfirmLogout : Screen("confirm_logout")
    object SwitchAccount : Screen("switch_account")
    object ChangeLanguage : Screen("change_language")
    // KYC
    object KycVerification : Screen("kyc_verification")
    // Legal / Static
    object TermsConditions : Screen("terms_conditions")
    object PrivacyPolicy : Screen("privacy_policy")
    object SellerTerms : Screen("seller_terms")
    object BuyerTerms : Screen("buyer_terms")
    // Help
    object HelpSupport : Screen("help_support")
    object ReportBug : Screen("report_bug")
    object RateApp : Screen("rate_app")
    // Social
    object Followers : Screen("followers/{userId}") { fun createRoute(id: String) = "followers/$id" }
    // Orders
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") { fun createRoute(id: String) = "order/$id" }
    object Payment : Screen("payment/{orderId}") { fun createRoute(id: String) = "payment/$id" }
}

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, "home", "home_outlined"),
    BottomNavItem("Chats", Screen.Inbox.route, "chat_bubble", "chat_bubble_outline"),
    BottomNavItem("Post", Screen.PostProduct.route, "add", "add"),
    BottomNavItem("Profile", Screen.Profile.createRoute(), "person", "person_outlined"),
    BottomNavItem("Search", Screen.Search.route, "search", "search")
)

data class BottomNavItem(val label: String, val route: String, val selectedIcon: String, val unselectedIcon: String)

@Composable
fun SddNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = Screen.Login.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgot = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToOtp = { phone -> navController.navigate(Screen.OtpVerify.createRoute(phone)) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            )
        }
        composable(Screen.OtpVerify.route, arguments = listOf(navArgument("phone") { type = NavType.StringType })) {
            OtpVerifyScreen(onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } })
        }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() }) }

        // Main with bottom nav
        composable(Screen.Home.route) { MainScaffold(navController) { HomeScreen(navController) } }
        composable(Screen.Inbox.route) { MainScaffold(navController) { InboxScreen(navController) } }
        composable(Screen.PostProduct.route) {
            PostProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.PostProduct.route) { inclusive = true } } }
            )
        }
        composable(Screen.Profile.route, arguments = listOf(navArgument("userId") { type = NavType.StringType; nullable = true; defaultValue = null })) {
            MainScaffold(navController) { ProfileScreen(navController) }
        }
        composable(Screen.Search.route) { MainScaffold(navController) { SearchScreen(navController) } }

        // Detail screens
        composable(Screen.ProductDetail.route, arguments = listOf(navArgument("productId") { type = NavType.StringType })) {
            ProductDetailScreen(navController)
        }
        composable(Screen.ChatDetail.route, arguments = listOf(navArgument("chatId") { type = NavType.StringType })) {
            ChatDetailScreen(navController)
        }
        composable(Screen.Notifications.route) { NotificationsScreen(navController) }
        composable(Screen.Wishlist.route) { WishlistScreen(navController) }

        // Settings
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController) }
        composable(Screen.ChangeEmail.route) { ChangeEmailScreen(navController) }
        composable(Screen.DeleteAccount.route) { DeleteAccountScreen(navController) }
        composable(Screen.SwitchAccount.route) { SwitchAccountScreen(navController) }
        composable(Screen.ChangeLanguage.route) { ChangeLanguageScreen(navController) }
        composable(Screen.ConfirmLogout.route) {
            AlertDialog(
                onDismissRequest = { navController.popBackStack() },
                title = { Text("Sign Out") },
                text = { Text("Are you sure you want to sign out?") },
                confirmButton = {
                    Button(onClick = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }) { Text("Sign Out") }
                },
                dismissButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") } }
            )
        }

        // KYC
        composable(Screen.KycVerification.route) { KycVerificationScreen(navController) }

        // Legal/Static
        composable(Screen.TermsConditions.route) { TermsConditionsScreen(navController) }
        composable(Screen.PrivacyPolicy.route) { PrivacyPolicyScreen(navController) }
        composable(Screen.SellerTerms.route) { TermsConditionsScreen(navController) }
        composable(Screen.BuyerTerms.route) { TermsConditionsScreen(navController) }

        // Help
        composable(Screen.HelpSupport.route) { HelpSupportScreen(navController) }
        composable(Screen.ReportBug.route) { ReportBugScreen(navController) }
        composable(Screen.RateApp.route) { RateAppScreen(navController) }

        // Social
        composable(Screen.Followers.route, arguments = listOf(navArgument("userId") { type = NavType.StringType })) {
            FollowersScreen(navController)
        }

        // Orders
        composable(Screen.Orders.route) { OrdersScreen(navController) }
        composable(Screen.OrderDetail.route, arguments = listOf(navArgument("orderId") { type = NavType.StringType })) {
            OrderDetailScreen(navController)
        }
    }
}

@Composable
fun MainScaffold(navController: NavController, content: @Composable () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = { SddBottomNavBar(currentRoute = currentRoute, onNavigate = { route -> navController.navigate(route) { launchSingleTop = true; restoreState = true } }) }
    ) { _ -> content() }
}
