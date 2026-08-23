package com.example.aprendeaprender.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R

data class HomeUiState(
    val userName: String = "",
    val inProgressTasks: Int = 0,
    val overdueTasks: Int = 0,
    val completedTasks: Int = 0,
    val todayTasks: Int = 0,
    val progress: Float = 0f
)

private val DarkBackground = Color(0xFF0D1B2A)
private val CardBackground = Color(0xFF1B2A3B)
private val TealAccent = Color(0xFF61C9D8)
private val GreenStat = Color(0xFF4CAF50)
private val YellowStat = Color(0xFFFFC107)
private val RedStat = Color(0xFFF44336)
private val SubtitleGray = Color(0xFF8899AA)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSubjectsClick: () -> Unit,
    onTasksTodayClick: () -> Unit,
    onEnrollSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        HeaderSection(
            userName = if (uiState.userName.isBlank()) {
                stringResource(R.string.home_default_user_name)
            } else {
                uiState.userName
            },
            inProgressTasks = uiState.inProgressTasks,
            overdueTasks = uiState.overdueTasks
        )

        Spacer(modifier = Modifier.height(20.dp))

        StatsCard(
            completedTasks = uiState.completedTasks,
            inProgressTasks = uiState.inProgressTasks,
            todayTasks = uiState.todayTasks,
            progress = uiState.progress.coerceIn(0f, 1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuRow(
            text = stringResource(R.string.home_view_subjects),
            onClick = onSubjectsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuRow(
            text = stringResource(R.string.home_view_tasks_today),
            onClick = onTasksTodayClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        EnrollButton(onClick = onEnrollSubjectClick)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    inProgressTasks: Int,
    overdueTasks: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(R.string.home_logo_content_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.home_greeting, userName),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_subtitle, inProgressTasks, overdueTasks),
                color = SubtitleGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun StatsCard(
    completedTasks: Int,
    inProgressTasks: Int,
    todayTasks: Int,
    progress: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = stringResource(R.string.home_stat_completed), value = completedTasks, color = GreenStat)
                StatItem(label = stringResource(R.string.home_stat_in_progress), value = inProgressTasks, color = YellowStat)
                StatItem(label = stringResource(R.string.home_stat_today), value = todayTasks, color = RedStat)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TealAccent,
                trackColor = Color(0xFF2A3A4B)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = SubtitleGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(48.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = value.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = Color.White, fontSize = 15.sp)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EnrollButton(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_enroll_subject),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}