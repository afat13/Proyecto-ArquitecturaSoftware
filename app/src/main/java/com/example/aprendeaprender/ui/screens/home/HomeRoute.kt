package com.example.aprendeaprender.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onNavigateToProfile: () -> Unit,
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToChallenges: () -> Unit = {},
    onEnrollSubjectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    HomeScreen(
        uiState = uiState,
        onSubjectsClick = onNavigateToSubjects,
        onTasksTodayClick = onNavigateToTasks,
        onEnrollSubjectClick = onEnrollSubjectClick,
        modifier = modifier
    )
}