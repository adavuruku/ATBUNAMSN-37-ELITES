package com.example.atbunamsn;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginScreen extends AppCompatActivity {
Button login;
TextView userid, password;
    byte[] byteArray;
    ProgressDialog pd;
    AlertDialog.Builder builder;
    URLConnection urlconnection;
    String allResult,userName, userPassword,et1,et2,et3,et4,et5,et6,et7,type;
    URL url;
    String address = "http://192.168.230.1/androidReg/androidNamsn.php";
    SharedPreferences MyName,MyDept,MyId,MyLevel,MyPics,MyPassword,MyQoute;
    private dbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        userid = (TextView) findViewById(R.id.userid);
        password = (TextView) findViewById(R.id.password);
        login = (Button) findViewById(R.id.btnSave);

        //initialize shared preferences
        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyId = this.getSharedPreferences("MyId", this.MODE_PRIVATE);
        MyLevel = this.getSharedPreferences("MyLevel", this.MODE_PRIVATE);
        MyPics = this.getSharedPreferences("MyPics", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);
        MyQoute = this.getSharedPreferences("MyQoute", this.MODE_PRIVATE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = userid.getText().toString().trim();
                userPassword = password.getText().toString().trim();
                if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPassword)){
                    displayMessage("Invalid Data's Provided - Please Verify");
                }else{
                    new testConnection().execute();
                }
            }
        });


        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Processing Request ...");
        pd.setTitle(R.string.app_name);
        pd.setIcon(R.mipmap.ic_app);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
    }
public void displayMessage(String msg){
    pd.hide();
    builder = new AlertDialog.Builder(this);
    builder.setMessage(msg);
    builder.setTitle(R.string.app_name);
    builder.setIcon(R.mipmap.ic_app);
    builder.setCancelable(false);
    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
        }
    });
    AlertDialog alert = builder.create();
    alert.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
    alert.show();
}
    class testConnection extends AsyncTask<String, Integer, String> {
        String outre;
        @Override
        protected String doInBackground(String... strings) {
            try{
                /** url = new URL(address);
                 urlconnection = url.openConnection();
                 urlconnection.setConnectTimeout(1000);
                 urlconnection.connect();
                 outre = "true";**/
                outre = "false";
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
                // new ReadJSON().execute();
                pd.show();
                volleyJsonArrayRequest(address);
            }
            else{
               // pd.hide();
                new LoginLocal().execute();
                //displayMessage("Error: No Internet Connection !!!");
            }
        }
    }


    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        //pd.hide();
                        try {
                            JSONObject jsonobject = new JSONObject(response);
                            String error = jsonobject.getString("Error");
                            if(error.equals("Error: Wrong Username Or Password !!!")){
                                new LoginLocal().execute();
                               // displayMessage("Error: Wrong Username Or Password !!!");
                            }else{
                                type= "online";
                                allResult = response;
                                new ReadJSON().execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            new LoginLocal().execute();
                            //displayMessage("Error: No Internet Connection !!!");
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                      //  pd.hide();
                        displayMessage("Error: No Internet Connection !!!");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "login");
                params.put("userID", userName);
                params.put("userPassword", userPassword);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }

    //login locally
    class LoginLocal extends AsyncTask<String, Integer, String> {
        String outre1;
        @Override
        protected String doInBackground(String... strings) {
            dbHelper = new dbHelper(getApplicationContext());
            Cursor cursor = dbHelper.getLogin(userName,userPassword);
            if(cursor.getCount() >0){
                cursor.moveToFirst();
                et1 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_NAME));
                et2 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_DEPT));
                et3 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID));
                et4 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_LEVEL));
                et6 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_PASSWORD));
                et7 = cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_QOUTE));
                    Cursor cursor2 = dbHelper.searchForPics(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID)));
                    if(cursor2.getCount() >0){
                        cursor2.moveToFirst();
                        byteArray = cursor2.getBlob(cursor2.getColumnIndex(dbColumnList.userPics.COLUMN_PICS));
                    }
                    cursor2.close();
                cursor.close();
                type="local";
                outre1="Yes";
                return null;
            }else{
                outre1="No";
            }
            return outre1;
        }

        @Override
        protected void onPostExecute(String content) {
            if(outre1.equals("Yes")) {
                // new ReadJSON().execute();
                new ReadJSON().execute();
            }
            else{
                // pd.hide();
                displayMessage("Error: Wrong Username Or Password !!!");
            }
        }
    }
    class ReadJSON extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {

                SharedPreferences.Editor editor;
                if(type.equals("online")) {
                    JSONObject jsonobject = new JSONObject(allResult);
                    //must be arranged exact way it comes from server
                    et1 = jsonobject.getString("MyName");
                    et2 = jsonobject.getString("MyDept");
                    et3 = jsonobject.getString("MyId");
                    et4 = jsonobject.getString("MyLevel");
                    et5 = jsonobject.getString("MyPics");
                    et6 = jsonobject.getString("MyPassword");
                    et7 = jsonobject.getString("MyQoute");
                }
                //name
                editor = MyName.edit();
                editor.putString("MyName", et1);
                editor.apply();

                //department
                editor = MyDept.edit();
                editor.putString("MyDept", et2);
                editor.apply();

                //userID
                editor = MyId.edit();
                editor.putString("MyId",et3);
                editor.apply();

                //level
                editor = MyLevel.edit();
                editor.putString("MyLevel", et4);
                editor.apply();

                //picture
                if(type.equals("online")) {
                    String MyPicsData = et5;
                    Bitmap bitmap = Glide.with(getApplicationContext()).load(MyPicsData).asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byteArray = stream.toByteArray();
                }
                String bytes = Base64.encodeToString(byteArray,0);
                editor = MyPics.edit();
                editor.putString("MyPics", bytes);
                editor.apply();

                //password
                editor = MyPassword.edit();
                editor.putString("MyPassword", et6);
                editor.apply();

                //Qoute
                editor = MyQoute.edit();
                editor.putString("MyQoute", et7);
                editor.apply();

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

            try {
                pd.hide();
                PendingIntent pendingIntent;
                AlarmManager manager;

                Intent alarmIntent = new Intent(getApplicationContext(), reminderService.class);
                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                manager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                int reminderIntervalMin = 40;
                int reminderIntervalSec = (int)(TimeUnit.MINUTES.toMillis(reminderIntervalMin));
                int syncTime = reminderIntervalSec;
                // manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime()+300, 300*1000, pendingIntent);
                manager.setRepeating(AlarmManager.RTC_WAKEUP, reminderIntervalSec, reminderIntervalSec + syncTime, pendingIntent);

                Toast.makeText(getApplicationContext(),"Welcome To ATBU 37 NAMSN ELITE YEAR BOOK",Toast.LENGTH_LONG).show();
              //  Toast.makeText(getApplicationContext(),et1 + ", " + et2 + "," + et3 + "," + et4 + "," + et5 + "," + et6 ,Toast.LENGTH_LONG).show();
              //  Toast.makeText(getApplicationContext(),allResult ,Toast.LENGTH_LONG).show();
                //move to home Screen
               Intent intent = new Intent(getApplicationContext(), homeView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                displayMessage("Error: No Internet Connection !!!");
            }

            super.onPostExecute(s);
        }
    }

}
