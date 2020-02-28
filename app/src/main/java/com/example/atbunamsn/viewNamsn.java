package com.example.atbunamsn;

import android.Manifest;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class viewNamsn extends AppCompatActivity {
    ProgressDialog pd;

    private DrawerLayout drawerLayout;
    Intent intent;
    AlertDialog.Builder builder;
    SharedPreferences MyName,MyDept,MyId,MyLevel,MyPics,MyPassword;

    private static int SPLASH_TIME_OUT = 500;//5seconds
    TextView connectionStatus, qoute, moment, friends, lectures, courses, phone, email_tx, permState, permAdd, currenttate, currentAdd;
    URLConnection urlconnection;
    URL url;
    public List<phoneList> arraylist;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String address = "http://192.168.230.1/androidReg/androidNamsn.php";
    String fullname, email;
    ImageView imageViewProduct;
    private boolean isConnected = false;
    public NetworkChangeReceiver receiver;
    String userID, userName, userDept, userLevel,dept,mypicsdata,prep,level,User_id;
    byte[] userPics,image_data;
    Toolbar toolbar;
    RecyclerView recyclerView;
    private SQLiteDatabase mDb;
    private dbHelper dbHelper;
    private phoneAdapter phoneAdapter;
    ProgressBar progressBar;
    Bitmap bitpmapsave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_namsn);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.phonerecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        dbHelper = new dbHelper(this);
        //mDb = dbHelper.getWritableDatabase();
        progressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        Button retrieve = (Button)findViewById(R.id.retrieve);
        retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  new FileDownload().execute();
            }
        });
        arraylist = new ArrayList<>();
        /**getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setTitle(userName);
         getSupportActionBar().setSubtitle(userLevel + " - " +  userDept);**/

        //setupToolbar();
        initialize();
        // String new_id_name = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        connectionStatus = (TextView) findViewById(R.id.status);

        ImageButton emailbut = (ImageButton) findViewById(R.id.emailbut);
        emailbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(viewNamsn.this, "Clickdd Email", Toast.LENGTH_SHORT).show();
                TextView email = (TextView) findViewById(R.id.email);
                 if (email.getText().toString().trim() !="Loading..." && ! (TextUtils.isEmpty(email.getText().toString().trim()))){
                     Intent intent = new Intent(Intent.ACTION_SEND);
                     intent.setType("plain/text");
                     intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email.getText().toString() });
                     intent.putExtra(Intent.EXTRA_SUBJECT, "");
                     intent.putExtra(Intent.EXTRA_TEXT, "");
                     if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, ""));
                     }
                 }
            }
        });


        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userLevel = intent.getStringExtra("userLevel");
        userDept = intent.getStringExtra("userDept");
        userID = intent.getStringExtra("userID");
        userPics = intent.getByteArrayExtra("userPics");


        Bitmap bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
        bitpmapsave = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
        imageViewProduct.setImageBitmap(bitmap);

        SubtitleCollapsingToolbarLayout con = (SubtitleCollapsingToolbarLayout) findViewById(R.id.collapsing);
        con.setSubtitle(userLevel + " Level - " + userDept);
        con.setTitle(userName);

        //for nav drawer

        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyId = this.getSharedPreferences("MyId", this.MODE_PRIVATE);
        MyLevel = this.getSharedPreferences("MyLevel", this.MODE_PRIVATE);
        MyPics = this.getSharedPreferences("MyPics", this.MODE_PRIVATE);

        dept = MyDept.getString("MyDept", "");
        fullname = MyName.getString("MyName", "");
        User_id = MyId.getString("MyId", "");
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

        //start nav drawer
        initNavigationDrawer();
        //ends nav drawer contents

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new testConnection().execute();
            }
        }, SPLASH_TIME_OUT);


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(fab);

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


    //verify once networ is change - wifi or sim data
    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        this.registerReceiver(receiver, filter);
        //   setupToolbar();
        super.onResume();
    }

    @Override
    public void onPause() {
        this.unregisterReceiver(receiver);
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
                Intent intent = new Intent(viewNamsn.this, homeView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
            }
    }

    public void initialize() {
        imageViewProduct = (ImageView) findViewById(R.id.profile_pic);
        qoute = (TextView) findViewById(R.id.qoute);
        moment = (TextView) findViewById(R.id.moment);
        friends = (TextView) findViewById(R.id.friends);
        lectures = (TextView) findViewById(R.id.lectures);
        courses = (TextView) findViewById(R.id.courses);
        //phone= (TextView)findViewById(R.id.phone);
        email_tx = (TextView) findViewById(R.id.email);
        permState = (TextView) findViewById(R.id.permState);
        permAdd = (TextView) findViewById(R.id.permAdd);
        currenttate = (TextView) findViewById(R.id.currenttate);
        currentAdd = (TextView) findViewById(R.id.currentAdd);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            isNetworkAvailable(context);
        }
    }

    //test if wireles or data is on
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            isConnected = true;
                            new testConnection().execute();
                        }
                        return true;
                    }
                }
            }
        }
        isConnected = false;
        return false;
    }

    //test wireles on ends
    //test internet connection class begins
    class testConnection extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            connectionStatus.setVisibility(View.INVISIBLE);
            connectionStatus.setVisibility(View.GONE);
            super.onPreExecute();
        }

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
                volleyJsonArrayRequest(address);
            }
            else{
                new loadLocalData().execute();
            }
        }
    }
//test internet connection stop

    //volley request
    public void volleyJsonArrayRequest(String url) {
        String REQUEST_TAG = "com.volley.volleyJsonArrayRequestSearched";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadValues(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new loadLocalData().execute();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "retrieve");
                params.put("userID", userID);
                return params;
            }
        };
        AppSingleton.getInstance(this).addToRequestQueue(postRequest, REQUEST_TAG);
    }

    //internet result load result
    public void loadValues(String comingNews) {
        try {
            JSONObject jsonobject = new JSONObject(comingNews);

           // Glide.with(this).load(userPics).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.5f).into(imageViewProduct);
            qoute.setText(jsonobject.getString("qoute"));
            moment.setText(jsonobject.getString("moment"));
            email_tx.setText(jsonobject.getString("email"));
            permState.setText(jsonobject.getString("permState"));
            permAdd.setText(jsonobject.getString("permAdd"));
            currenttate.setText(jsonobject.getString("currentstate"));
            currentAdd.setText(jsonobject.getString("currentAdd"));
            int j = 1;
            String friends_ = "";
            String[] fri = jsonobject.getString("friends").split(",");
            for (String fr : fri) {
                friends_ = friends_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + fr;
                j = j + 1;
            }
            friends.setText(friends_.trim());

            j = 1;
            String lectures_ = "";
            String[] lect = jsonobject.getString("lectures").split(",");
            for (String lec : lect) {
                lectures_ = lectures_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + lec;
                j = j + 1;
            }
            lectures.setText(lectures_.trim());

            j = 1;
            String courses_ = "";
            String[] cour = jsonobject.getString("courses").split(",");
            for (String cou : cour) {
                courses_ = courses_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + cou;
                j = j + 1;
            }
            courses.setText(courses_.trim());

            String phone_ = "";
            String[] phon = jsonobject.getString("phone").split(",");
            //phoneList contact1 = new phoneList();
            arraylist.clear();
            for (String pho : phon) {
                phone_ = phone_ + System.getProperty("line.separator") + pho;
                // phoneList contact = new phoneList();
                //contact.setPhone(pho);
                arraylist.add(new phoneList(pho));
            }

            //onclic of email


            //semail_tx.setText(phone_);
            phoneAdapter = new phoneAdapter(arraylist, getApplication(), new phoneAdapter.OnItemClickListener() {
                @Override
                public void onMessageClick(View v, int position) {
                    String phone_id = arraylist.get(position).getPhone();
                    Uri smsUri = Uri.parse("tel:" + phone_id);
                    Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                    intent.putExtra("address", phone_id);
                    intent.putExtra("sms_body", "");
                    intent.setType("vnd.android-dir/mms-sms");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }

                @Override
                public void onCallClick(View v, int position) {
                    String phone_id = "tel:" + arraylist.get(position).getPhone();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(phone_id));
                    if (callIntent.resolveActivity(getPackageManager()) != null) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                            return;
                        }
                        startActivity(callIntent);
                    }
                    // Toast.makeText(getApplicationContext(),"Phone Clicked Short - ",Toast.LENGTH_SHORT).show();
                }
            });
            phoneAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(phoneAdapter);
            // phone.setText(phone_.trim());
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
            connectionStatus.setVisibility(View.INVISIBLE);
            connectionStatus.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //load SQLite Data
    class loadLocalData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(String content) {
            // connectionStatus.setText("No Internet here !!" + userID);
            Cursor cursor = dbHelper.getDetails(dbColumnList.userDetails.COLUMN_USERID, userID);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    qoute.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_QOUTE)));
                    moment.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_BESTMOMENT)));
                    email_tx.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_EMAIL)));
                    permState.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_STATE)) +
                            " - " + cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_LGOV)));
                    permAdd.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_PERMADD)));
                    currenttate.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_CSTATE)) +
                            " - " + cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_CLGOV)));
                    currentAdd.setText(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_CPERMADD)));
                }
            }


            //FRIENDS
            cursor = dbHelper.getAllSingle(dbColumnList.userBest.TABLE_NAME, dbColumnList.userBest.COLUMN_USERID, userID);
            String fri = "";
            int j = 1;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String frien = cursor.getString(cursor.getColumnIndex(dbColumnList.userBest.COLUMN_BESTFRIEND));
                    fri = fri + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + frien;
                    j = j + 1;
                }
                friends.setText(fri.trim());
            }

            //lecturer
            cursor = dbHelper.getAllSingle(dbColumnList.userLecturer.TABLE_NAME, dbColumnList.userLecturer.COLUMN_USERID, userID);
            String lectures_ = "";
            j = 1;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String lect = cursor.getString(cursor.getColumnIndex(dbColumnList.userLecturer.COLUMN_BESTLECTURER));
                    lectures_ = lectures_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + lect;
                    j = j + 1;
                }
                lectures.setText(lectures_.trim());
            }

            //courses
            cursor = dbHelper.getAllSingle(dbColumnList.userCourse.TABLE_NAME, dbColumnList.userCourse.COLUMN_USERID, userID);
            j = 1;
            String courses_ = "";
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String cou = cursor.getString(cursor.getColumnIndex(dbColumnList.userCourse.COLUMN_BESTCOURSE));
                    courses_ = courses_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + cou;
                    j = j + 1;
                }
                courses.setText(courses_.trim());
            } else {
                courses.setText(" alright noo");
            }

            //phones
            cursor = dbHelper.getAllSingle(dbColumnList.userPhone.TABLE_NAME, dbColumnList.userPhone.COLUMN_USERID, userID);
            j = 1;
            String phone_ = "";
            if (cursor.getCount() > 0) {
                arraylist.clear();
                while (cursor.moveToNext()) {
                    String pho = cursor.getString(cursor.getColumnIndex(dbColumnList.userPhone.COLUMN_PHONE));
                    // phone_ = phone_ + System.getProperty("line.separator") + System.getProperty("line.separator") + String.valueOf(j) + ". " + pho;
                    //j = j + 1;
                    arraylist.add(new phoneList(pho));
                }
            }

            phoneAdapter = new phoneAdapter(arraylist, getApplication(), new phoneAdapter.OnItemClickListener() {
                @Override
                public void onMessageClick(View v, int position) {
                    String phone_id = arraylist.get(position).getPhone();
                    Uri smsUri = Uri.parse("tel:" + phone_id);
                    Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                    intent.putExtra("address", phone_id);
                    intent.putExtra("sms_body", "");
                    intent.setType("vnd.android-dir/mms-sms");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }

                @Override
                public void onCallClick(View v, int position) {
                    String phone_id = "tel:" + arraylist.get(position).getPhone();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(phone_id));
                    if (callIntent.resolveActivity(getPackageManager()) != null) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                            return;
                        }
                        startActivity(callIntent);
                    }
                   // Toast.makeText(getApplicationContext(),"Phone Clicked Short - ",Toast.LENGTH_SHORT).show();
                }
            });
            phoneAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(phoneAdapter);

            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatus.setText("No Internet Connection !!");

        }
    }


    //menu issues and nav drawers

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
                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
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

    //nav drawer starts


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
                        intent = new Intent(getApplicationContext(), homeView.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        finish();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), viewNamsn.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_status:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), changeQoute.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent(getApplicationContext(), changePics.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
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
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
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
                        finish();
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

                final Dialog d = new Dialog(viewNamsn.this);
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
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
                btnProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), viewNamsn.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), changePics.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
                btnPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(getApplicationContext(), changePassword.class);
                        intent.putExtra("userName", fullname);
                        intent.putExtra("userLevel", level);
                        intent.putExtra("userDept", dept);
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
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
                        intent.putExtra("userID", User_id);
                        intent.putExtra("userPics", image_data);
                        d.hide();
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
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

    //nav drrawer ends

    //menu settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_others, menu);
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
                intent = new Intent(getApplicationContext(), homeView.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
                break;
            case R.id.action_save:
                //save profile image of any user
                drawerLayout.closeDrawers();
                saveImage s = new saveImage();
               //Date tI = Calendar.getInstance().getTime();
                long sa = System.currentTimeMillis();
                String u = userName.replace(" ","_").replace(".","");
                String fileID = u + "_" + String.valueOf(sa)+".jpg";
                boolean t = s.storeImage(bitpmapsave,fileID,getApplicationContext());
                if(t){
                    Toast.makeText(getApplicationContext(),"Saved to Gallery",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Failed To Saved Image... Retry",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), viewNamsn.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", User_id);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_status:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changeQoute.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", User_id);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changePics.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", User_id);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
                intent = new Intent(getApplicationContext(), changePassword.class);
                intent.putExtra("userName", fullname);
                intent.putExtra("userLevel", level);
                intent.putExtra("userDept", dept);
                intent.putExtra("userID", User_id);
                intent.putExtra("userPics", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
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
                finish();
                break;
            case R.id.action_signout:
                drawerLayout.closeDrawers();
                verify_close();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
