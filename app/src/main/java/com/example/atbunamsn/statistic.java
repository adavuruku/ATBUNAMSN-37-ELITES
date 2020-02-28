package com.example.atbunamsn;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by sherif146 on 04/06/2017.
 */
public class statistic extends Fragment implements SearchView.OnQueryTextListener{
    private List<Product> arrayliststat;
    ProgressDialog pd;
    private static int SPLASH_TIME_OUT = 500;//5seconds
    TextView connectionStatus;

    URLConnection urlconnection;
    URL url;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String address = "http://192.168.230.1/androidReg/androidNamsn.php";
    byte[] image_data;
    String fullname, email,comingNews;
    homeView bl;
    private boolean isConnected = false;
   // public NetworkChangeReceiver receiver;
    SharedPreferences statisticlist;
    RecyclerView recyclerView;
    private SQLiteDatabase mDb;
    private dbHelper dbHelper;
    private recyclerAdapter recyclerAdapter;
    String search;
    ProgressBar progressBar;
    ArrayList<Product> newList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.computer, container, false);
        bl = (homeView) getActivity();
        setHasOptionsMenu(true);
        //arraylist = new ArrayList<>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) rootView.findViewById(R.id.simpleProgressBar);
        dbHelper = new dbHelper(getActivity());
       // mDb = dbHelper.getWritableDatabase();
        connectionStatus = (TextView) rootView.findViewById(R.id.status);

        //when recyclerView is Clicked
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),recyclerView , new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //handle click events here
                // Toast.makeText(getActivity(),"Yeaa Clicked - " + userPics,Toast.LENGTH_SHORT).show();
                String userID,userDept,userName,userLevel;
                byte[] userPics;
                if(search=="searchresult") {
                    userID = newList.get(position).getuserID();
                    userDept = newList.get(position).getDept();
                    userName = newList.get(position).getName();
                    userLevel = newList.get(position).getLevel();
                    userPics = newList.get(position).getBLOB();
                }else{
                    userID = arrayliststat.get(position).getuserID();
                    userDept = arrayliststat.get(position).getDept();
                    userName = arrayliststat.get(position).getName();
                    userLevel = arrayliststat.get(position).getLevel();
                    userPics = arrayliststat.get(position).getBLOB();
                }
                Intent intent = new Intent (getActivity(),viewNamsn.class);
                intent.putExtra("userName", userName);
                intent.putExtra("userLevel", userLevel);
                intent.putExtra("userDept", userDept);
                intent.putExtra("userID", userID);
                intent.putExtra("userPics", userPics);
                bl.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //handle longClick if any
                //String new_id_name = arraylist.get(position).getuserID();
                //Toast.makeText(getActivity(),"Yeaa Clicked Long - " + new_id_name,Toast.LENGTH_SHORT).show();
            }
        }));

        connectionStatus = (TextView) rootView.findViewById(R.id.status);
        arrayliststat = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
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
        },SPLASH_TIME_OUT);
        return rootView;
    }

    /**  @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    new Handler().postDelayed(new Runnable() {
    @Override
    public void run() {
    new testConnection().execute();
    }
    },SPLASH_TIME_OUT);
    super.onActivityCreated(savedInstanceState);
    }

     @Override
     public void onResume(){
     IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
     receiver = new NetworkChangeReceiver();
     getActivity().registerReceiver(receiver, filter);
     super.onResume();
     }
     @Override
     public void onPause(){
     getActivity().unregisterReceiver(receiver);
     super.onPause();
     }

     //verify once networ is change - wifi or sim data
     /**  public class NetworkChangeReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(final Context context, final Intent intent) {
     isNetworkAvailable(context);
     }
     }

     public boolean isNetworkAvailable(Context context) {
     ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     if (connectivity != null) {
     NetworkInfo[] info = connectivity.getAllNetworkInfo();
     if (info != null) {
     for (int i = 0; i < info.length; i++) {
     if (info[i].getState() == NetworkInfo.State.CONNECTED) {
     if(!isConnected){
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
     }**/

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

    //volley request
    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequestStat";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        comingNews = response;
                        new loadValues().execute();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new loadLocalData().execute();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "search");
                params.put("department", "Statistics");
                return params;
            }
        };
        AppSingleton.getInstance(getActivity()).addToRequestQueue(postRequest, REQUEST_TAG);
    }

    //load result
    class loadValues extends AsyncTask<String, Integer, String >{

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jsonobject = null;
                JSONArray jsonarray = null;
                jsonarray = new JSONArray(comingNews);
                arrayliststat.clear();
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);
                    Product contact = new Product();
                    contact.setDept(jsonobject.getString("userDept"));
                    contact.setName(jsonobject.getString("fullName"));
                    contact.setLevel(jsonobject.getString("userLevel"));
                    contact.setEmail(jsonobject.getString("userEmail"));
                    contact.setGender(jsonobject.getString("userGender"));
                    contact.setPics_path(jsonobject.getString("profile"));

                    String MyPics = jsonobject.getString("profile");
                    Bitmap bitmap =  Glide.with(getActivity()).load(MyPics).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    contact.setBLOB(byteArray);

                    contact.setuserID(jsonobject.getString("userId"));
                    arrayliststat.add(contact);
                }
                //  bl.politiclist = arraylist;
                /**  SharedPreferences.Editor editor = computerlist.edit();
                 //update content saved in preference - politicsData
                 editor.putString("computerData", comingNews);
                 editor.commit();**/

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
            String stat = "online";
            recyclerAdapter = new recyclerAdapter(recyclerView, arrayliststat, getActivity(),stat);
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerAdapter);
            search = "main";
            //super.onPostExecute(s);
        }
    }

    //load SQLite Data
    class loadLocalData extends AsyncTask<String,Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            arrayliststat.clear();
            // connectionStatus.setText("No Internet here !!" + userID);
            String dept = "Statistics";
            Cursor cursor = dbHelper.getDetails(dbColumnList.userDetails.COLUMN_DEPT,dept);
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
                        contact.setBLOB(cursor2.getBlob(cursor2.getColumnIndexOrThrow(dbColumnList.userPics.COLUMN_PICS)));
                    }
                    cursor2.close();
                    arrayliststat.add(contact);
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
            recyclerAdapter = new recyclerAdapter(recyclerView, arrayliststat, getActivity(),stat);
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerAdapter);
            search = "main";
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        searchView.setBackgroundColor(getResources().getColor(R.color.fairwhite));
        super.onCreateOptionsMenu(menu,inflater);
        //return true;
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
        for(Product cont : arrayliststat){
            String name_ = cont.getName().toLowerCase();
            String depart_ = cont.getDept().toLowerCase();
            if(name_.contains(newText) || depart_.contains(newText)){
                newList.add(cont);
            }
        }
        recyclerAdapter.setFilter(newList);
        search = "searchresult";
        recyclerAdapter.notifyDataSetChanged();
        return true;
    }
}
