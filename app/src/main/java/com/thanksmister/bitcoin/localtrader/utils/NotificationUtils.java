/*
 * Copyright (c) 2015 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.thanksmister.bitcoin.localtrader.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.ui.activities.MainActivity;

import timber.log.Timber;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class NotificationUtils extends ContextWrapper
{
    public static int NOTIFICATION_ID = 1138;
    public static int BALANCE_NOTIFICATION_ID = 1975;
    public static int MESSAGE_NOTIFICATION_ID = 1976;
    public static int NOTIFICATION_ERROR_ID = 1925;
    
    public static int NOTIFICATION_TYPE_BALANCE = 4;
    public static int NOTIFICATION_TYPE_CONTACT = 5;
    public static int NOTIFICATION_TYPE_ADVERTISEMENT = 6;
    public static int NOTIFICATION_TYPE_MESSAGE = 7;
    public static int NOTIFICATION_TYPE_NOTIFICATION = 8;

    public static final String ANDROID_CHANNEL_ID = "com.thanksmister.bitcoin.localtrader.ANDROID";
    public static String ANDROID_CHANNEL_NAME;
    private NotificationManager notificationManager;
    private final PendingIntent pendingIntent;
    private final Intent notificationIntent;

    public NotificationUtils(Context context) {
        super(context);
        ANDROID_CHANNEL_NAME = "LocalBitcoins Notification";
        notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        String description = "LocalBitcoins Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        getManager().createNotificationChannel(mChannel);
    }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    public void createNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder nb = getAndroidChannelNotification(title, message);
            getManager().notify(NOTIFICATION_ID, nb.build());
        } else {
            NotificationCompat.Builder nb = getAndroidNotification(title, message);
            // This ensures that navigating backward from the Activity leads out of your app to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notificationIntent);
            nb.setContentIntent(pendingIntent);
            getManager().notify(NOTIFICATION_ID, nb.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        final int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        Notification.Builder builder =  new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(color)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setAutoCancel(true);

        builder.setContentIntent(pendingIntent);
        return builder;
    }

    public NotificationCompat.Builder getAndroidNotification(String title, String body) {
        final int color = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        return new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setPriority(PRIORITY_MAX)
                .setVisibility(VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setColor(color)
                .setAutoCancel(true);
    }

    public void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }


    // Old Notification Methods
    @Deprecated
    public static void createNotification(Context context, String ticker, String title, String message, int type, String notification_id)
    {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, type);
        if(notification_id != null) {
            notificationIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_ID, notification_id);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setTicker(ticker);
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS); // requires VIBRATE permission
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL| Notification.FLAG_SHOW_LIGHTS;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(MESSAGE_NOTIFICATION_ID, notification);
    }

    @Deprecated
    public static void createBalanceNotification(Context context, String ticker, String title, String message, int type, String contact_id)
    {
        Timber.d("Create Balance Notification!!");
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        notificationIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, type);
        if(type == NOTIFICATION_TYPE_BALANCE && contact_id != null) {
            notificationIntent.putExtra(MainActivity.EXTRA_CONTACT, contact_id);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setTicker(ticker);
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            int resource =  R.mipmap.ic_launcher;
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resource);
            Resources res = context.getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            // bm = Bitmap.createScaledBitmap(bm, width, height, false);
            //builder.setLargeIcon(bm);
        }*/

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS); // requires VIBRATE permission
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL| Notification.FLAG_SHOW_LIGHTS;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(BALANCE_NOTIFICATION_ID, notification);
    }

    @Deprecated
    public static void createMessageNotification(Context context, String ticker, String title, String message, int type, String contact_id)
    {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        notificationIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, type);
        if(type == NOTIFICATION_TYPE_MESSAGE && contact_id != null) {
            notificationIntent.putExtra(MainActivity.EXTRA_CONTACT, contact_id);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setTicker(ticker);
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            int resource =  R.mipmap.ic_launcher;
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resource);
            Resources res = context.getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            //bm = Bitmap.createScaledBitmap(bm, width, height, false);
            //builder.setLargeIcon(bm);
        }*/
        
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS); // requires VIBRATE permission
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL| Notification.FLAG_SHOW_LIGHTS;
        
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(MESSAGE_NOTIFICATION_ID, notification);
    }
}
