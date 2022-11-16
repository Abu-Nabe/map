package com.socirank.Fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.R;
import com.socirank.Activity.FriendProfileActivity;
import com.socirank.Adapter.LeaderboardChatAdapter;
import com.socirank.Api.ApiClient;
import com.socirank.Api.MySingleton;
import com.socirank.Content.FullVideoWeekly;
import com.socirank.Message.ChatActivity;
import com.socirank.Model.LocationChatModel;
import com.socirank.Model.LocationModel;
import com.socirank.Utils.PreferencesUtils;
import com.socirank.ViewPager.LeaderboardPager;
import com.socirank.ZFirebaseCM.Client;
import com.socirank.ZFirebaseCM.Data;
import com.socirank.ZFirebaseCM.FCMClientService;
import com.socirank.ZFirebaseCM.MyResponse;
import com.socirank.ZFirebaseCM.Sender;
import com.socirank.Z_Extension.AppName;
import com.socirank.Z_Extension.getAge;
import com.socirank.Z_Extension.getNightMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    Location location;
    String type = "full";
    String connect = "false";
    String country, actualText;

    String privacy = "yes";
    String privacyType = "no";
    String unreadCount;
    Bitmap bmImg, icon;
    LinearLayout locationButton, locationTypeButton, leaderboardButton, locationChatButton, locationVotedButton;

    TextView connectionText;
    private double longitude;
    private double latitude;

    BroadcastReceiver broadcastReceiver;

    LocationManager locationManager;
    private LocationRequest locationRequest;
    FCMClientService apiService;

    String currentuserName, permission, mapType;

    GoogleApiClient googleApiClient;
    GoogleMap map;

    LeaderboardChatAdapter leaderboardChatAdapter;
    private List<LocationChatModel> locationChatList = new ArrayList<>();

    ArrayList<String> tokenList = new ArrayList<String>();
    Toolbar mTopToolbar;

    private static int PERMISSION_REQUEST = 1;

    String configVerification = "no";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_home,
                container, false);

        mapType = "get_location_private";

        permission = Manifest.permission.ACCESS_FINE_LOCATION;

        mTopToolbar = v.findViewById(R.id.toolbar);
        mTopToolbar.inflateMenu(R.menu.map_menu);
        mTopToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openDrawer();
                return false;
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(FCMClientService.class);

        currentuserName = PreferencesUtils.getUsername(getContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setAlpha(1.0f);

        getCountry();

        return v;
    }

    private void getToken()
    {
        String URL = getResources().getString(R.string.Url_Connect) + "FCM/"+ "CountryFirebaseCM.php";

        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {

                try {
                    JSONArray jsonArray = new JSONArray(result_code);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);

                        tokenList.add(item.getString("gcm_registration_id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("check", String.valueOf(e));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("failed", volleyError.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("country", country);
                return param;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void broadCast(RecyclerView recyclerView)
    {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String sender = intent.getStringExtra("sender");
                String receiver = intent.getStringExtra("receiver");
                String message = intent.getStringExtra("message");

                Log.d("broadcasted", "succeed");
                processMessage(sender, receiver, message, recyclerView);
            }
        };
    }

    private void processMessage(String sender, String country, String message, RecyclerView recyclerView)
    {
        if(country.equals(country)){
            String timestring = String.valueOf(System.currentTimeMillis());
            LocationChatModel msg = new LocationChatModel(sender, message, timestring, country);
            locationChatList.add(msg);
            scrollToBottom(recyclerView);
        }
    }

    private void openDrawer()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.activity_map_main);

        TextView locationTypeText = bottomSheetDialog.findViewById(R.id.location_type_text);
        locationButton = bottomSheetDialog.findViewById(R.id.getLocation);
        locationTypeButton = bottomSheetDialog.findViewById(R.id.location_type);
        leaderboardButton = bottomSheetDialog.findViewById(R.id.location_rank);
        locationChatButton = bottomSheetDialog.findViewById(R.id.location_chat);
        locationVotedButton = bottomSheetDialog.findViewById(R.id.location_voted);

        connectionText = bottomSheetDialog.findViewById(R.id.location_type_text);

        if(connect.equals("true")){
            locationTypeText.setText("Disconnect");
        }
        locationTypeButton.setOnClickListener(this::onClick);
        locationButton.setOnClickListener(this::onClick);
        leaderboardButton.setOnClickListener(this::onClick);
        locationChatButton.setOnClickListener(this::onClick);
        locationVotedButton.setOnClickListener(this::onClick);


        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    private void getCountry()
    {
        String config = PreferencesUtils.getUsername(getContext());
        String URL = getResources().getString(R.string.Url_Connect) + "configUser.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                try {
                    JSONObject jsonObject = new JSONObject(result_code);
                    String configCountry = jsonObject.getString("country");

                    country = configCountry;
                    getToken();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("check", String.valueOf(e));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("username", config);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    public void onClick(View v) {
        if(v == locationButton){
            map.clear();
            getCurrentLocation();
        }
        if(v == locationTypeButton){

            if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("Allow Location Access");
                builder1.setMessage("SociRank needs access to your location. Turn on Location Services in your device settings.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });

                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            } else {
                if(connect.equals("true")){
                    connectionText.setText(getString(R.string.Connect));
                    connect = "false";
                    privacy = "yes";
                    map.clear();
                    deleteLocation("delete_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude));
                }else{
                    String[] options = {"Global", "Friends"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            if(which == 0)
                            {
                                getAgeLimit();
                            }else if(which == 1)
                            {
                                connectionText.setText(getResources().getString(R.string.Disconnect));
                                mapType = "get_location_private";
                                changeLocationType();
                            }
                        }
                    });
                    builder.show();
                }
            }
        }
        if(v == leaderboardButton){
            goToLeaderboard();
        }
        if(v == locationChatButton){
            // if user below 18 can't join public chat.
            if(!country.equals("")){
                showChatDialog();
            }else {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Country needs to be selected");
                alertDialog.setMessage("You haven't selected a country!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        }
        if(v == locationVotedButton){
            goToVoted();
        }
    }

    private void getAgeLimit()
    {
        String currentuserName = PreferencesUtils.getUsername(getContext());
        String URL = getResources().getString(R.string.Url_Connect) + "Age.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code)
            {
                try {
                    JSONObject jsonObject = new JSONObject(result_code);
                    String year = jsonObject.getString("year");
                    String month = jsonObject.getString("month");
                    String day = jsonObject.getString("day");

                    String require = getAge.age(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

                    if(Integer.parseInt(require) > 17){
                        connectionText.setText(getResources().getString(R.string.Disconnect));
                        mapType = "get_location_public";
                        changeLocationType();
                    }else {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Age Requirements");
                        alertDialog.setMessage("You do not meet the age required.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to get age.", Toast.LENGTH_SHORT).show();
                    Log.d("Check", String.valueOf(e));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                Toast.makeText(getContext(), "Failed to age age.", Toast.LENGTH_SHORT).show();
            }
        }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();

                param.put("type", "get");
                param.put("username", currentuserName);

                return param;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void showChatDialog()
    {
        if(mapType.equals("connect")){
            // alert error msg
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("Connection");
            alertDialog.setMessage("You haven't connected to the map");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else if(mapType.equals("get_location_private")){
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(R.layout.activity_location_chat);

            TextView label;
            EditText commentArea;
            RecyclerView recyclerView;
            CircleImageView profile;
            ImageView addComment, send;
            label = bottomSheetDialog.findViewById(R.id.comment_label);
            commentArea = bottomSheetDialog.findViewById(R.id.commentArea);
            profile = bottomSheetDialog.findViewById(R.id.profile);
            send = bottomSheetDialog.findViewById(R.id.send_msg);
            addComment = bottomSheetDialog.findViewById(R.id.comment_icon);

            recyclerView = bottomSheetDialog.findViewById(R.id.RecyclerView);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            broadCast(recyclerView);

            String IMAGE_URL = getResources().getString(R.string.Url_Connect) + "images/";
            String img = currentuserName + ".png";
            String picassoURL = IMAGE_URL + img;

            Glide.with(getActivity()).load(picassoURL).into(profile);

            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            getMsgs(recyclerView);

            addComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                }
            });


            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!commentArea.getText().toString().equals("")){
                        sendMsg(commentArea.getText().toString(), recyclerView);
                        commentArea.setText("");
                    }
                }
            });
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDialog.show();
        }
    }

    private void getMsgs(RecyclerView recyclerView)
    {
        locationChatList.clear();
        String URL = getResources().getString(R.string.Url_Connect) + "leaderboardChat.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                try {
                    JSONArray jsonArray = new JSONArray(result_code);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject item = jsonArray.getJSONObject(i);

                        String username = item.getString("username");
                        String message = item.getString("message");
                        String timestamp = item.getString("timestamp");
                        String country = item.getString("country");

                        LocationChatModel msg = new LocationChatModel(username, message, timestamp, country);
                        locationChatList.add(msg);
                    }

                    leaderboardChatAdapter = new LeaderboardChatAdapter(getContext(), locationChatList);
                    recyclerView.setAdapter(leaderboardChatAdapter);
                    leaderboardChatAdapter.notifyDataSetChanged();

                    scrollToBottom(recyclerView);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("check", String.valueOf(e));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("type", "get_public");
                param.put("country", country);
                return param;
            }
        };



        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void sendMsg(String msg, RecyclerView recyclerView)
    {
        String actualText = msg.replaceAll("\\s+", " ");

        if(!actualText.equals("")) {
            String timestring = String.valueOf(System.currentTimeMillis());

            LocationChatModel locationChatModel = new LocationChatModel(currentuserName, actualText, timestring, country);
            locationChatList.add(locationChatModel);
            scrollToBottom(recyclerView);

            String URL = getResources().getString(R.string.Url_Connect) + "leaderboardChat.php";
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String result_code) {
                    updateMsg(actualText, currentuserName, country);
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> param = new HashMap<>();
                    param.put("type", "public");
                    param.put("username", currentuserName);
                    param.put("message", actualText);
                    param.put("timestamp", timestring);
                    param.put("country", country);
                    return param;
                }
            };

            int socketTimeout = 0;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            request.setRetryPolicy(policy);
            AppName.getInstance().addToRequestQueue(request);
        }
    }

    private void updateMsg(String actualText, String currentuserName, String country)
    {
        for (int i = 0; i < tokenList.size(); i++) {
            Data data = new Data(currentuserName, R.drawable.chat_icon, actualText + " Chat" + " " + currentuserName+": "+ actualText, "New Country Message",
                    country, actualText, "", "yes");

            Sender send = new Sender(data, tokenList.get(i));

            apiService.sendNotification(send)
                    .enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, retrofit2.Response<MyResponse> response) {
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
        }
    }

    private void scrollToBottom(RecyclerView recyclerView)
    {
        if(leaderboardChatAdapter != null){
            leaderboardChatAdapter.notifyDataSetChanged();
            if (leaderboardChatAdapter.getItemCount() > 1)
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, leaderboardChatAdapter.getItemCount() - 1);
        }else {
            leaderboardChatAdapter = new LeaderboardChatAdapter(getContext(), locationChatList);
            recyclerView.setAdapter(leaderboardChatAdapter);
            leaderboardChatAdapter.notifyDataSetChanged();
        }

    }

    private void goToLeaderboard()
    {
        Intent intent = new Intent(getContext(), LeaderboardPager.class);
        startActivity(intent);
    }

    private void goToVoted() {
        Intent intent = new Intent(getContext(), FullVideoWeekly.class);
        startActivity(intent);
    }

    private void changeLocationType()
    {
        privacy = "no";
        connect = "true";
        if(mapType.equals("get_location_private")){
            map.clear();
            if(bmImg == null){
                new AsyncCaller().execute();
            }else{
                getCurrentLocation();
            }
        }else {
            map.clear();
            if(bmImg == null){
                new AsyncCaller().execute();
            }else{
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if(bmImg == null){
            new AsyncCaller().execute();
        }else{
            getCurrentLocation();
        }

        if(getNightMode.isNightMode(getContext())){
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.night_mode));
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap( 1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public String getAddress(Context context, double lat, double lng) {
        // may not need this
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getActivity())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(getActivity())
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(getContext())
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0) {

                                        int index = locationResult.getLocations().size() - 1;
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();

                                        moveMap();
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng latLng = new LatLng(latitude, longitude);

        if(bmImg != null){

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(bmImg.getWidth(), bmImg.getHeight(), conf);
            Canvas canvas1 = new Canvas(bmp);


            Paint color = new Paint();
            color.setTextSize(35);
            color.setColor(Color.BLACK);

            canvas1.drawBitmap(bmImg, 0,0, color);

            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(currentuserName).icon(BitmapDescriptorFactory.fromBitmap(getCircleBitmap(getResizedBitmap(bmp, 120, 120)))));

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    if(!currentuserName.equals(marker.getTitle())){
                        mapSheet(marker.getTitle());
                    }

                    return false;
                }
            });
            configLocation("set_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude), privacy);

            if(!connect.equals("false")){
                new AsyncMarkers().execute();
            }
        }else {
            Bitmap icon = drawableToBitmap(getResources().getDrawable(R.drawable.profile_icon_map));
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(PreferencesUtils.getUsername(getContext()))
                    .icon(BitmapDescriptorFactory.fromBitmap(getCircleBitmap(icon))));
            configLocation("set_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude), privacy);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {

                    if(!currentuserName.equals(marker.getTitle())){
                        mapSheet(marker.getTitle());
                    }

                    return false;
                }
            });

            if(!connect.equals("false")){
                new AsyncMarkers().execute();
            }
        }

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void mapSheet(String username)
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.activity_map_sheet);

        ImageView profilePic, countryPic, verificationImage;
        TextView usernameView, hobbyView, redView, greenView, purpleView;
        Button profileButton, followButton, messageButton;

        profilePic = bottomSheetDialog.findViewById(R.id.profile_pic);
        countryPic = bottomSheetDialog.findViewById(R.id.country_image);
        usernameView = bottomSheetDialog.findViewById(R.id.profile_username);
        hobbyView = bottomSheetDialog.findViewById(R.id.profile_hobbyname);
        redView = bottomSheetDialog.findViewById(R.id.red_label);
        greenView = bottomSheetDialog.findViewById(R.id.green_label);
        purpleView = bottomSheetDialog.findViewById(R.id.purple_label);
        profileButton = bottomSheetDialog.findViewById(R.id.visitButton);
        followButton = bottomSheetDialog.findViewById(R.id.followButton);
        messageButton = bottomSheetDialog.findViewById(R.id.messageButton);
        verificationImage = bottomSheetDialog.findViewById(R.id.verification_image);

        messageButton.setEnabled(false);

        configMap(usernameView, hobbyView, profilePic, countryPic, username, verificationImage);
        configurePrivacy(username, messageButton);
        checkFollow(username, followButton);
        getUnread(username);
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile(username);
            }
        });

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow(username, followButton);
            }
        });
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToMessages(username);
            }
        });
    }

    private void getUnread(String username)
    {
        String currentuserName = PreferencesUtils.getUsername(getContext());
        String URL = getResources().getString(R.string.Url_Connect) + "Social.php";

        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                try {
                    JSONArray jsonArray = new JSONArray(result_code);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        unreadCount = (String.valueOf(item.getInt("message_count")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("check", String.valueOf(e));
                    unreadCount = "0";
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                unreadCount = "0";
            }
        }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();

                param.put("type", "unread_count");
                param.put("sender", currentuserName);
                param.put("receiver", username);
                return param;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void GoToMessages(String username)
    {
        String imageUrl = getResources().getString(R.string.Url_Connect) + "images/" + username + ".png";

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("image", imageUrl);
        intent.putExtra("unreadcount", unreadCount);
        intent.putExtra("verification", configVerification);
        startActivity(intent);
    }

    private void goToProfile(String username)
    {
        String imageUrl = getResources().getString(R.string.Url_Connect) + "images/" + username + ".png";

        Intent intent = new Intent(getContext(), FriendProfileActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("imageURL", imageUrl);
        startActivity(intent);
    }

    private void follow(String username, Button followButton)
    {
        String URL = getResources().getString(R.string.Url_Connect) + "Social.php";
        if(privacyType.equals("public")){
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String result_code) {
                    if(result_code.equals("followed")){
                        followButton.setText("Following");
                    }else {
                        followButton.setText("Follow");
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            }){

                String currentuserName = PreferencesUtils.getUsername(getContext());
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> param = new HashMap<>();

                    param.put("type", "follow");
                    param.put("follower", currentuserName);
                    param.put("followed", username);
                    return param;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getmInstance(getContext()).addToRequestQueue(request);
        }else {
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String result_code) {
                    if(result_code.equals("requested")){
                        followButton.setText("Requested");
                    }else {
                        followButton.setText("Follow");
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }){

                String currentuserName = PreferencesUtils.getUsername(getContext());
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> param = new HashMap<>();

                    param.put("type", "request");
                    param.put("request", currentuserName);
                    param.put("requested", username);
                    return param;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getmInstance(getContext()).addToRequestQueue(request);
        }
    }

    private void checkRequest(String username, Button followButton)
    {
        String URL = getResources().getString(R.string.Url_Connect) + "Social.php";

        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                if(result_code.equals("requested")){
                    followButton.setText("Requested");
                }else {
                    followButton.setText("Follow");
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){

            String currentuserName = PreferencesUtils.getUsername(getContext());
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();

                param.put("type", "request_check");
                param.put("request", currentuserName);
                param.put("requested", username);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void checkFollow(String username, Button followButton)
    {
        String URL = getResources().getString(R.string.Url_Connect) + "Social.php";

        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                if(result_code.equals("followed")){
                    followButton.setText("Following");
                }else {
                    checkRequest(username, followButton);
                }
                Log.d("hey", result_code);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){

            String currentuserName = PreferencesUtils.getUsername(getContext());
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();

                param.put("type", "follow_check");
                param.put("follower", currentuserName);
                param.put("followed", username);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void configurePrivacy(String username, Button messageButton)
    {
        String URL = getResources().getString(R.string.Url_Connect) + "settings/Privacy.php";

        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                if (result_code.equals("private"))
                {
                    privacyType = "private";
                    messageButton.setText("Private");
                } else {
                    privacyType = "public";
                    messageButton.setText("Message");
                    messageButton.setEnabled(true);
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                privacyType = "no";
                messageButton.setText("Message");
                messageButton.setEnabled(true);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("username", username);
                param.put("config", "get");
                return param;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void configMap(TextView usernameView, TextView hobbyView, ImageView profilePic, ImageView countryPic, String userName, ImageView verification_image)
    {
        usernameView.setText(userName);

        String imageUrl = getResources().getString(R.string.Url_Connect) + "images/" + userName + ".png";
        Glide.with(getContext())
                .load(imageUrl)
                .placeholder(R.drawable.profile_icon).into(profilePic);

        String URL = getResources().getString(R.string.Url_Connect) + "configUser.php";
        StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String result_code) {
                try {
                    JSONObject jsonObject = new JSONObject(result_code);
                    String configHobby = jsonObject.getString("hobby");
                    String configCountry = jsonObject.getString("country");
                    String configVerification = jsonObject.getString("verification");

                    String URL_FLAG = getResources().getString(R.string.Url_Connect) + "flags/" + configCountry + ".png";
                    if(!configCountry.equals("")){
                        Glide.with(getContext()).load(URL_FLAG).placeholder(R.drawable.question_mark).into(countryPic);
                    }


                    if(configHobby.equals("")){
                        hobbyView.setText("regular");
                    }else{
                        hobbyView.setText(configHobby);
                    }

                    if(configVerification.equals("red")){
                        verification_image.setColorFilter(Color.parseColor("#FF0000"));
                        verification_image.setVisibility(View.VISIBLE);
                    }else if(configVerification.equals("green")){
                        verification_image.setColorFilter(Color.parseColor("#00FF00"));
                        verification_image.setVisibility(View.VISIBLE);
                    }else if(configVerification.equals("purple")){
                        verification_image.setColorFilter(Color.parseColor("#FFBB86FC"));
                        verification_image.setVisibility(View.VISIBLE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("check", String.valueOf(e));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("username", userName);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getmInstance(getContext()).addToRequestQueue(request);
    }

    private void configLocation(String type, String username, String longitude, String latitude, String privacy)
    {
        Call<List<LocationModel>> call = ApiClient.apiInterface().configLocation(type, username, longitude, latitude, privacy);
        call.enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(Call<List<LocationModel>> call, Response<List<LocationModel>> response) {
                List<LocationModel> locationUpdate = response.body();

            }
            @Override
            public void onFailure(Call<List<LocationModel>> call, Throwable t) {
                Log.d("error", t.toString());
            }

        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        deleteLocation("delete_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        deleteLocation("delete_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude));
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {

    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
        }
        @Override
        protected Void doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            String IMAGE_URL = getResources().getString(R.string.Url_Connect) + "images/";
            String img = currentuserName + ".png";
            String picassoURL = IMAGE_URL + img;

            try {
                URL url = new URL(picassoURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCurrentLocation();
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread
        }

    }

    private class AsyncMarkers extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
        }
        @Override
        protected Void doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String URL = getResources().getString(R.string.Url_Connect) + "location.php";

            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String result_code) {
                    try {
                        JSONArray jsonArray = new JSONArray(result_code);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            String name = item.getString("username");
                            String privacy = item.getString("privacy");

                            if(privacy.equals("no")){
                                if(!name.equals(currentuserName)){
                                    double longitude = Double.parseDouble(item.getString("longitude"));
                                    double latitude = Double.parseDouble(item.getString("latitude"));

                                    String IMAGE_URL = getResources().getString(R.string.Url_Connect) + "images/";
                                    String img = name + ".png";
                                    String picassoURL = IMAGE_URL + img;

                                    LatLng latLng = new LatLng(latitude, longitude);

                                    Thread thread = new Thread(new Runnable() {

                                        @Override
                                        public void run() {

                                            try {
                                                URL url = new URL(picassoURL);
                                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                                conn.setDoInput(true);
                                                conn.connect();
                                                InputStream is = conn.getInputStream();
                                                icon = BitmapFactory.decodeStream(is);

                                                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                                                Bitmap bmp = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), conf);
                                                Canvas canvas1 = new Canvas(bmp);


                                                Paint color = new Paint();
                                                color.setTextSize(35);
                                                color.setColor(Color.BLACK);

                                                canvas1.drawBitmap(icon, 0,0, color);

                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Code here will run in UI thread
                                                        map.addMarker(new MarkerOptions()
                                                                .position(latLng)
                                                                .draggable(true)
                                                                .title(name).icon(BitmapDescriptorFactory.fromBitmap(getCircleBitmap(getResizedBitmap(bmp, 120, 120)))));

                                                        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                            @Override
                                                            public boolean onMarkerClick(@NonNull Marker marker) {
                                                                if(!currentuserName.equals(marker.getTitle())){
                                                                    mapSheet(marker.getTitle());
                                                                }
                                                                return false;
                                                            }
                                                        });
                                                    }
                                                });

                                            } catch (IOException e) {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Bitmap iconImg = drawableToBitmap(getResources().getDrawable(R.drawable.profile_icon_map));
                                                        map.addMarker(new MarkerOptions()
                                                                .position(latLng)
                                                                .draggable(true)
                                                                .title(name)
                                                                .icon(BitmapDescriptorFactory.fromBitmap(getCircleBitmap(iconImg))));
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    thread.start();

                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }){


                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> param = new HashMap<>();

                    param.put("type", mapType);
                    param.put("username", currentuserName);
                    return param;
                }

            };

            request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getmInstance(getContext()).addToRequestQueue(request);

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread
        }

    }

    private void deleteLocation(String type, String username, String longitude, String latitude)
    {
        Call<List<LocationModel>> call = ApiClient.apiInterface().configLocation(type, username, longitude, latitude, privacy);
        call.enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(Call<List<LocationModel>> call, Response<List<LocationModel>> response) {
                List<LocationModel> locationUpdate = response.body();

            }
            @Override
            public void onFailure(Call<List<LocationModel>> call, Throwable t) {
                Log.d("error", t.toString());
            }

        });
    }

    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        // do request on edit profile
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);

        } else {
            if(bmImg == null){
                new AsyncCaller().execute();
            }else{
                if (isGPSEnabled()) {
                    getCurrentLocation();
                }else {
                    turnOnGPS();
                }
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if(bmImg == null){
                    new AsyncCaller().execute();
                }else{
                    if (isGPSEnabled()) {
                        getCurrentLocation();
                    }else {
                        turnOnGPS();
                    }
                }
            }
            return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter("update"));

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        deleteLocation("delete_location", currentuserName, String.valueOf(longitude), String.valueOf(latitude));
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);

            checkPermission(permission, 201);
        }
    }


    public static HomeFragment newInstance() {

        HomeFragment f = new HomeFragment();
        Bundle b = new Bundle();

        f.setArguments(b);

        return f;
    }
}
