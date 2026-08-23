package com.example.aprendeaprender.data.api

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class LoginRequest(val email: String, val password: String)

data class ApiUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class SessionResponse(
    val token: String,
    val expiresAt: String,
    val user: ApiUser
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phone: String
)

data class SubjectRequest(
    val name: String,
    val instructor: String,
    val utadeoId: Int? = null,
    val topics: List<String> = emptyList()
)

data class SubjectResponse(
    val id: String,
    val name: String,
    val instructor: String?,
    val utadeoId: Int?,
    val createdAt: String
)

data class ParticipantRequest(val name: String, val role: String)
data class ParticipantResponse(val name: String, val role: String)

data class UtadeoSubjectRequest(
    val utadeoId: Int,
    val name: String,
    val instructor: String,
    val participants: List<ParticipantRequest> = emptyList()
)

data class TaskRequest(
    val subjectId: String,
    val title: String,
    val description: String,
    val dueAt: String?,
    val priority: String,
    val status: String
)

data class TaskResponse(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val title: String,
    val description: String,
    val dueAt: String?,
    val priority: String,
    val status: String,
    val createdAt: String
)

data class UtadeoTaskRequest(
    val assignmentId: Int,
    val courseId: Int,
    val title: String,
    val description: String,
    val dueDateMillis: Long,
    val status: String
)

data class StatusRequest(val status: String)

data class SubjectCompletionResponse(
    val subjectId: String,
    val subjectName: String,
    val completed: Boolean
)

data class DailyChallengeResponse(
    val id: String,
    val date: String,
    val totalSubjects: Int,
    val completed: Boolean,
    val subjects: List<SubjectCompletionResponse>
)

data class ChallengeQuestionRequest(
    val question: String,
    val options: List<String>,
    val correctOption: Int,
    val explanation: String
)

data class ChallengeQuestionResponse(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val question: String,
    val options: List<String>,
    val correctOption: Int,
    val explanation: String
)
