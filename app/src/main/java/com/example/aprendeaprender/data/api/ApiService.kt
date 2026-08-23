package com.example.aprendeaprender.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): SessionResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): SessionResponse

    @GET("api/auth/me")
    suspend fun me(): ApiUser

    @DELETE("api/auth/session")
    suspend fun logout()

    @GET("api/profile")
    suspend fun getProfile(): ApiUser

    @PATCH("api/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiUser

    @GET("api/subjects")
    suspend fun getSubjects(): List<SubjectResponse>

    @GET("api/subjects/{id}")
    suspend fun getSubject(@Path("id") id: String): SubjectResponse

    @POST("api/subjects")
    suspend fun createSubject(@Body request: SubjectRequest): SubjectResponse

    @DELETE("api/subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: String)

    @GET("api/tasks")
    suspend fun getTasks(): List<TaskResponse>

    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskResponse

    @PATCH("api/tasks/{id}/status")
    suspend fun updateTaskStatus(@Path("id") id: String, @Body request: StatusRequest): TaskResponse

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)

    @GET("api/challenges/today")
    suspend fun getTodayChallenge(): DailyChallengeResponse

    @POST("api/challenges/today/subjects/{subjectId}/complete")
    suspend fun completeChallengeSubject(@Path("subjectId") subjectId: String): DailyChallengeResponse
}
