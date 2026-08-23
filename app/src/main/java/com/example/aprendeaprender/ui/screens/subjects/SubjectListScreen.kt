package com.example.aprendeaprender.ui.screens.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.ui.theme.*
import com.example.aprendeaprender.viewmodel.SubjectListUiState

private val CardBackground = Color(0xFF1B2A3B)


@Composable
fun SubjectListScreen(
    uiState: SubjectListUiState,
    onDeleteClick: (String) -> Unit,
    onSubjectClick: (Subject) -> Unit,
    onImportUtadeoClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    stringResource(R.string.subject_delete_title),
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Text(
                    stringResource(R.string.subject_delete_message),
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(subject.id)
                    subjectToDelete = null
                }) {
                    Text(stringResource(R.string.subject_btn_delete), color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text(stringResource(R.string.profile_btn_cancel), color = TextGray)
                }
            }
        )
    }

    Scaffold(containerColor = DarkBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Text("‹", fontSize = 32.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
            }

            Text(
                text = stringResource(R.string.subject_my_subjects),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Importar desde Utadeo
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onImportUtadeoClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subject_import_utadeo),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else if (uiState.subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.subject_no_subjects),
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.subjects) { subject ->
                        SubjectCard(
                            subject = subject,
                            onClick = { onSubjectClick(subject) },
                            onDeleteClick = { subjectToDelete = subject }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.asignatura,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = subject.instructor, fontSize = 14.sp, color = TextGray)

            if (subject.temas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.subject_topics_count, subject.temas.size),
                    fontSize = 12.sp,
                    color = TextGray
                )
                subject.temas.forEach { tema ->
                    Text(
                        text = "• $tema",
                        fontSize = 13.sp,
                        color = TextWhite,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}