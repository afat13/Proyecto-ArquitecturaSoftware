package com.example.aprendeaprender.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R

enum class BottomNavDestination {
    HOME, SUBJECTS, TASKS, CHAT, CHALLENGES, PROFILE
}

private data class BottomNavItem(
    val destination: BottomNavDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val GlassBackground = Color(0xFF1B2A3B).copy(alpha = 0.65f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val TealAccent = Color(0xFF61C9D8)
private val SubtitleGray = Color(0xFF8899AA)

@Composable
fun BottomNavBar(
    selectedDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            destination = BottomNavDestination.HOME,
            labelRes = R.string.nav_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            destination = BottomNavDestination.SUBJECTS,
            labelRes = R.string.nav_subjects,
            selectedIcon = Icons.Filled.MenuBook,
            unselectedIcon = Icons.Outlined.MenuBook
        ),
        BottomNavItem(
            destination = BottomNavDestination.TASKS,
            labelRes = R.string.nav_tasks,
            selectedIcon = Icons.Filled.Assignment,
            unselectedIcon = Icons.Outlined.Assignment
        ),
        BottomNavItem(
            destination = BottomNavDestination.CHAT,
            labelRes = R.string.nav_chat,
            selectedIcon = Icons.AutoMirrored.Filled.Chat,
            unselectedIcon = Icons.AutoMirrored.Outlined.Chat
        ),
        BottomNavItem(
            destination = BottomNavDestination.CHALLENGES,
            labelRes = R.string.nav_challenges,
            selectedIcon = Icons.Filled.EmojiEvents,
            unselectedIcon = Icons.Outlined.EmojiEvents
        ),
        BottomNavItem(
            destination = BottomNavDestination.PROFILE,
            labelRes = R.string.nav_profile,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    val glassShape = RoundedCornerShape(40.dp)
    val density = LocalDensity.current
    var barWidthPx by remember { mutableIntStateOf(0) }

    val selectedIndex = items.indexOfFirst { it.destination == selectedDestination }.coerceAtLeast(0)
    val itemWidthPx = if (barWidthPx > 0) barWidthPx / items.size else 0
    val targetOffsetPx = selectedIndex * itemWidthPx

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffsetPx.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_indicator_offset"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .clip(glassShape)
            .background(GlassBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassBorder,
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = glassShape
            )
            .onSizeChanged { barWidthPx = it.width }
    ) {
        if (itemWidthPx > 0) {
            val indicatorWidthDp = with(density) { itemWidthPx.toDp() }
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.toInt(), 0) }
                    .size(width = indicatorWidthDp, height = 64.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.destination == selectedDestination

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDestinationSelected(item.destination)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                        tint = if (isSelected) TealAccent else SubtitleGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(item.labelRes),
                        color = if (isSelected) TealAccent else SubtitleGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}