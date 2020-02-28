package com.example.atbunamsn;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by sherif146 on 12/01/2018.
 */

public class myBackgroundService extends Service {
    URLConnection urlconnection;
    URL url;
    String address = "http://192.168.230.1/androidReg/androidNamsnService.php";
    private SQLiteDatabase mDb;
    private dbHelper dbHelper;
    String comingNews;
    private CountDownTimer timer,timer2;
    public List<Product> arraylisted,notifyliste;
    int numItem = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new dbHelper(getApplicationContext());
        arraylisted = new ArrayList<>();
        notifyliste = new ArrayList<>();
        //verify once networ is change - wifi or sim data
        address = "http://192.168.230.1/androidReg/androidNamsnService.php";
        //new testConnection().execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
       /** timer2 = new CountDownTimer(300000, 20) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                try{
                    new remindAlluser().execute();
                }catch(Exception e){
                    // Log.e("Error", "Error: " + e.toString());
                }
            }
        }.start();**/

        final Handler hndl = new Handler();
        Timer tim = new Timer();

        //for frequent alert
    /**    TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                hndl.post(new Runnable() {
                    @Override
                    public void run() {
                        new remindAlluser().execute();
                    }
                });
            }
        };**/

        //for updating database
        TimerTask doUpdateAsyncTask = new TimerTask() {
            @Override
            public void run() {
                hndl.post(new Runnable() {
                    @Override
                    public void run() {
                        //new testConnection().execute();
                    }
                });
            }
        };

      //  tim.schedule(doAsyncTask,0,180000);
        tim.schedule(doUpdateAsyncTask,0,300000);

        //start at first
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // new testConnection().execute();
            }
        },1000);
        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void showMessage(){
        final Random random = new Random();
        int i = random.nextInt(2 - 0 + 1) + 0;
        Toast.makeText(getApplicationContext(),"Service Started at - "+ String.valueOf(i) + " Time",Toast.LENGTH_SHORT).show();
        timer.start();
    }
    class testConnection extends AsyncTask<String, Integer, String> {
        String outre;
        @Override
        protected String doInBackground(String... strings) {
            try{
                url = new URL(address);
                urlconnection = url.openConnection();
                urlconnection.setConnectTimeout(1500);
                urlconnection.connect();
                outre = "true";
                return outre;
            } catch (Exception e) {
                outre = "false";
                return outre;
            }
            //return null;
        }

        @Override
        protected void onPostExecute(String content) {
            if(outre.equals("true")) {
                volleyJsonArrayRequest(address);
            }
        }
    }

    //reminder profile ends


    public void volleyJsonArrayRequest(String url) {
        String REQUEST_TAG = "com.volley.volleyJsonArrayRequestService";
        //details
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        comingNews = response;
                        volleyFriendRequest(address);
                        new loadDetails().execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "details");
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }

    public void volleyFriendRequest(String url) {
        //friends
        String REQUEST_FRIEND = "com.volley.volleyJsonArrayRequestFriend";
        StringRequest postFriends = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadFriends(response);
                        volleyLecturerRequest(address);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "friend");
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postFriends, REQUEST_FRIEND);
    }
    public void volleyLecturerRequest(String url) {
        //lecturer
        String REQUEST_LECTURER = "com.volley.volleyJsonArrayRequestLecturer";
        StringRequest postlecturer = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadLecturer(response);
                        volleyPhoneRequest(address);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "lecturer");
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postlecturer, REQUEST_LECTURER);
    }

    public void volleyPhoneRequest(String url) {
        //PHONE
        String  REQUEST_PHONE= "com.volley.volleyJsonArrayRequestPhone";
        StringRequest postPhone = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        loadPhone(response);
                        volleyCourseRequest(address);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "phone");
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postPhone, REQUEST_PHONE);
    }
    public void volleyCourseRequest(String url) {
        //COURSE
        String REQUEST_COURSE = "com.volley.volleyJsonArrayRequestCourse";
        StringRequest postCourse = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadCourse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "course");
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postCourse, REQUEST_COURSE);
    }



    class loadDetails extends  AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {
            numItem =0;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jsonobject = null;
                JSONArray jsonarray = null;
                jsonarray = new JSONArray(comingNews);
                arraylisted.clear();
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);
                    Cursor cursor = dbHelper.getDetails(dbColumnList.userDetails.COLUMN_USERID,jsonobject.getString("userId"));
                    if (cursor.getCount()>0) {
                        //IT EXIST THEN UPDATE
                        String MyPics = jsonobject.getString("profile");
                        Bitmap bitmap =  Glide.with(getApplicationContext()).load(MyPics).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        Cursor cursor2 = dbHelper.searchForPics(jsonobject.getString("userId"));
                        if(cursor2.getCount() >0){
                            dbHelper.updateForPics(jsonobject.getString("userId"),byteArray);
                        }else{
                            dbHelper.insertForPics(jsonobject.getString("userId"),byteArray);
                        }
                        cursor2.close();
                        /**dbHelper.updateDetails(jsonobject.getString("userDept"),jsonobject.getString("userBestMoment"),jsonobject.getString("userCLgov"),
                         jsonobject.getString("userCPermAdd"),jsonobject.getString("userCState"),jsonobject.getString("userEmail"),jsonobject.getString("userGender"),
                         jsonobject.getString("userLevel"),jsonobject.getString("userLgov"),jsonobject.getString("fullName"),jsonobject.getString("userPermAdd"),
                         jsonobject.getString("userQoute"),jsonobject.getString("userRegNo"),jsonobject.getString("userReligion"),jsonobject.getString("userId"),jsonobject.getString("userState"),
                         byteArray);**/
                        dbHelper.updateDetails(jsonobject.getString("userDept"),jsonobject.getString("userBestMoment"),jsonobject.getString("userCLgov"),
                                jsonobject.getString("userCPermAdd"),jsonobject.getString("userCState"),jsonobject.getString("userEmail"),jsonobject.getString("userGender"),
                                jsonobject.getString("userLevel"),jsonobject.getString("userLgov"),jsonobject.getString("fullName"),jsonobject.getString("userPermAdd"),
                                jsonobject.getString("userQoute"),jsonobject.getString("userRegNo"),jsonobject.getString("userReligion"),jsonobject.getString("userId"),
                                jsonobject.getString("userState"),jsonobject.getString("password"));
                    }else {
                        //INSERT AS NEW RECORD
                        Product contact = new Product();

                        String MyPics = jsonobject.getString("profile");
                        Bitmap bitmap =  Glide.with(getApplicationContext()).load(MyPics).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        dbHelper.addNewDetails(jsonobject.getString("userDept"),jsonobject.getString("userBestMoment"),jsonobject.getString("userCLgov"),
                                jsonobject.getString("userCPermAdd"),jsonobject.getString("userCState"),jsonobject.getString("userEmail"),jsonobject.getString("userGender"),
                                jsonobject.getString("userLevel"),jsonobject.getString("userLgov"),jsonobject.getString("fullName"),jsonobject.getString("userPermAdd"),
                                jsonobject.getString("userQoute"),jsonobject.getString("userRegNo"),jsonobject.getString("userReligion"),jsonobject.getString("userId"),
                                jsonobject.getString("userState"),jsonobject.getString("password"));
                        //create notification contents
                        numItem = numItem + 1;
                        contact.setDept(jsonobject.getString("userDept"));
                        contact.setLevel(jsonobject.getString("userLevel"));
                        contact.setName(jsonobject.getString("fullName"));
                        contact.setuserID(jsonobject.getString("userId"));
                        contact.setBLOB(byteArray);
                        arraylisted.add(contact);
                    }
                    cursor.close();
                }
                //cursor.close();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //determine the notification type to use
           // Toast.makeText(getApplicationContext(),String.valueOf(numItem) + " - Selected !!",Toast.LENGTH_LONG).show();
            if(numItem > 1){
                buildNotificationMany();
            }
            if(numItem == 1){
                buildNotificationSingle();
            }
        }
    }
    //load friends
    public void loadFriends(String comingNews) {
        try {
            JSONObject jsonobject = null;
            JSONArray jsonarray = null;
            jsonarray = new JSONArray(comingNews);
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                Cursor cursor = dbHelper.getAllMultiple(dbColumnList.userBest.TABLE_NAME,dbColumnList.userBest.COLUMN_USERID,
                        dbColumnList.userBest.COLUMN_BESTFRIEND, jsonobject.getString("userId"),jsonobject.getString("bestFriend"));
                if (cursor.getCount()>0) {
                    //IT EXIST THEN UPDATE
                    cursor.moveToFirst();
                    long id = cursor.getLong(cursor.getColumnIndex(dbColumnList.userBest._ID));
                    dbHelper.updateForAll(dbColumnList.userBest.TABLE_NAME,dbColumnList.userBest.COLUMN_BESTFRIEND,jsonobject.getString("bestFriend"),
                            dbColumnList.userBest._ID, String.valueOf(id));
                }else {
                    //INSERT AS NEW RECORD
                    dbHelper.insertForAll(dbColumnList.userBest.TABLE_NAME,dbColumnList.userBest.COLUMN_BESTFRIEND,dbColumnList.userBest.COLUMN_USERID,
                            jsonobject.getString("bestFriend"),jsonobject.getString("userId"));
                }
                cursor.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // loadLecturer
    public void loadLecturer(String comingNews) {
        try {
            JSONObject jsonobject = null;
            JSONArray jsonarray = null;
            jsonarray = new JSONArray(comingNews);
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                Cursor cursor = dbHelper.getAllMultiple(dbColumnList.userLecturer.TABLE_NAME,dbColumnList.userLecturer.COLUMN_USERID,
                        dbColumnList.userLecturer.COLUMN_BESTLECTURER, jsonobject.getString("userId"),jsonobject.getString("bestLecturer"));
                if (cursor.getCount()>0) {
                    //IT EXIST THEN UPDATE
                    cursor.moveToFirst();
                    long id = cursor.getLong(cursor.getColumnIndex(dbColumnList.userLecturer._ID));

                    dbHelper.updateForAll(dbColumnList.userLecturer.TABLE_NAME,dbColumnList.userLecturer.COLUMN_BESTLECTURER,jsonobject.getString("bestLecturer"),
                            dbColumnList.userLecturer._ID, String.valueOf(id));
                }else {
                    //INSERT AS NEW RECORD
                    dbHelper.insertForAll(dbColumnList.userLecturer.TABLE_NAME,dbColumnList.userLecturer.COLUMN_BESTLECTURER,dbColumnList.userLecturer.COLUMN_USERID,
                            jsonobject.getString("bestLecturer"),jsonobject.getString("userId"));
                }
                cursor.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //bestCourses
    public void loadCourse(String comingNews) {
        try {
            JSONObject jsonobject = null;
            JSONArray jsonarray = null;
            jsonarray = new JSONArray(comingNews);
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                Cursor cursor = dbHelper.getAllMultiple(dbColumnList.userCourse.TABLE_NAME,dbColumnList.userCourse.COLUMN_USERID,
                        dbColumnList.userCourse.COLUMN_BESTCOURSE, jsonobject.getString("userId"),jsonobject.getString("bestCourses"));
                if (cursor.getCount()>0) {
                    //IT EXIST THEN UPDATE
                    cursor.moveToFirst();
                    long id = cursor.getLong(cursor.getColumnIndex(dbColumnList.userLecturer._ID));

                    dbHelper.updateForAll(dbColumnList.userCourse.TABLE_NAME,dbColumnList.userCourse.COLUMN_BESTCOURSE,jsonobject.getString("bestCourses"),
                            dbColumnList.userCourse._ID, String.valueOf(id));
                }else {
                    //INSERT AS NEW RECORD
                    dbHelper.insertForAll(dbColumnList.userCourse.TABLE_NAME,dbColumnList.userCourse.COLUMN_BESTCOURSE,dbColumnList.userCourse.COLUMN_USERID,
                            jsonobject.getString("bestCourses"),jsonobject.getString("userId"));
                }
                cursor.close();
            }
            //restar the connection
//            timer.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //phones
    //bestCourses
    public void loadPhone(String comingNews) {
        try {
            JSONObject jsonobject = null;
            JSONArray jsonarray = null;
            jsonarray = new JSONArray(comingNews);
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                Cursor cursor = dbHelper.getAllMultiple(dbColumnList.userPhone.TABLE_NAME,dbColumnList.userPhone.COLUMN_USERID,
                        dbColumnList.userPhone.COLUMN_PHONE, jsonobject.getString("userId"),jsonobject.getString("phone"));
                if (cursor.getCount()>0) {
                    //IT EXIST THEN UPDATE
                    cursor.moveToFirst();
                    long id = cursor.getLong(cursor.getColumnIndex(dbColumnList.userLecturer._ID));

                    dbHelper.updateForAll(dbColumnList.userPhone.TABLE_NAME,dbColumnList.userPhone.COLUMN_PHONE,jsonobject.getString("phone"),
                            dbColumnList.userPhone._ID, String.valueOf(id));
                }else {
                    //INSERT AS NEW RECORD
                    dbHelper.insertForAll(dbColumnList.userPhone.TABLE_NAME,dbColumnList.userPhone.COLUMN_PHONE,dbColumnList.userPhone.COLUMN_USERID,
                            jsonobject.getString("phone"),jsonobject.getString("userId"));
                }
                cursor.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //my all single alerNotification classes
    public void buildNotificationSingle(){
        if(arraylisted.size() ==1){
            Product contact = arraylisted.get(0);

            String userDept = arraylisted.get(0).getDept();
            String userName = arraylisted.get(0).getName();
            String userLevel = arraylisted.get(0).getLevel();
            String userID = arraylisted.get(0).getuserID();
            String str  = notifyliste.get(0).getEmail();
            if(str.length() >=200){
                int k = str.length() / 2;
                str = str.substring(0,k) + " ...";
            }
            String prep = userName + System.getProperty("line.separator") + userLevel + " Level - " + userDept;
            byte[] userPics = arraylisted.get(0).getBLOB();

            //build the actviity intents
            Intent notificationIntent = new Intent(getApplicationContext(),viewNamsn.class);

            notificationIntent.putExtra("userName", userName);
            notificationIntent.putExtra("userLevel", userLevel);
            notificationIntent.putExtra("userDept", userDept);
            notificationIntent.putExtra("userID", userID);
            notificationIntent.putExtra("userPics", userPics);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pend =  PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            //mBuilder = buildNotification(userPics,prep);
            //design the new custom view
            RemoteViews notificationView = new RemoteViews(
                    getApplicationContext().getPackageName(),
                    R.layout.activity_custom_notification
            );
            Bitmap bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
            // notificationView.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
            notificationView.setImageViewBitmap(R.id.imagenotileft,bitmap);
            // Locate and set the Text into customnotificationtext.xml TextViews
            notificationView.setTextViewText(R.id.title, (getString(R.string.app_name)));
            notificationView.setTextViewText(R.id.text, prep);
           // notificationView.setTextViewText(R.id.qoute, str);

            //go and build the notification
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setContentIntent(pend);
            mBuilder.setContentTitle(getString(R.string.app_name));
            mBuilder.setContentText(userName + " Recently Joined ATBU 37 NAMSSN ELITE E-YEAR BOOK !!!");
            mBuilder.setTicker(userName + " Recently Joined ATBU 37 NAMSSN ELITE E-YEAR BOOK !!!");
            mBuilder.setAutoCancel(true);
            mBuilder.setContent(notificationView);
            mBuilder.setSmallIcon(R.mipmap.ic_app);
            mBuilder.setPriority(2);
            Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmsound);
            mBuilder.setLights(Color.RED, 3000, 3000);
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000 });
            //display the notification

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0123,mBuilder.build());
        }
       // arraylisted.clear();
    }

    //frequent single alert
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
            Intent notificationIntent = new Intent(getApplicationContext(),viewNamsn.class);

            notificationIntent.putExtra("userName", userName);
            notificationIntent.putExtra("userLevel", userLevel);
            notificationIntent.putExtra("userDept", userDept);
            notificationIntent.putExtra("userID", userID);
            notificationIntent.putExtra("userPics", userPics);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pend =  PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            //mBuilder = buildNotification(userPics,prep);
            //design the new custom view
            RemoteViews notificationView = new RemoteViews(
                    getApplicationContext().getPackageName(),
                    R.layout.activity_custom_notification
            );
            Bitmap bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
            // notificationView.setImageViewResource(R.id.imagenotileft,R.mipmap.ic_launcher);
            notificationView.setImageViewBitmap(R.id.imagenotileft,bitmap);
            // Locate and set the Text into customnotificationtext.xml TextViews
            notificationView.setTextViewText(R.id.title, (getString(R.string.app_name)));
            notificationView.setTextViewText(R.id.text, prep);
           // notificationView.setTextViewText(R.id.qoute, str);

            //go and build the notification
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(getApplicationContext());

            Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmsound);
            mBuilder.setLights(Color.RED, 3000, 3000);
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000 });

            mBuilder.setContentIntent(pend);
            mBuilder.setContentTitle(getString(R.string.app_name));
            mBuilder.setContentText("Click To View - " + userName + " Profile !!!");
            mBuilder.setTicker("Click To View - " + userName + " Profile !!!");
            mBuilder.setAutoCancel(true);
            mBuilder.setContent(notificationView);
            mBuilder.setSmallIcon(R.mipmap.ic_app);
            mBuilder.setPriority(2);
            //display the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1992,mBuilder.build());

            //start the frequent timer
          //  timer2.start();
        }
    }

    //many
    public void buildNotificationMany(){
        //the one that start the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //the builder that structure the notification
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(getApplicationContext());

        //the intent or activity the notification will respond to
        Intent notificationIntent = new Intent(getApplicationContext(),homeView.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pend =  PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        String prep = String.valueOf(numItem)  + " Student's Recently Join the ATBU NAMSSN 37 ELITES E-YEAR BOOK !!" + System.getProperty("line.separator") +
                " Click To View Their Profiles";

        Uri alarmsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmsound);
        mBuilder.setLights(Color.RED, 3000, 3000);
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000 });

        mBuilder.setContentIntent(pend);
        mBuilder.setContentTitle(getString(R.string.app_name));
        mBuilder.setContentText(prep);
        mBuilder.setTicker(prep);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.mipmap.ic_app);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        int k = 0;
        for (int i=0; i < arraylisted.size(); i++) {
            if(i< 10) {
                String prepLine = String.valueOf(i + 1) + ". " +
                        arraylisted.get(i).getName() + " - " + arraylisted.get(i).getLevel() + " Level - " + arraylisted.get(i).getDept();
                inboxStyle.addLine(prepLine);
            }else{
                k = k + 1;
            }
        }
        if(k>0){
            String left = "And "+String.valueOf(k)+ " Other Student's Recently Join the ATBU NAMSSN 37 ELITES E-YEAR BOOK !!" + System.getProperty("line.separator") +
                    " Click To View Their Profiles";
            inboxStyle.addLine(left);
        }
        mBuilder.setStyle(inboxStyle);
        notificationManager.notify(84437,mBuilder.build());
       // arraylisted.clear();
    }
}
