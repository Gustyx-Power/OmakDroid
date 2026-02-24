package id.xms.omakdroid.ui.setup

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.xms.omakdroid.core.SettingsRepository

@Composable
fun SetupWizard(
    settingsRepository: SettingsRepository,
    onSetupComplete: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: SetupViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                viewModel = viewModel,
                onNext = { navController.navigate("timezone") }
            )
        }
        
        composable("timezone") {
            TimezoneScreen(
                viewModel = viewModel,
                onNext = { navController.navigate("user_setup") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("user_setup") {
            UserSetupScreen(
                viewModel = viewModel,
                onNext = { navController.navigate("desktop") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("desktop") {
            DesktopScreen(
                viewModel = viewModel,
                onNext = { navController.navigate("engine") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("engine") {
            EngineScreen(
                viewModel = viewModel,
                onInstall = { navController.navigate("install_progress") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("install_progress") {
            InstallProgressScreen(
                viewModel = viewModel,
                settingsRepository = settingsRepository,
                onComplete = { navController.navigate("complete") }
            )
        }
        
        composable("complete") {
            CompleteScreen(
                settingsRepository = settingsRepository,
                onRestart = onSetupComplete
            )
        }
    }
}
