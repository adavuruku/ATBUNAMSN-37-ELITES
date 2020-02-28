package com.example.atbunamsn;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class changePics extends AppCompatActivity {
    ProgressDialog pd;

    private DrawerLayout drawerLayout;
    Intent intent;
    AlertDialog.Builder builder;

    private static int SPLASH_TIME_OUT = 500;//5seconds
    TextView btnClose;
    String prev_p;
    String address = "http://192.168.230.1/androidReg/androidNamsn.php";
    byte[] image_data,imageBytes;
    String fullname, email, qoute;
    ImageView imageViewProduct,my_pics;
    private boolean isConnected = false;
    String userID,loginData, userName, userDept, userLevel,dept,prep,level,User_id;
    byte[] userPics;
    Toolbar toolbar;
    Button saveRecord;
    String imageSelected = "No";
    URLConnection urlconnection;
    Snackbar snackbar;
    Uri FileUri;
    Bitmap bitmap1;
    final int REQUEST_CODE_GALLERY=999;
    final int REQUEST_CODE_CAMERA=777;
    URL url;

    private dbHelper dbHelper;
    ProgressBar progressBar;
    SharedPreferences MyPassword,MyName,MyDept,MyPics,MyQoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pics);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialize();
        dbHelper = new dbHelper(this);
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userLevel = intent.getStringExtra("userLevel");
        userDept = intent.getStringExtra("userDept");
        userID = intent.getStringExtra("userID");
        userPics = intent.getByteArrayExtra("userPics");

        //bitmap1 is the former pictures
        bitmap1 = BitmapFactory.decodeByteArray(userPics,0,userPics.length);
        imageViewProduct.setImageBitmap(bitmap1);
        my_pics.setImageBitmap(bitmap1);

        //initialize shared preferences - for saved qoute
        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);
        MyPics = this.getSharedPreferences("MyPics", this.MODE_PRIVATE);


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


        SubtitleCollapsingToolbarLayout con = (SubtitleCollapsingToolbarLayout) findViewById(R.id.collapsing);
        con.setSubtitle(userLevel + " Level - " + userDept);
        con.setTitle(userName);

        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Processing Request ...");
        pd.setTitle(R.string.app_name);
        pd.setIcon(R.mipmap.ic_app);
        pd.setIndeterminate(true);
        pd.setCancelable(true);

        saveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageSelected.equals("Yes")){
                    //testCpnection
                   new testConnection().execute();
                }else{
                    //dont save
                    displayMessage("Error: No Photo / Image Selected !!");
                }
            }
        });

        //photo browse
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.browse);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);
                View snackView = getLayoutInflater().inflate(R.layout.browsefile, null);
                layout.addView(snackView, 0);
                snackbar.show();
                //browse
                TextView fab2 = (TextView) snackView.findViewById(R.id.btnChoose);
                fab2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(
                                changePics.this,new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                },REQUEST_CODE_GALLERY
                        );
                    }
                });

                //camera
                TextView fab1 = (TextView) snackView.findViewById(R.id.btnCamera);
                fab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                    }
                });
                //close snackview
                btnClose = (TextView) snackView.findViewById(R.id.btnClose);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });

            }
        });
        //snackbar ends

        final FloatingActionButton faba = (FloatingActionButton) findViewById(R.id.fab);
        faba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faba.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(faba);

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
                finish();
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
    public void initialize() {
        imageViewProduct = (ImageView) findViewById(R.id.profile_pic);
        my_pics = (ImageView) findViewById(R.id.my_pics);
        saveRecord = (Button) findViewById(R.id.btnSave);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE_GALLERY);
            }else{
                displayMessage("You don't have permission to Acces Phone Gallery!");
            }
            return;
        }

        if(requestCode == REQUEST_CODE_CAMERA){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                captureImage();
            }else{
                displayMessage("You don't have permission to Acces Phone Camera !");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //when browse to gallery
        snackbar.dismiss();
        Bitmap photo = null;
        // photo = (Bitmap) data.getExtras().get("data");
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data !=null) {
            Uri uri = data.getData();

            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();

            String j = (returnCursor.getString(nameIndex));
            String g = String.valueOf(Long.toString(returnCursor.getLong(sizeIndex)));
            //Toast.makeText(this,j + " - - "+g,Toast.LENGTH_LONG).show();
            int kbsize = (int) returnCursor.getLong(sizeIndex) / 1024;
            if (kbsize > 3000) {
                displayMessage("The Image Size Must be less than or equal to 30KB");
            } else {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    //imageViewProduct.setImageBitmap(bitmap);
                    my_pics.setImageBitmap(bitmap);
                    imageSelected = "Yes";
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageBytes = baos.toByteArray();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

            //when use device camera
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data !=null) {
                Uri uri = data.getData();
                Cursor returnCursor =
                        getContentResolver().query(uri, null, null, null, null);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();

                int kbsize = (int) returnCursor.getLong(sizeIndex) / 1024;
                if(kbsize > 3000){
                    displayMessage("The Image Size Must be less than or equal to 30KB");
                }else{
                    photo = (Bitmap) data.getExtras().get("data");
                    //imageViewProduct.setImageBitmap(photo);
                    my_pics.setImageBitmap(photo);
                    imageSelected = "Yes";
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageBytes = baos.toByteArray();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void captureImage() {
        String imagename = "urPics.jpg";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + imagename);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        FileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUri);
        // start the image capture Intent
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void localUpdatePics(){
        dbHelper.updateForPics(userID,imageBytes);
        Toast.makeText(changePics.this,"Your Profile Picture was Successfully Updated / Change !!",Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor;
        String bytes = Base64.encodeToString(imageBytes,0);
        editor = MyPics.edit();
        editor.putString("MyPics", bytes);
        editor.apply();

        Intent intent = new Intent(changePics.this, homeView.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }

    class testConnection extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            pd.show();
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
                //displayMessage("Error: No Internet Connection !! " + System.getProperty("line.separator") + "Please Verify !");
                localUpdatePics();
            }
        }
    }


    //volley to update pics
    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        pd.hide();
                        if(response.equals("Successfully Changed !!!")){
                            localUpdatePics();

                        }else{
                            //displayMessage("Error: Unable To Update Profile Picture.. Network Problem !!");
                            localUpdatePics();
                           // imageViewProduct.setImageBitmap(bitmap1);
                            my_pics.setImageBitmap(bitmap1);
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.hide();
                        //displayMessage("Error: Unable To Update Profile Picture.. Network Problem !!");
                        localUpdatePics();
                        //imageViewProduct.setImageBitmap(bitmap1);
                        my_pics.setImageBitmap(bitmap1);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                String encoded_string  = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                params.put("opr", "changePics");
                params.put("newPics", encoded_string);
                params.put("userID", userID);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(changePics.this, homeView.class);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            finish();
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

                final Dialog d = new Dialog(changePics.this);
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
                        d.hide();
                    }
                });
                btnPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.hide();
                        intent = new Intent(getApplicationContext(), changePassword.class);
                         intent.putExtra("userName", fullname);
                         intent.putExtra("userLevel", level);
                         intent.putExtra("userDept", dept);
                         intent.putExtra("userID", User_id);
                         intent.putExtra("userPics", image_data);

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
                boolean t = s.storeImage(bitmap1,fileID,getApplicationContext());
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
