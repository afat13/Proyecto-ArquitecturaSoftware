package com.example.aprendeaprender.ui.screens.challenges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendeaprender.R
import com.example.aprendeaprender.data.model.ChallengeQuestion
import com.example.aprendeaprender.data.model.Subject
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.ui.theme.DarkSurface
import com.example.aprendeaprender.ui.theme.ErrorRed
import com.example.aprendeaprender.ui.theme.SuccessGreen
import com.example.aprendeaprender.ui.theme.TextGray
import com.example.aprendeaprender.ui.theme.TextWhite
import com.example.aprendeaprender.viewmodel.ChallengeDotStatus
import com.example.aprendeaprender.viewmodel.DailyChallengeUiState
import com.example.aprendeaprender.viewmodel.SubjectChallengeUiState
import kotlinx.coroutines.delay

private const val QUESTION_TIME_SECONDS = 60

@Composable
fun ChallengeDailyScreen(
    uiState: DailyChallengeUiState,
    onOpenSubjects: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.challenge_daily_title),
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(52.dp))

        if (uiState.cargando) {
            CircularProgressIndicator(color = CyanAccent)
        } else {
            DailyInfo(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            CalendarCard(uiState = uiState)

            Spacer(modifier = Modifier.height(18.dp))

            if (uiState.retoHoyCompletado) {
                Text(
                    text = stringResource(R.string.challenge_return_tomorrow),
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                PillButton(
                    text = stringResource(R.string.challenge_completed_return_tomorrow),
                    color = SuccessGreen,
                    onClick = {}
                )
            } else {
                PillButton(
                    text = stringResource(R.string.challenge_subjects_button),
                    color = CyanAccent,
                    onClick = onOpenSubjects
                )
            }
        }

        uiState.mensajeErrorResId?.let { errorRes ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(errorRes),
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DailyInfo(uiState: DailyChallengeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = uiState.anio.toString(),
                color = TextGray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = uiState.mes,
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(TextWhite.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.challenge_month_progress,
                        uiState.diasCompletadosCount,
                        uiState.diasEnMes
                    ),
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.trophyLevel.label,
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarCard(uiState: DailyChallengeUiState) {
    val weekDays = stringArrayResource(R.array.challenge_week_days)
    val blanks = (uiState.primerDiaSemana - 1).coerceIn(0, 6)
    val totalCells = blanks + uiState.diasEnMes
    val rows = ((totalCells + 6) / 7).coerceAtLeast(5)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (column in 0 until 7) {
                        val cell = row * 7 + column
                        val day = cell - blanks + 1

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..uiState.diasEnMes) {
                                DayCell(
                                    day = day,
                                    isToday = day == uiState.diaActual,
                                    isCompleted = uiState.diasCompletados.contains(day)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isCompleted: Boolean
) {
    val background = when {
        isCompleted -> SuccessGreen
        isToday -> CyanAccent
        else -> Color.Transparent
    }

    val textColor = when {
        isCompleted || isToday -> DarkBackground
        else -> TextWhite
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontSize = 18.sp,
            fontWeight = if (isCompleted || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ChallengeSubjectScreen(
    uiState: SubjectChallengeUiState,
    onSubjectClick: (Subject) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = stringResource(R.string.challenge_subject_title),
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(54.dp))

        when {
            uiState.cargando -> {
                CircularProgressIndicator(color = CyanAccent)
            }

            uiState.materias.isEmpty() -> {
                Text(
                    text = stringResource(R.string.challenge_no_subjects),
                    color = TextGray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                if (uiState.todasCompletadas) {
                    Text(
                        text = stringResource(R.string.challenge_all_completed_today),
                        color = SuccessGreen,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }

                uiState.materias.forEach { subject ->
                    val completada = uiState.materiasCompletadas.contains(subject.id)

                    PillButton(
                        text = if (completada) {
                            stringResource(R.string.challenge_completed_label)
                        } else {
                            subject.asignatura
                        },
                        color = if (completada) SuccessGreen else CyanAccent,
                        onClick = {
                            if (!completada) {
                                onSubjectClick(subject)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        uiState.mensajeErrorResId?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PillButton(
            text = stringResource(R.string.back_label),
            color = DarkSurface,
            textColor = TextWhite,
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ChallengeQuizScreen(
    uiState: SubjectChallengeUiState,
    onAnswerSelected: (Int) -> Unit,
    onTryAgainClick: () -> Unit,
    onNextClick: () -> Unit,
    onTimeExpired: () -> Unit,
    onBackToSubjectsClick: () -> Unit
) {
    val question = uiState.preguntaActual

    var segundosRestantes by remember(question?.id) {
        mutableIntStateOf(QUESTION_TIME_SECONDS)
    }

    LaunchedEffect(
        question?.id,
        uiState.mostrarResultado,
        uiState.cargandoActividad,
        uiState.materiaCompletada
    ) {
        if (
            question == null ||
            uiState.mostrarResultado ||
            uiState.cargandoActividad ||
            uiState.materiaCompletada
        ) {
            return@LaunchedEffect
        }

        segundosRestantes = QUESTION_TIME_SECONDS

        while (segundosRestantes > 0) {
            delay(1000)
            segundosRestantes -= 1
        }

        onTimeExpired()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.challenge_daily_title),
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        ProgressDots(uiState = uiState)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = uiState.materiaActual?.asignatura.orEmpty(),
            color = TextWhite,
            fontSize = 17.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(
                R.string.challenge_result_summary,
                uiState.respuestasCorrectas,
                uiState.respuestasIncorrectas,
                uiState.respuestasPorTiempo
            ),
            color = TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        when {
            uiState.cargandoActividad -> {
                CircularProgressIndicator(color = CyanAccent)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.challenge_loading_questions),
                    color = TextGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            uiState.materiaCompletada -> {
                CompletionMessage(
                    text = stringResource(R.string.challenge_subject_completed),
                    onBackToSubjectsClick = onBackToSubjectsClick
                )
            }

            question == null -> {
                Text(
                    text = stringResource(R.string.challenge_error_questions),
                    color = ErrorRed,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                PillButton(
                    text = stringResource(R.string.challenge_back_subjects),
                    color = CyanAccent,
                    onClick = onBackToSubjectsClick
                )
            }

            else -> {
                Text(
                    text = stringResource(
                        R.string.challenge_question_counter,
                        uiState.numeroPreguntaVisual,
                        uiState.totalObjetivoPreguntas
                    ),
                    color = TextGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        R.string.challenge_time_remaining,
                        segundosRestantes
                    ),
                    color = if (segundosRestantes <= 10 && !uiState.mostrarResultado) {
                        ErrorRed
                    } else {
                        TextGray
                    },
                    fontSize = 13.sp,
                    fontWeight = if (segundosRestantes <= 10 && !uiState.mostrarResultado) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestionCard(question = question)

                Spacer(modifier = Modifier.height(26.dp))

                question.opciones.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        color = answerColor(
                            index = index,
                            question = question,
                            uiState = uiState
                        ),
                        onClick = {
                            if (!uiState.mostrarResultado && !uiState.cargandoActividad) {
                                onAnswerSelected(index)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.mostrarResultado) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            uiState.tiempoAgotado -> stringResource(R.string.challenge_timeout)
                            uiState.respuestaCorrecta -> stringResource(R.string.challenge_correct)
                            else -> stringResource(R.string.challenge_wrong)
                        },
                        color = when {
                            uiState.tiempoAgotado -> TextGray
                            uiState.respuestaCorrecta -> SuccessGreen
                            else -> ErrorRed
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (question.explicacion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = question.explicacion,
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (uiState.esperandoSiguientePorTiempo) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.challenge_next_question_pending),
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PillButton(
                        text = stringResource(R.string.challenge_next),
                        color = CyanAccent,
                        onClick = if (uiState.respuestaCorrecta) {
                            onNextClick
                        } else {
                            onTryAgainClick
                        }
                    )
                } else if (uiState.generandoSiguienteLote) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.challenge_generating_next),
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        uiState.mensajeErrorResId?.let { errorRes ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(errorRes),
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProgressDots(uiState: SubjectChallengeUiState) {
    val total = uiState.totalObjetivoPreguntas.coerceAtLeast(6)

    val statuses = (0 until total).map { index ->
        uiState.resultadosPreguntas.getOrNull(index) ?: ChallengeDotStatus.PENDING
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.chunked(8).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { status ->
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(dotColor(status))
                    )
                }
            }
        }
    }
}

private fun dotColor(status: ChallengeDotStatus): Color {
    return when (status) {
        ChallengeDotStatus.PENDING -> TextWhite
        ChallengeDotStatus.CORRECT -> SuccessGreen
        ChallengeDotStatus.WRONG -> ErrorRed
        ChallengeDotStatus.TIMEOUT -> TextGray
        ChallengeDotStatus.INTERRUPTED -> TextGray
    }
}

@Composable
private fun HeaderLogo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(R.string.logo_app_desc),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
private fun QuestionCard(question: ChallengeQuestion) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 190.dp)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = question.pregunta,
                color = TextWhite,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp
            )
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            color = DarkBackground,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun PillButton(
    text: String,
    color: Color,
    textColor: Color = DarkBackground,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun CompletionMessage(
    text: String,
    onBackToSubjectsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = SuccessGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            PillButton(
                text = stringResource(R.string.challenge_back_subjects),
                color = CyanAccent,
                onClick = onBackToSubjectsClick
            )
        }
    }
}

private fun answerColor(
    index: Int,
    question: ChallengeQuestion,
    uiState: SubjectChallengeUiState
): Color {
    if (!uiState.mostrarResultado) return CyanAccent

    val selected = uiState.respuestaSeleccionada
    val correct = question.respuestaCorrecta

    return when {
        index == correct -> SuccessGreen
        index == selected && selected != correct -> ErrorRed
        else -> CyanAccent
    }
}