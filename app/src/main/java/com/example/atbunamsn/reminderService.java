package com.example.atbunamsn;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sherif146 on 01/02/2018.
 */

public class reminderService extends BroadcastReceiver {
    private dbHelper dbHelper;
    public List<Product> notifyliste;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        dbHelper = new dbHelper(context);
        notifyliste = new ArrayList<>();
        this.context = context;
        new remindAlluser().execute();
    }


    //user profile reminder task
    class remindAlluser extends AsyncTask<String, Integer, String> {
        String outre;
        @Override
        protected String doInBackground(String... strings) {
            //get the no of users in the sqlite server
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
                        if(str.length() >=200){
                            int k = str.length() / 2;
                            str = str.substring(0,k) + " ...";
                        }
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
            return null;
        }

        @Override
        protected void onPostExecute(String content) {
            if(notifyliste.size()>0) {
                buildNotificationSingleAlert();
            }
        }
    }


    public void buildNotificationSingleAlert(){
        if(notifyliste.size() ==1){
            Product contact = notifyliste.get(0);

            String userDept = notifyliste.get(0).getDept();
            String userName = notifyliste.get(0).getName();
            String userLevel = notifyliste.get(0).getLevel();
            String userID = notifyliste.get(0).getuserID();
            String str  = notifyliste.get(0).getEmail();
            if(str.length() >=200){
                int k = str.length() / 2;
                str = str.substring(0,k) + " ...";
            }
            String prep = userName + System.getProperty("line.separator") + userLevel + " Level - " + userDept;
            byte[] userPics = notifyliste.get(0).getBLOB();

            //build the actviity intents
            Intent notificationIntent = new Intent(context,viewNamsn.class);

            notificationIntent.putExtra("userName", userName);
            notificationIntent.putExtra("userLevel", userLevel);
            notificationIntent.putExtra("userDept", userDept);
            notificationIntent.putExtra("userID", userID);
            notificationIntent.putExtra("userPics", userPics);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pend =  PendingIntent.getActivity(context,55,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);



            Bitmap bitmap= BitmapFactory.decodeByteArray(userPics,0,userPics.length);
            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, 100, 100, false);

            // notificationView.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
            //intent for phone action
            String phone_id="";
            Cursor cursorp = dbHelper.getAllSingle(dbColumnList.userPhone.TABLE_NAME,dbColumnList.userPhone.COLUMN_USERID,userID);
            if(cursorp.getCount()>0){
                cursorp.moveToFirst();
                phone_id = cursorp.getString(cursorp.getColumnIndex(dbColumnList.userPhone.COLUMN_PHONE));
            }else{
                phone_id = "08164377187";
            }
            cursorp.close();
            //get the phone

            //String phone_id = "tel:" + arraylist.get(position).getPhone();
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" +phone_id));
            if (callIntent.resolveActivity(context.getPackageManager()) != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context,"Phone Not Granted",Toast.LENGTH_SHORT).show();
                    return;
                }
                // context.startActivity(callIntent);
            }
            PendingIntent pendCall =  PendingIntent.getActivity(context,53,callIntent,PendingIntent.FLAG_UPDATE_CURRENT);


            //String phone_id = arraylist.get(position).getPhone();
            Uri smsUri = Uri.parse("tel:" +phone_id);
            Intent intentMsg = new Intent(Intent.ACTION_VIEW, smsUri);
            intentMsg.putExtra("address", phone_id);
            intentMsg.putExtra("sms_body", "");
            intentMsg.setType("vnd.android-dir/mms-sms");
            if (intentMsg.resolveActivity(context.getPackageManager()) != null) {
                //context.startActivity(intentMsg);
            }
            PendingIntent pendMsg =  PendingIntent.getActivity(context,35,intentMsg,PendingIntent.FLAG_UPDATE_CURRENT);


            //mBuilder = buildNotification(userPics,prep);
            //design the new custom view
           /** RemoteViews notificationView = new RemoteViews(
                    context.getPackageName(),
                    R.layout.activity_custom_notification
            );
            Bitmap bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
            // notificationView.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
            notificationView.setImageViewBitmap(R.id.imagenotileft,bitmap);
            // Locate and set the Text into customnotificationtext.xml TextViews
            notificationView.setTextViewText(R.id.title, (context.getString(R.string.app_name)));
            notificationView.setTextViewText(R.id.text, prep);
            // notificationView.setTextViewText(R.id.qoute, str);**/

            //go and build the notification
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(context);

            Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmsound);
            mBuilder.setLights(Color.RED, 3000, 3000);
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000 });
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            mBuilder.setContentIntent(pend);
            mBuilder.setContentTitle(context.getString(R.string.app_name));
            mBuilder.setContentText(prep);
            mBuilder.setTicker("Click To View - " + userName + " Profile !!!");
            mBuilder.setAutoCancel(true);
           // mBuilder.setContent(notificationView); - for custom Notification
            mBuilder.setSmallIcon(R.mipmap.ic_app);
            mBuilder.setLargeIcon(resizedBitmap);
            mBuilder.addAction(R.drawable.ic_message,"Message",pendMsg);
            mBuilder.addAction(R.drawable.ic_call,"Call",pendCall);
            mBuilder.addAction(R.drawable.ic_alluser_not,"View",pend);
            mBuilder.setPriority(0);
           // mBuilder.addAction(R.drawable.ic_alluser,"Message",pendMsg);
            //mBuilder.addAction(R.drawable.ic_alluser,"Call",pendCall);
            //mBuilder.addAction(R.drawable.ic_alluser,"View",pend);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mBuilder.setPriority(Notification.PRIORITY_HIGH|Notification.PRIORITY_MAX);
            }
            //display the notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1992,mBuilder.build());

            //start the frequent timer
            //  timer2.start();
        }
    }
}