package com.example.atbunamsn;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sherif146 on 27/01/2018.
 */

public class myAlarmReceiver extends BroadcastReceiver {
    Context context;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, reminderService.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        int reminderIntervalMin = 40;
        int reminderIntervalSec = (int)(TimeUnit.MINUTES.toMillis(reminderIntervalMin));
        int syncTime = reminderIntervalSec;
       // manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime()+300, 300*1000, pendingIntent);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, reminderIntervalSec, reminderIntervalSec + syncTime, pendingIntent);
    }
}
