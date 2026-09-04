package com.appvendor.feature_dashboard.presentation

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.datastore.UserPreferences
import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_dashboard.data.remote.DashboardApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import android.content.Context
import android.media.RingtoneManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApiService,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var stompClient: StompClient? = null

    init {
        loadDashboard()
        setupWebSocket()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.userRoles.collect { roles ->
                _state.update { it.copy(userRoles = roles) }
            }
        }
        viewModelScope.launch {
            userPreferences.orderVisibilityStatuses.collect { statuses ->
                _state.update { it.copy(orderVisibilityStatuses = statuses) }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall { dashboardApi.getDashboard() }) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, dashboardData = result.data.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = safeApiCall { dashboardApi.getDashboard() }) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false, dashboardData = result.data.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isRefreshing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setupWebSocket() {
        viewModelScope.launch {
            val vendorId = userPreferences.userVendorId.firstOrNull() ?: return@launch
            val token = userPreferences.userToken.firstOrNull() ?: ""
            
            val baseUrl = com.appvendor.core.utils.Constants.BASE_URL
            val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "ws/websocket"
            
            val httpHeaders = mapOf("Authorization" to "Bearer $token")
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, httpHeaders)
            stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

            stompClient?.lifecycle()?.subscribe { event ->
                when (event.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        println("STOMP connection opened")
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        println("STOMP connection closed, attempting to reconnect...")
                        Thread.sleep(3000)
                        val reconnectToken = kotlinx.coroutines.runBlocking { userPreferences.userToken.firstOrNull() } ?: ""
                        val headers = listOf(ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer $reconnectToken"))
                        stompClient?.connect(headers)
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        println("STOMP connection error")
                        event.exception?.printStackTrace()
                    }
                    else -> {}
                }
            }

            val headers = listOf(ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer $token"))
            stompClient?.connect(headers)

            stompClient?.topic("/topic/vendor/$vendorId")?.subscribe({ stompMessage ->
                // When any order changes, auto-refresh the full dashboard
                val payload = stompMessage.payload ?: ""
                
                val isPending = payload.contains("PENDING", ignoreCase = true) || payload.contains("NEW_ORDER", ignoreCase = true)
                
                if (isPending) {
                    playNotificationSound()
                    showSystemNotification("New Order", "You have received a new pending order.", true)
                } else {
                    showSystemNotification("Order Update", "An order status has been updated.", false)
                }
                
                refreshOrders()
            }, { error ->
                error.printStackTrace()
            })
        }
    }

    private fun showSystemNotification(title: String, message: String, isPending: Boolean) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    com.appvendor.core.utils.Constants.NOTIFICATION_CHANNEL_ID,
                    com.appvendor.core.utils.Constants.NOTIFICATION_CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new orders"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val intent = android.content.Intent(context, com.appvendor.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val builder = androidx.core.app.NotificationCompat.Builder(context, com.appvendor.core.utils.Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(com.appvendor.R.mipmap.app_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            if (isPending) {
                builder.setSilent(true)
            }
                
            val notification = builder.build()
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playNotificationSound() {
        try {
            // Using TYPE_ALARM for a louder sound
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
            
            // Stop the alarm after 4 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                if (r.isPlaying) {
                    r.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stompClient?.disconnect()
    }
}
