package com.appvendor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.appvendor.core.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeQueueFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this new token to your backend if it refreshes while logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Extract the data payload sent from our Spring Boot backend
        val data = message.data
        if (data.isNotEmpty()) {
            val type = data["type"] // e.g., "ORDER_UPDATE"
            val orderId = data["orderId"]
            val status = data["status"] // e.g., "PREPARING", "READY", "PENDING"
            val queueNumber = data["queueNumber"] ?: ""

            // Display sound only for pending orders
            val isPending = status != null && status.equals("PENDING", ignoreCase = true)
            // Display sound only for pending orders
            if (isPending) {
                playNotificationSound()
            }

            val title = if (isPending) "New Order" else "Order Update"
            val body = if (isPending) 
                "You have received a new pending order (Order $queueNumber)."
            else 
                "Order $queueNumber is now $status!"

            showNotification(title, body, orderId, isPending)
        }
    }

    private fun showNotification(title: String, body: String, orderId: String?, isPending: Boolean) {
        val channelId = Constants.NOTIFICATION_CHANNEL_ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, Constants.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(this, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (orderId != null) {
                action = android.content.Intent.ACTION_VIEW
                data = android.net.Uri.parse("appvendor://order_detail/$orderId")
            }
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.app_icon) // Using the app icon
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            
        // If it's a pending order, we only want the custom alarm sound to play, not the default ding dong.
        if (isPending) {
            builder.setSilent(true)
        }

        val notification = builder.build()

        // Use a unique ID (or hash the orderId) so multiple notifications stack properly
        val notificationId = orderId?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun playNotificationSound() {
        try {
            // Using TYPE_ALARM for a louder sound
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val r = RingtoneManager.getRingtone(applicationContext, notificationUri)
            r.play()

            // Stop the alarm after 4 seconds
            CoroutineScope(Dispatchers.Main).launch {
                delay(4000)
                if (r.isPlaying) {
                    r.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
