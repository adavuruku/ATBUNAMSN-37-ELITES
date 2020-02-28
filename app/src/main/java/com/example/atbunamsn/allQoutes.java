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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class allQoutes extends AppCompatActivity implements SearchView.OnQueryTextListener{
    ProgressDialog pd;

    private DrawerLayout drawerLayout;
    Intent intent;
    AlertDialog.Builder builder;

    private static int SPLASH_TIME_OUT = 500;//5seconds
    String extraSearch;
    TextView prev;
    String prev_p;
    URLConnection urlconnection;
    URL url;
    String address = "http://192.168.230.1/androidReg/androidNamsnService.php";
    byte[] image_data;
    String fullname, email, qoute, comingNews;
    ImageView imageViewProduct;
    private boolean isConnected = false;
    String userID, loginData, userName, userDept, userLevel, dept, prep, level, User_id;
    byte[] userPics;
    Toolbar toolbar;
    Button saveRecord;
    Bitmap bitmap;
    Snackbar snackbar;
    private dbHelper dbHelper;
    String search;
    TextView connectionStatus;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<Product> newList, arraylistmaths;
    ;
    private qouteAdapter recyclerAdapter;
    ProgressBar progressBar;
    SharedPreferences MyPassword, MyName, MyDept, MyPics, MyQoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_qoutes);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userLevel = intent.getStringExtra("userLevel");
        userDept = intent.getStringExtra("userDept");
        userID = intent.getStringExtra("userID");
        userPics = intent.getByteArrayExtra("userPics");
        initialize();

        bitmap = BitmapFactory.decodeByteArray(userPics, 0, userPics.length);
        imageViewProduct.setImageBitmap(bitmap);

        SubtitleCollapsingToolbarLayout con = (SubtitleCollapsingToolbarLayout) findViewById(R.id.collapsing);
        con.setSubtitle(userLevel + " Level - " + userDept);
        con.setTitle(userName);

        //initialize shared preferences - for saved qoute
        MyName = this.getSharedPreferences("MyName", this.MODE_PRIVATE);
        MyDept = this.getSharedPreferences("MyDept", this.MODE_PRIVATE);
        MyPassword = this.getSharedPreferences("MyPassword", this.MODE_PRIVATE);
        MyQoute = this.getSharedPreferences("MyQoute", this.MODE_PRIVATE);

        //for nav drawer
        dept = userDept;
        fullname = userName;
        User_id = userID;
        level = userLevel;
        image_data = userPics;
        prep = fullname + System.getProperty("line.separator") + level + " Level - " + dept;
        //start nav drawer
        initNavigationDrawer();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.performLongClick();
            }
        });


        connectionStatus = (TextView) findViewById(R.id.status);
        arraylistmaths = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new testConnection().execute();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new testConnection().execute();
            }
        }, SPLASH_TIME_OUT);
        //register for contextmenu
        registerForContextMenu(fab);

    }
    public void initialize() {
        imageViewProduct = (ImageView) findViewById(R.id.profile_pic);
        dbHelper = new dbHelper(getApplicationContext());

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.createmenu, menu);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(allQoutes.this, homeView.class);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            finish();
        }
    }

    class testConnection extends AsyncTask<String, Integer, String> {
        String outre;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            connectionStatus.setVisibility(View.INVISIBLE);
            connectionStatus.setVisibility(View.GONE);
            super.onPreExecute();
        }
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
                new loadLocalDatae().execute();
            }
        }
    }

    //volley request
    public void volleyJsonArrayRequest(String url) {
        String REQUEST_TAG = "com.volley.volleyJsonArrayRequestMaths";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        comingNews = response;
                        new loadValuese().execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new loadLocalDatae().execute();
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
        AppSingleton.getInstance(this).addToRequestQueue(postRequest, REQUEST_TAG);
    }


    //menu issues and nav drawers

    public void verify_close() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Do You Really Want to Exit ATBU NAMSSN 37 ELITE... ?. ");
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_app);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                int id = menuItem.getItemId();
                switch (id) {
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
        TextView tv_email = (TextView) header.findViewById(R.id.tv_email);
        TextView tv_fullname = (TextView) header.findViewById(R.id.tv_name);


        // prepare navigation view header

        tv_email.setText(level + " Level - " + dept);
        tv_fullname.setText(fullname);

        ImageView imageV = (ImageView) header.findViewById(R.id.profile_image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_data, 0, image_data.length);
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

                final Dialog d = new Dialog(allQoutes.this);
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
                        d.hide();
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
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.fairwhite));
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View v) {

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
        getMenuInflater().inflate(R.menu.menu_qoutes, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
       // searchView.setBackgroundColor(getResources().getColor(R.color.fairwhite));
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toString().toLowerCase();
        newList = new ArrayList<>();
        //craeta and instance of Contact array class
        // loop throug it
        for(Product cont : arraylistmaths){
            String name_ = cont.getName().toLowerCase();
            String depart_ = cont.getEmail().toLowerCase();
            if(name_.contains(newText) || depart_.contains(newText)){
                newList.add(cont);
            }
        }
        recyclerAdapter.setFilter(newList);
        search = "searchresult";
        recyclerAdapter.notifyDataSetChanged();
       recyclerAdapter = new qouteAdapter( newList, allQoutes.this, new qouteAdapter.OnItemClickListener() {
            @Override
            public void onNameClick(View v, int position) {
                intent = new Intent(getApplicationContext(), viewNamsn.class);
                intent.putExtra("userName", newList.get(position).getName());
                intent.putExtra("userLevel", newList.get(position).getLevel());
                intent.putExtra("userDept", newList.get(position).getDept());
                intent.putExtra("userID", newList.get(position).getuserID());
                intent.putExtra("userPics", newList.get(position).getBLOB());
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            }
        });
        recyclerAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerAdapter);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
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
                String u = userName.replace(" ", "_").replace(".", "");
                String fileID = u + "_" + String.valueOf(sa) + ".jpg";
                boolean t = s.storeImage(bitmap, fileID, getApplicationContext());
                if (t) {
                    Toast.makeText(getApplicationContext(), "Saved to Gallery", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed To Saved Image... Retry", Toast.LENGTH_LONG).show();
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
                break;
            case R.id.action_signout:
                drawerLayout.closeDrawers();
                verify_close();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class loadValuese extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jsonobject = null;
                JSONArray jsonarray = null;
                jsonarray = new JSONArray(comingNews);
                arraylistmaths.clear();
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);
                    Product contact = new Product();
                    contact.setDept(jsonobject.getString("userDept"));
                    contact.setName(jsonobject.getString("fullName"));
                    contact.setLevel(jsonobject.getString("userLevel"));
                    contact.setEmail(jsonobject.getString("userQoute"));
                    contact.setGender(jsonobject.getString("userGender"));
                    contact.setuserID(jsonobject.getString("userId"));
                    String MyPics = jsonobject.getString("profile");
                    Bitmap bitmap =  Glide.with(getApplication()).load(MyPics).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    contact.setBLOB(byteArray);

                    arraylistmaths.add(contact);
                }
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
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
            connectionStatus.setVisibility(View.INVISIBLE);
            connectionStatus.setVisibility(View.GONE);

            recyclerAdapter = new qouteAdapter( arraylistmaths, allQoutes.this, new qouteAdapter.OnItemClickListener() {
                @Override
                public void onNameClick(View v, int position) {
                    intent = new Intent(getApplicationContext(), viewNamsn.class);
                    intent.putExtra("userName", arraylistmaths.get(position).getName());
                    intent.putExtra("userLevel", arraylistmaths.get(position).getLevel());
                    intent.putExtra("userDept", arraylistmaths.get(position).getDept());
                    intent.putExtra("userID", arraylistmaths.get(position).getuserID());
                    intent.putExtra("userPics", arraylistmaths.get(position).getBLOB());
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            });
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerAdapter);
            search = "main";
            //super.onPostExecute(s);
        }
    }

    //load SQLite Data
    class loadLocalDatae extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

            arraylistmaths.clear();

          Cursor cursor = dbHelper.getAllUser();
           //String dept = "Mathematics";
          //Cursor cursor = dbHelper.getDetails(dbColumnList.userDetails.COLUMN_DEPT,dept);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Product contact = new Product();
                    contact.setDept(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_DEPT)));
                    contact.setName(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_NAME)));
                    contact.setLevel(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_LEVEL)));
                    contact.setEmail(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_QOUTE)));
                    contact.setGender(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_GENDER)));
                    contact.setuserID(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID)));

                    Cursor cursor2 = dbHelper.searchForPics(cursor.getString(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_USERID)));
                    if(cursor2.getCount() >0){
                        cursor2.moveToFirst();
                        contact.setBLOB(cursor2.getBlob(cursor2.getColumnIndex(dbColumnList.userPics.COLUMN_PICS)));
                    }
                    cursor2.close();
                    arraylistmaths.add(contact);

                }
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(String content) {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatus.setText("No Internet Connection !!");
            String stat = "ofline";
           // Toast.makeText(getApplicationContext(),"GOT ITADDED - " + String.valueOf(arraylistmaths.size()),Toast.LENGTH_LONG).show();
            recyclerAdapter = new qouteAdapter( arraylistmaths, allQoutes.this, new qouteAdapter.OnItemClickListener() {
                @Override
                public void onNameClick(View v, int position) {
                    intent = new Intent(getApplicationContext(), viewNamsn.class);
                    intent.putExtra("userName", arraylistmaths.get(position).getName());
                    intent.putExtra("userLevel", arraylistmaths.get(position).getLevel());
                    intent.putExtra("userDept", arraylistmaths.get(position).getDept());
                    intent.putExtra("userID", arraylistmaths.get(position).getuserID());
                    intent.putExtra("userPics", arraylistmaths.get(position).getBLOB());
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    finish();
                }
            });
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerAdapter);
            search = "main";
        }
    }

}

