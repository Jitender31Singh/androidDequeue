package com.appvendor.core.utils

/**
 * Global constants for the AppVendor application.
 */
object Constants {
//    const val BASE_URL = "http://192.168.31.95:8080/"
//    const val BASE_URL = "https://dequeue-qofo.onrender.com/"

    const val BASE_URL = "https://dequeue-production.up.railway.app/"

    // API timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val STARTING_PAGE_INDEX = 1

    // Intent Extras
    const val EXTRA_VENDOR_ID = "extra_vendor_id"
    const val EXTRA_ORDER_ID = "extra_order_id"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "appvendor_channel_id"
    const val NOTIFICATION_CHANNEL_NAME = "AppVendor Notifications"
}
