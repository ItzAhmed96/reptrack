package com.example.reptrack.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TrainerDashboard : Screen("trainer_dashboard")
    object CreateProgram : Screen("create_program")
    object ManageExercises : Screen("manage_exercises/{programId}") {
        fun createRoute(programId: String) = "manage_exercises/$programId"
    }
    object TraineeDashboard : Screen("trainee_dashboard")
    object ProgramDetail : Screen("program_detail/{programId}") {
        fun createRoute(programId: String) = "program_detail/$programId"
    }
    object Workout : Screen("workout/{exerciseId}") {
        fun createRoute(exerciseId: String) = "workout/$exerciseId"
    }
    object ProgressHistory : Screen("progress_history")
    object Home : Screen("home")
    object WorkoutTab : Screen("workout_tab")
    object Profile : Screen("profile")
    object ExploreWorkouts : Screen("explore_workouts")
    object CreateWorkout : Screen("create_workout")
    object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: String) = "workout_detail/$workoutId"
    }
    object SocialFeed : Screen("social_feed")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object SearchUser : Screen("search_user")
}
