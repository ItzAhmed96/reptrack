package com.example.reptrack.data.data_sources.remote

import com.example.reptrack.common.Constants
import com.example.reptrack.common.Resource
import com.example.reptrack.domain.models.Notification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class NotificationRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    suspend fun createNotification(notification: Notification): Resource<String> {
        return try {
            val ref = firestore.collection(Constants.NOTIFICATIONS_COLLECTION).document()
            val newNotification = notification.copy(id = ref.id, timestamp = Timestamp.now())
            ref.set(newNotification).await()
            android.util.Log.d("NotificationDS", "Created notification: ${notification.type} for user ${notification.userId}")
            Resource.Success(newNotification.id)
        } catch (e: Exception) {
            android.util.Log.e("NotificationDS", "Error creating notification: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to create notification")
        }
    }

    suspend fun getNotificationsForUser(userId: String): Resource<List<Notification>> {
        return try {
            val snapshot = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val notifications = snapshot.toObjects(Notification::class.java)
            // Sort in memory by timestamp descending
            val sortedNotifications = notifications.sortedByDescending { it.timestamp.seconds }
            Resource.Success(sortedNotifications)
        } catch (e: Exception) {
            android.util.Log.e("NotificationDS", "Error fetching notifications: ${e.message}", e)
            Resource.Error(e.message ?: "Failed to fetch notifications")
        }
    }

    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark notification as read")
        }
    }

    suspend fun markAllAsRead(userId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all as read")
        }
    }

    suspend fun getUnreadCount(userId: String): Resource<Int> {
        return try {
            val snapshot = firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get unread count")
        }
    }

    suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }
}
