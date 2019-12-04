package com.example.location.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.location.*
import com.example.location.data.enum.Actions
import com.example.location.data.model.LocationModel
import java.text.DateFormat
import java.util.*

fun buildNotification(context: Context, model: LocationModel?): Notification {

    val intent = Intent(context, LocationService::class.java)
    intent.action = Actions.STOP.name
    val pendingIntent =
        PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    val text: CharSequence = getLocationText(context, model)
    val activityPendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java), 0
    )
    val servicePendingIntent = PendingIntent.getService(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    return NotificationCompat.Builder(context, CHANNEL_ID)
        .addAction(
            R.drawable.ic_launch, context.getString(R.string.launch_activity),
            activityPendingIntent
        )
        .addAction(
            R.drawable.ic_close, context.getString(R.string.action_stop),
            servicePendingIntent
        )
        .setAutoCancel(true)
        .setContentText(text)
        .setContentIntent(pendingIntent)
        .setContentTitle(getLocationTitle(context))
        .setSmallIcon(R.drawable.ic_notification)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()

}

fun updateNotification(
    manager: NotificationManager?, notification: Notification
) {
    manager?.addToNotificationManager(
        notification = notification
    )

}

fun NotificationManager.addToNotificationManager(
    notification: Notification
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Channel"
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        createNotificationChannel(channel)
    }

    notify(NOTIFICATION_ID, notification)
}

fun getLocationTitle(context: Context): String {
    return context.getString(
        R.string.location_updated,
        DateFormat.getDateTimeInstance().format(Date())
    )
}

fun getLocationText(context: Context, location: LocationModel?): String {
    return context.getString(R.string.current_location, location?.latitude, location?.longitude)
}