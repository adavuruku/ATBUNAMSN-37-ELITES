package com.example.atbunamsn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator;

public class MainActivity extends AppCompatActivity {
    ImageView img;
    SharedPreferences MyName,MyDept,MyId,MyLevel,MyPics,MyPassword;
    private dbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScaleInTopAnimator animator = new ScaleInTopAnimator();
        img = (ImageView) findViewById(R.id.app);
        boolean goingDown = false;
        AnimationUtils.imagegroup(img,goingDown);

        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyId = this.getSharedPreferences("MyId", this.MODE_PRIVATE);
        MyLevel = this.getSharedPreferences("MyLevel", this.MODE_PRIVATE);
        MyPics = this.getSharedPreferences("MyPics", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startProg();
            }
        },5000);

        dbHelper = new dbHelper(this);

        File database = this.getDatabasePath(dbHelper.DATABASE_NAME);
        //if it has not copy it do this - copy it
        if(false == database.exists()){
            dbHelper.getReadableDatabase();
            //COPY DATTABASE
            if(copyDatabase(this)){
                Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Not Copied",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    private boolean copyDatabase(Context context){
        try {
            InputStream inputStream =context.getAssets().open(dbHelper.DATABASE_NAME);
            String outfilename = dbHelper.DBLOCATION + dbHelper.DATABASE_NAME;
            OutputStream outputStream = new FileOutputStream(outfilename);
            byte[] buff = new byte[1024];
            int length = 0;
            while((length = inputStream.read(buff))> 0){
                outputStream.write(buff,0,length);
            }
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void startProg(){
        //retrieve content saved in preference - politicsData

        String loginData = MyId.getString("MyId", "");
        String namedata = MyName.getString("MyName", "");
        String departmentdata = MyDept.getString("MyDept", "");
        String leveldata = MyLevel.getString("MyLevel", "");
        String picsdata = MyPics.getString("MyPics", "");
        String pasworddata = MyPassword.getString("MyPassword", "");

      // Toast.makeText(this,"1:"+loginData + " - 2: "+departmentdata + " - 3: " + leveldata + " - 4: " + pasworddata,Toast.LENGTH_LONG).show();
        if(loginData != "" &&  leveldata != "" &&  namedata != "" &&  departmentdata != "" &&  picsdata != "" &&  pasworddata != ""){
            clearImage();
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

            Intent intent = new Intent (this,homeView.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
            finish();
        }else{
            //load login page
            clearImage();
            Intent intent = new Intent (this,LoginScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
            finish();
        }
    }

    public void clearImage(){
        try {
            boolean goingDown =false;
            AnimationUtils.imagegroup(img,goingDown);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
