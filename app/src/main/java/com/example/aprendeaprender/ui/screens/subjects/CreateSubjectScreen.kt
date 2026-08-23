package com.example.aprendeaprender.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.ui.components.AppButton
import com.example.aprendeaprender.ui.components.AppTextField
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.CreateSubjectUiState

private val CardBackground = Color(0xFF1B2A3B)

@Composable
fun CreateSubjectScreen(
    uiState: CreateSubjectUiState,
    onAsignaturaChange: (String) -> Unit,
    onInstructorChange: (String) -> Unit,
    onTemaChange: (Int, String) -> Unit,
    onAgregarTema: () -> Unit,
    onEliminarTema: (Int) -> Unit,
    onInscribirClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(containerColor = DarkBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Text("‹", fontSize = 32.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
            }

            Text(
                text = stringResource(R.string.subject_new_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = CyanAccent,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.subject_data_header),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = uiState.asignatura,
                label = stringResource(R.string.subject_name_label),
                onValueChange = onAsignaturaChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = uiState.instructor,
                label = stringResource(R.string.subject_instructor_label),
                onValueChange = onInstructorChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.subject_topics_header),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            uiState.temas.forEachIndexed { index, tema ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppTextField(
                        value = tema,
                        label = stringResource(R.string.subject_topic_label, index + 1),
                        onValueChange = { onTemaChange(index, it) },
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.temas.size > 1) {
                        IconButton(onClick = { onEliminarTema(index) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAgregarTema)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subject_add_topic),
                        color = TextWhite,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            uiState.mensajeErrorResId?.let { resId ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(resId), color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = if (uiState.cargando) {
                    stringResource(R.string.common_loading)
                } else {
                    stringResource(R.string.subject_enroll)
                },
                onClick = onInscribirClick,
                enabled = !uiState.cargando
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}