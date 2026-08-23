package com.appvendor.feature_reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.feature_reports.presentation.components.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = datePickerState.selectedStartDateMillis
                    val endMillis = datePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC"))
                        val start = formatter.format(Instant.ofEpochMilli(startMillis))
                        val end = formatter.format(Instant.ofEpochMilli(endMillis))
                        viewModel.updateDateRange(start, end)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                title = { Text("Select date range", modifier = Modifier.padding(16.dp)) }
            )
        }
    }

    Scaffold(
        snackbarHost = {
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.dismissError() }) { Text("Dismiss") } }
                ) { Text(state.error!!) }
            }
        },
        containerColor = Color(0xFF0B0F0D),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp) // Major section breaks
            ) {
                item {
                    ReportsHeader()
                }

                item {
                    DateRangeSelector(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onClick = { showDatePicker = true }
                    )
                }

                val todayReport = state.todayReport
                val summaryReport = state.summaryReport

                if (state.isLoadingToday && todayReport == null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (todayReport != null) {
                    val isEmptyDay = todayReport.totalRevenue == 0.0 && todayReport.totalOrders == 0 && todayReport.completedOrders == 0
                    
                    if (isEmptyDay) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your day is just getting started.\nNo orders yet today.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        item {
                            TodaySnapshot(report = todayReport)
                        }
                        item {
                            // Extract active orders from summary report if available, else from today
                            // Since today report doesn't have a map of statuses, we rely on summary report's byStatus
                            val byStatus = summaryReport?.orderReport?.byStatus ?: mapOf(
                                "PENDING" to todayReport.pendingOrders,
                                "COMPLETED" to todayReport.completedOrders
                            )
                            OrderActivityCard(byStatus = byStatus)
                        }
                    }
                }

                if (state.isLoadingSummary && summaryReport == null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (summaryReport != null) {
                    item {
                        PeakHoursCard(hours = summaryReport.peakHourReport)
                    }

                    item {
                        TopItemsList(items = summaryReport.popularItemReport)
                    }

                    item {
                        QueuePerformanceCard(stats = summaryReport.queueStatsReport)
                    }

                    item {
                        InsightsSection(report = summaryReport)
                    }
                }
            }
        }
    }
}
