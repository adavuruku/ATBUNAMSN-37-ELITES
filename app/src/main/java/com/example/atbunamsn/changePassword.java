package com.example.atbunamsn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class changePassword extends AppCompatActivity {
    ProgressDialog pd;

    private DrawerLayout drawerLayout;
    Intent intent;
    AlertDialog.Builder builder;

    private static int SPLASH_TIME_OUT = 500;//5seconds
    TextView prev, newP, retype;
    String current_p,retype_p,prev_p;
    URLConnection urlconnection;
    URL url;
    String address = "http://192.168.230.1/androidReg/androidNamsn.php";
    byte[] image_data;
    String fullname, email;
    ImageView imageViewProduct;
    private boolean isConnected = false;
    String userID,loginData, userName, userDept, userLevel,dept,prep,level,User_id;
    byte[] userPics;
    Toolbar toolbar;
    Button saveRecord;
    Bitmap bitmap;
    private dbHelper dbHelper;
    ProgressBar progressBar;
    SharedPreferences MyPassword,MyName,MyDept,MyPics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialize();

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userLevel = intent.getStringExtra("userLevel");
        userDept = intent.getStringExtra("userDept");
        userID = intent.getStringExtra("userID");
        userPics = intent.getByteArrayExtra("userPics");


        bitmap = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
        imageViewProduct.setImageBitmap(bitmap);

        SubtitleCollapsingToolbarLayout con = (SubtitleCollapsingToolbarLayout) findViewById(R.id.collapsing);
        con.setTitle(userName);
        con.setSubtitle(userLevel + " Level - " + userDept);

        //initialize shared preferences - for saved password
        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);
        dbHelper = new dbHelper(this);

        //for nav drawer
        dept = userDept;
        fullname = userName;
        User_id = userID;
        level= userLevel;
        image_data = userPics;
        prep = fullname + System.getProperty("line.separator") + level +" Level - " + dept;
        //start nav drawer
        initNavigationDrawer();
        //ends nav drawer contents

        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Processing Request ...");
        pd.setTitle(R.string.app_name);
        pd.setIcon(R.mipmap.ic_app);
        pd.setIndeterminate(true);
        pd.setCancelable(true);

        saveRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                pd.show();
                prev_p = prev.getText().toString();
                current_p = newP.getText().toString();
                retype_p = retype.getText().toString();
                //verify if all details are provided
                if(TextUtils.isEmpty(prev_p) || TextUtils.isEmpty(current_p)|| TextUtils.isEmpty(retype_p) || !current_p.equals(retype_p)){

                    displayMessage("Error: Invalid Data's Provided !! " + System.getProperty("line.separator") + "Please Verify !");
                }
                else
                {
                    String pasworddata = MyPassword.getString("MyPassword", "");
                    if(pasworddata.trim().equals(prev_p.trim()) && pasworddata.trim() !="") {
                        new testConnection().execute();

                    }else{
                        displayMessage("Error: Enter A Valid Current Password !! " + System.getProperty("line.separator")
                                + "Please Verify !");
                    }
                }
            }
        });

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
                finish();
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
                finish();
                break;
            case R.id.action_pas:
                break;
            case R.id.action_signout:
                verify_close();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(changePassword.this, homeView.class);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            finish();
        }
    }

    public void initialize() {
        imageViewProduct = (ImageView) findViewById(R.id.profile_pic);
        saveRecord = (Button) findViewById(R.id.btnSave);
        prev = (EditText) findViewById(R.id.prev);
        newP = (EditText) findViewById(R.id.current);
        retype = (EditText) findViewById(R.id.current_re);
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
                pd.show();
                volleyJsonArrayRequest(address);
            }
            else{
               // displayMessage("Error: No Internet Connection !! " + System.getProperty("line.separator") + "Please Verify !");
                localUpdatePassword();
            }
        }
    }

    public void localUpdatePassword(){
        dbHelper.updateForAll(dbColumnList.userDetails.TABLE_NAME,dbColumnList.userDetails.COLUMN_PASSWORD,current_p,
                dbColumnList.userBest.COLUMN_USERID, userID);
        Toast.makeText(changePassword.this,"Your Password was Successfully Updated / Change !!",Toast.LENGTH_LONG).show();
        //move to home Screen
        //edit passsword sharedpref
        SharedPreferences.Editor editor;

        editor = MyPassword.edit();
        editor.putString("MyPassword", current_p);
        editor.apply();

        Intent intent = new Intent(changePassword.this, homeView.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }
    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        pd.hide();
                        if(response.equals("Successfully Updated !!!")){
                            localUpdatePassword();
                        }else{
                            //displayMessage("Error: No Internet Connection !! " + System.getProperty("line.separator") + "Please Verify !");
                            localUpdatePassword();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.hide();
                        //displayMessage("Error: No Internet Connection !! " + System.getProperty("line.separator") + "Please Verify !");
                        localUpdatePassword();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "changePassword");
                params.put("newPassword", current_p);
                params.put("userID", userID);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
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
                        finish();
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();
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

                final Dialog d = new Dialog(changePassword.this);
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
                        d.hide();
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
                boolean t = s.storeImage(bitmap,fileID,getApplicationContext());
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
