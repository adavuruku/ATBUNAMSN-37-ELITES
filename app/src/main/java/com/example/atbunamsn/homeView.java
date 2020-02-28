package com.example.atbunamsn;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
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
import java.util.concurrent.ExecutionException;

public class homeView extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private SQLiteDatabase mDb;

    private dbHelper dbHelper;
    URLConnection urlconnection;
    URL url;
    String comingNews;
    String address = "http://192.168.230.1/androidReg/androidNamsnService.php";
    AlertDialog.Builder builder;
    SharedPreferences MyName,MyDept,MyId,MyLevel,MyPics,MyPassword;
    String fullname,dept,prep,userID, level, mypicsdata;
    byte[] image_data;
    Intent intent;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setSaveEnabled(true);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
       // tabLayout.setTabsFromPagerAdapter(mSectionsPagerAdapter);

       mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        dbHelper = new dbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        //initialize shared preferences
        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyId = this.getSharedPreferences("MyId", this.MODE_PRIVATE);
        MyLevel = this.getSharedPreferences("MyLevel", this.MODE_PRIVATE);
        MyPics = this.getSharedPreferences("MyPics", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);


        dept = MyDept.getString("MyDept", "");
        fullname = MyName.getString("MyName", "");
        userID = MyId.getString("MyId", "");
        level= MyLevel.getString("MyLevel", "");

        mypicsdata= MyPics.getString("MyPics", "");
        image_data = Base64.decode(mypicsdata,0);

        prep = fullname + System.getProperty("line.separator") + level +" Level - " + dept;

        if(fullname=="" || dept==""){
            Intent intenty = new Intent (this,LoginScreen.class);
            startActivity(intenty);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
            finish();
        }

        initNavigationDrawer();

       //launchTestService();
        Intent i = new Intent(this, myBackgroundService.class);
        startService(i);


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(fab);


        //startAlarm();
    }

    public void startAlarm() {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 1000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime()+300, 300*1000, pendingIntent);
       // Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.createmenu,menu);
    }

    @Override
    public boolean onContextItemSelected(
            MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_status:
                intent = new Intent(getApplicationContext(), changeQoute.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_pics:
                intent = new Intent(getApplicationContext(), changePics.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_pas:
                intent = new Intent(getApplicationContext(), changePassword.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_signout:
                verify_close();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }


    public void verify_close(){
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Do You Really Want to Exit ATBU NAMSSN 37 ELITE... ?. ");
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_app);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //EMPTY SOME OF THE SHARED PREFERANCES

                SharedPreferences.Editor editor;
                editor = MyPassword.edit();
                editor.putString("MyPassword", "");
                editor.apply();
                //name
                editor = MyName.edit();
                editor.putString("MyName", "");
                editor.apply();
                //department
                editor = MyDept.edit();
                editor.putString("MyDept", "");
                editor.apply();
                Intent intent = new Intent(homeView.this, LoginScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alert.show();
    }


    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, myBackgroundService.class);
        // Add extras to the bundle
        //i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }
    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.action_home:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), viewNamsn.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.action_status:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), changeQoute.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), changePics.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), changePassword.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.action_viewQoutes:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), allQoutes.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.action_signout:
                        drawerLayout.closeDrawers();
                        verify_close();
                        break;
                }
                return true;
            }
        });

        //set the header contents
        View header = navigationView.getHeaderView(0);
        TextView tv_email = (TextView)header.findViewById(R.id.tv_email);
        TextView tv_fullname = (TextView)header.findViewById(R.id.tv_name);


        // prepare navigation view header

        tv_email.setText(level + " Level - " + dept);
        tv_fullname.setText(fullname);

        ImageView imageV = (ImageView)header.findViewById(R.id.profile_image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_data,0,image_data.length);
        imageV.setImageBitmap(bitmap);

        imageV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View snackView = getLayoutInflater().inflate(R.layout.dialogview, null);
                ImageView imv = (ImageView) snackView.findViewById(R.id.diaprofile_pic);
                Bitmap bitmap = BitmapFactory.decodeByteArray(image_data, 0, image_data.length);
                imv.setImageBitmap(bitmap);
                TextView myvi = (TextView) snackView.findViewById(R.id.txtUser);

                //buttons in dialogview
                ImageButton btnPassword = (ImageButton) snackView.findViewById(R.id.btnPassword);
                ImageButton btnProfile = (ImageButton) snackView.findViewById(R.id.btnProfile);
                ImageButton btnPics = (ImageButton) snackView.findViewById(R.id.btnPics);
                ImageButton btnEditQoute = (ImageButton) snackView.findViewById(R.id.btnEditQoute);

                myvi.setText(prep);

                final Dialog d = new Dialog(homeView.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCanceledOnTouchOutside(true);
                //  d.setTitle(fullname);
                d.setContentView(snackView);
                d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                d.show();
                drawerLayout.closeDrawers();

                btnEditQoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), changeQoute.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                });
                btnProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), viewNamsn.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                });
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), changePics.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                });
                btnPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), changePassword.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                });
                imv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //String[] cour = dept.split(" Level - ");
                        //courses.setText(courses_.trim());
                        Intent intent = new Intent(getApplicationContext(), viewNamsn.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        //finish();
                    }
                });
            }
        });


        //ENDS HERE
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.fairwhite));
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerClosed(View v){

                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                //drawerLayout.setBackgroundColor(getResources().getColor(R.color.fairwhite));
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
        //DIALOG OPERATION
    //menu settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.action_home:
                drawerLayout.closeDrawers();
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), viewNamsn.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_status:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changeQoute.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changePics.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changePassword.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_viewQoutes:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), allQoutes.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.action_signout:
                drawerLayout.closeDrawers();
                verify_close();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openNewActivity(String activity){

    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    computer computer = new computer();
                    computer.setRetainInstance(true);
                    return computer;
                case 1:
                    math math = new math();
                    math.setRetainInstance(true);
                    return math;
                case 2:
                    statistic statistic = new statistic();
                    statistic.setRetainInstance(true);
                    return statistic;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "COMPUTER";
                case 1:
                    return "MATHEMATICS";
                case 2:
                    return "STATISTICS";

            }
            return null;
        }
    }
}
