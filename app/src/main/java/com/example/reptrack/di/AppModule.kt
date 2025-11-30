package com.example.reptrack.di

import com.example.reptrack.data.data_sources.local.ExerciseLocalDataSource
import com.example.reptrack.data.data_sources.local.ProgramLocalDataSource
import com.example.reptrack.data.data_sources.local.ProgressLocalDataSource
import com.example.reptrack.data.data_sources.local.UserLocalDataSource
import com.example.reptrack.data.data_sources.remote.AuthDataSource
import com.example.reptrack.data.data_sources.remote.ExerciseRemoteDataSource
import com.example.reptrack.data.data_sources.remote.ProgramRemoteDataSource
import com.example.reptrack.data.data_sources.remote.ProgressRemoteDataSource
import com.example.reptrack.data.data_sources.remote.WorkoutPlanRemoteDataSource
import com.example.reptrack.data.repositories.ProgramRepositoryImpl
import com.example.reptrack.data.repositories.ProgressRepositoryImpl
import com.example.reptrack.data.repositories.UserRepositoryImpl
import com.example.reptrack.domain.repositories.ProgramRepository
import com.example.reptrack.domain.repositories.ProgressRepository
import com.example.reptrack.domain.repositories.UserRepository
import com.example.reptrack.domain.use_cases.AuthUseCase
import com.example.reptrack.domain.use_cases.ManageProgramUseCase
import com.example.reptrack.domain.use_cases.TrackProgressUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AppModule {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Local Data Sources
    private lateinit var _userLocalDataSource: UserLocalDataSource
    fun provideUserLocalDataSource(context: android.content.Context): UserLocalDataSource {
        if (!::_userLocalDataSource.isInitialized) {
            _userLocalDataSource = UserLocalDataSource(context)
        }
        return _userLocalDataSource
    }

    private lateinit var _programLocalDataSource: ProgramLocalDataSource
    fun provideProgramLocalDataSource(context: android.content.Context): ProgramLocalDataSource {
        if (!::_programLocalDataSource.isInitialized) {
            _programLocalDataSource = ProgramLocalDataSource(context)
        }
        return _programLocalDataSource
    }
    
    private lateinit var _exerciseLocalDataSource: ExerciseLocalDataSource
    fun provideExerciseLocalDataSource(context: android.content.Context): ExerciseLocalDataSource {
        if (!::_exerciseLocalDataSource.isInitialized) {
            _exerciseLocalDataSource = ExerciseLocalDataSource(context)
        }
        return _exerciseLocalDataSource
    }
    
    private lateinit var _progressLocalDataSource: ProgressLocalDataSource
    fun provideProgressLocalDataSource(context: android.content.Context): ProgressLocalDataSource {
        if (!::_progressLocalDataSource.isInitialized) {
            _progressLocalDataSource = ProgressLocalDataSource(context)
        }
        return _progressLocalDataSource
    }
    
    // Remote Data Sources
    val authDataSource: AuthDataSource by lazy { AuthDataSource(auth, firestore) }
    val programRemoteDataSource: ProgramRemoteDataSource by lazy { ProgramRemoteDataSource(firestore) }
    val exerciseRemoteDataSource: ExerciseRemoteDataSource by lazy { ExerciseRemoteDataSource(firestore) }
    val progressRemoteDataSource: ProgressRemoteDataSource by lazy { ProgressRemoteDataSource(firestore) }
    val workoutPlanRemoteDataSource: WorkoutPlanRemoteDataSource by lazy { WorkoutPlanRemoteDataSource(firestore) }
    
    // Social Feed Data Sources
    val postRemoteDataSource: com.example.reptrack.data.data_sources.remote.PostRemoteDataSource by lazy { 
        com.example.reptrack.data.data_sources.remote.PostRemoteDataSource(firestore) 
    }
    val commentRemoteDataSource: com.example.reptrack.data.data_sources.remote.CommentRemoteDataSource by lazy { 
        com.example.reptrack.data.data_sources.remote.CommentRemoteDataSource(firestore) 
    }
    val likeRemoteDataSource: com.example.reptrack.data.data_sources.remote.LikeRemoteDataSource by lazy { 
        com.example.reptrack.data.data_sources.remote.LikeRemoteDataSource(firestore) 
    }
    val followRemoteDataSource: com.example.reptrack.data.data_sources.remote.FollowRemoteDataSource by lazy {
        com.example.reptrack.data.data_sources.remote.FollowRemoteDataSource(firestore)
    }
    val notificationRemoteDataSource: com.example.reptrack.data.data_sources.remote.NotificationRemoteDataSource by lazy {
        com.example.reptrack.data.data_sources.remote.NotificationRemoteDataSource(firestore)
    }

    // Repositories
    val userRepository: UserRepository by lazy { UserRepositoryImpl(authDataSource) }
    
    private lateinit var _programRepository: ProgramRepository
    fun provideProgramRepository(context: android.content.Context): ProgramRepository {
        if (!::_programRepository.isInitialized) {
            _programRepository = ProgramRepositoryImpl(
                programRemoteDataSource,
                exerciseRemoteDataSource,
                workoutPlanRemoteDataSource,
                provideProgramLocalDataSource(context),
                provideExerciseLocalDataSource(context)
            )
        }
        return _programRepository
    }
    
    private lateinit var _progressRepository: ProgressRepository
    fun provideProgressRepository(context: android.content.Context): ProgressRepository {
        if (!::_progressRepository.isInitialized) {
            _progressRepository = ProgressRepositoryImpl(
                progressRemoteDataSource,
                provideProgressLocalDataSource(context)
            )
        }
        return _progressRepository
    }
    
    // Social Feed Repository
    val socialRepository: com.example.reptrack.data.repositories.SocialRepositoryImpl by lazy {
        com.example.reptrack.data.repositories.SocialRepositoryImpl(
            postRemoteDataSource,
            commentRemoteDataSource,
            likeRemoteDataSource,
            followRemoteDataSource,
            notificationRemoteDataSource,
            firestore
        )
    }
    
    // Use Cases
    val authUseCase: AuthUseCase by lazy { AuthUseCase(userRepository) }
    
    fun provideManageProgramUseCase(context: android.content.Context): ManageProgramUseCase {
        return ManageProgramUseCase(provideProgramRepository(context))
    }
    
    fun provideTrackProgressUseCase(context: android.content.Context): TrackProgressUseCase {
        return TrackProgressUseCase(provideProgressRepository(context))
    }
    
    // Social Feed Use Case
    val socialFeedUseCase: com.example.reptrack.domain.use_cases.SocialFeedUseCase by lazy {
        com.example.reptrack.domain.use_cases.SocialFeedUseCase(socialRepository)
    }
}
