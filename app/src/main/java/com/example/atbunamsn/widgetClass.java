package com.example.atbunamsn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by sherif146 on 10/02/2018.
 */

public class widgetClass extends AppWidgetProvider{
    private dbHelper dbHelper;
    public List<Product> notifyliste;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        dbHelper = new dbHelper(context);
        notifyliste = new ArrayList<>();
       // Toast.makeText(context,"Alarm Updated ",Toast.LENGTH_SHORT).show();
        Cursor cursornum = dbHelper.getAllUser();
        int no = cursornum.getCount();
        cursornum.close();
        notifyliste.clear();
        Random random = new Random();
        int searchid = random.nextInt(no - 0 + 1) + 0;
        if(searchid > 0){
            Cursor cursor = dbHelper.getDetails(dbColumnList.userDetails._ID,String.valueOf(searchid));
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Product contact = new Product();
                    String str = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_QOUTE));
                    contact.setDept(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_DEPT)));
                    contact.setName(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_NAME)));
                    contact.setLevel(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_LEVEL)));
                    contact.setEmail(str);
                    contact.setGender(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_GENDER)));
                    contact.setuserID(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID)));
                    Cursor cursor2 = dbHelper.searchForPics(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID)));
                    if(cursor2.getCount() >0){
                        cursor2.moveToFirst();
                        contact.setBLOB(cursor2.getBlob(cursor2.getColumnIndex(dbColumnList.userPics.COLUMN_PICS)));
                    }
                    notifyliste.add(contact);
                }
            }
            cursor.close();
        }

        if(notifyliste.size() ==1) {
            String userDept = notifyliste.get(0).getDept();
            String userName = notifyliste.get(0).getName();
            String userLevel = notifyliste.get(0).getLevel();
            String userID = notifyliste.get(0).getuserID();
            String str = notifyliste.get(0).getEmail();
            byte[] userPics = notifyliste.get(0).getBLOB();
            String str1 = str + "...";
            if (str.length() >=100) {
                //int k = str.length() / 2;
                str1 = str.substring(0, 100) + "...";
            }


            //build the actviity intents
            Intent intentnot = new Intent(context,viewNamsn.class);

            intentnot.putExtra("userName", userName);
            intentnot.putExtra("userLevel", userLevel);
            intentnot.putExtra("userDept", userDept);
            intentnot.putExtra("userID", userID);
            intentnot.putExtra("userPics", userPics);
            String prep = userName + System.getProperty("line.separator") + userLevel + " Level - " + userDept;
            intentnot.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            //intentnot.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent =  PendingIntent.getActivity(context,56,intentnot,PendingIntent.FLAG_UPDATE_CURRENT);

            for (int i = 0; i < appWidgetIds.length; i++) {
                int appWidgetId = appWidgetIds[i];
               // RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                RemoteViews notificationView = new RemoteViews(
                        context.getPackageName(),
                        R.layout.widget
                );
                notificationView.setRemoteAdapter(R.id.imagenotileft,intentnot);
                Bitmap bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
                // notificationView.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
                notificationView.setImageViewBitmap(R.id.imagenotileft,bitmap);
                // Locate and set the Text into customnotificationtext.xml TextViews
                notificationView.setTextViewText(R.id.title, (context.getString(R.string.title)));
                notificationView.setTextViewText(R.id.text, prep);
                notificationView.setTextViewText(R.id.qoute, str1);
                notificationView.setOnClickPendingIntent(R.id.imagenotileft,pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId,notificationView);
            }
        }
    }
}

