package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.generator.GeneratorScreen
import com.example.ui.generator.GeneratorViewModel
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.navigation.PromptBottomNavigationBar
import com.example.ui.navigation.Screen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.promptpro.ui.templates.TemplateEditorScreen
import com.example.promptpro.ui.templates.TemplateEditorViewModel
import com.example.promptpro.ui.templates.TemplateListScreen
import com.example.promptpro.ui.templates.TemplateListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as PromptGeneratorApplication).container

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                MainAppScreen(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    navController: NavHostController,
    appContainer: com.example.di.AppContainer,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Generator.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            PromptBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Screen.Generator.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Generator.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Generator.route) {
                val generatorViewModel: GeneratorViewModel = viewModel(
                    factory = GeneratorViewModel.provideFactory(
                        generatePromptUseCase = appContainer.generatePromptUseCase,
                        savePromptUseCase = appContainer.savePromptUseCase,
                        preferencesDataStore = appContainer.preferencesDataStore
                    )
                )
                GeneratorScreen(viewModel = generatorViewModel)
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.provideFactory(
                        getPromptHistoryUseCase = appContainer.getPromptHistoryUseCase,
                        deletePromptUseCase = appContainer.deletePromptUseCase,
                        clearPromptHistoryUseCase = appContainer.clearPromptHistoryUseCase,
                        exportPromptHistoryUseCase = appContainer.exportPromptHistoryUseCase
                    )
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Templates.route) {
            val templatesViewModel: TemplateListViewModel = viewModel(
                factory = TemplateListViewModel.provideFactory(
                    observeTemplatesUseCase = appContainer.observeTemplatesUseCase,
                    deleteTemplateUseCase = appContainer.deleteTemplateUseCase
                )
            )
            TemplateListScreen(
                viewModel = templatesViewModel,
                onTemplateClick = { id ->
                    navController.navigate("templates/editor/$id")
                },
                onCreateNew = {
                    navController.navigate("templates/editor/new")
                }
            )
        }

        composable(
            route = "templates/editor/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId").orEmpty()
            val editorViewModel: TemplateEditorViewModel = viewModel(
                factory = TemplateEditorViewModel.provideFactory(appContainer.promptTemplateRepository)
            )
            if (templateId.isNotBlank() && templateId != "new") {
                editorViewModel.loadTemplate(templateId)
            }
            TemplateEditorScreen(
                viewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(
                        preferencesDataStore = appContainer.preferencesDataStore,
                        exportPromptHistoryUseCase = appContainer.exportPromptHistoryUseCase,
                        clearPromptHistoryUseCase = appContainer.clearPromptHistoryUseCase
                    )
                )
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
