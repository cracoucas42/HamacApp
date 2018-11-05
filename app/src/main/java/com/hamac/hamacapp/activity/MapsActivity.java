package com.hamac.hamacapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hamac.hamacapp.R;
import com.hamac.hamacapp.data.DatabaseHelper;
import com.hamac.hamacapp.data.Hamac;
import com.hamac.hamacapp.data.Navigation;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener,
        LocationSource.OnLocationChangedListener,
        View.OnCreateContextMenuListener,
        Serializable {

    private GoogleMap mMap;
    private int hamacCounter = 0;
    private ArrayList<Hamac> hamacList = new ArrayList<Hamac>();
    //    private LocationListener listener;
    private ProgressDialog dialog;
    private View mView;
    private boolean firstLaunch_flag = false;
    private PopupWindow mPopupWindow;
    private Location currentLocation;
    private LocationManager lm;
    private Circle mCircle;
    private DatabaseHelper hamacDatabaseHelper;
    //    private StorageReference mStorageRef;
    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private ImageView popupMarkerPhotoView;
    private String currentDir = "";
    private ValueEventListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        firstLaunch_flag = true;

        //Get Data from DB and populate HamacList
        hamacDatabaseHelper = new DatabaseHelper(this);
//        populateHamacListFromLocalDb();

        //Manage DataBase
        mDatabase = FirebaseDatabase.getInstance().getReference("HAMAC_LIST_ONLINE");

        //Get ShareData
        Intent myIntent = MapsActivity.this.getIntent();

        //Check first launch from shareData
        //TODO
        firstLaunch_flag = myIntent.getBooleanExtra("FIRST_LAUNCH", true);
        hamacList = (ArrayList<Hamac>) myIntent.getSerializableExtra("HAMAC_LIST_FROM_ONLINE_DB");

        //Get ShareData HamacList
//        if (!firstLaunch_flag)
//        {
//            HamacList = (List<Hamac>) myIntent.getSerializableExtra("HAMAC_LIST");
//        }

        //SAVE OBJECTS when reload Activity like change the orientation of screen
        //Make sure to do this check otherwise someString will
        //cause an error when your activity first loads!
        if (savedInstanceState != null) {
            //Do whatever you need with the string here, like assign it to variable.
            //Log.d("XXX", savedInstanceState.getString(STRING_CONSTANT));
            hamacList = (ArrayList<Hamac>) savedInstanceState.getSerializable("CLICKED_POSITION_LIST");
            firstLaunch_flag = (boolean) savedInstanceState.getSerializable("FIRST_LAUNCH");
        }

        //Start Manage Toolbar > how to share this code into several Activities
        Toolbar hamacToolBar = findViewById(R.id.hamac_toolbar);
        MapsActivity.this.setSupportActionBar(hamacToolBar);
        //End Manage Toolbar

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mView = mapFragment.getView();
    }

//    private void populateHamacListFromLocalDb()
//    {
//        Log.d("MapsActivity", "populateListView: Displaying data in the ListView.");
//        Toast.makeText(getBaseContext(), "Populate List Before", Toast.LENGTH_SHORT).show();
//        //get the data and append to a list
//        Cursor data = hamacDatabaseHelper.getData();
//        ArrayList<String> listData = new ArrayList<>();
//        while(data.moveToNext())
//        {
//            //get the value from the database in column 1
//            //then add it to the ArrayList
//            Hamac restoreHamac = new Hamac(data.getString(1),
//                    data.getString(2),
//                    data.getString(3),
//                    Double.valueOf(data.getString(4)),
//                    Double.valueOf(data.getString(5)));
//
//            hamacList.add(restoreHamac);
//        }
//        Toast.makeText(getBaseContext(), "Populate List After", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
//      shareData.putParcelableArray("HAMAC_LIST", (Parcelable[]) HamacList);
//      myIntent.putExtra("HAMAC_LIST", (Serializable) HamacList);

        Navigation nav = new Navigation();
        Intent myIntent = nav.getIntentfromSelectedActivity(MapsActivity.this, item.getItemId(), firstLaunch_flag);

        //Can share another Bundle if necessary
        //myIntent.putExtra("lastName", "Your Last Name Here");
        myIntent.putExtra("FIRST_LAUNCH", firstLaunch_flag);
//        if (currentActivity)
        startActivity(myIntent);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("CLICKED_POSITION_LIST", hamacList);
        savedInstanceState.putSerializable("FIRST_LAUNCH", firstLaunch_flag);
        //declare values before saving the state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        hamacList = (ArrayList<Hamac>) savedInstanceState.getSerializable("CLICKED_POSITION_LIST");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    /*@Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onMapReady(GoogleMap googleMap)//charge map when ready to show
    {
        mMap = googleMap;
        // Setting onclick event listener for the marker of Hamacs
        mMap.setOnMarkerClickListener(this);
        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(this);

        //Get current Location by GPS
        //Show start position with current GPS position
        //Check the management of the question for permission, mettre l'app en pause le temps de la reponse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED |
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                    1);

            Toast.makeText(getApplicationContext(), "Request PERMISSION", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //REQUEST PERMISSION
            Toast.makeText(getApplicationContext(), "PERMISSION OK", Toast.LENGTH_LONG).show();
        }

        if (!mMap.isMyLocationEnabled())
        {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }

        //Launch of the Location Service
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, listener);

        if (firstLaunch_flag)
        {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait!");
            dialog.show();
        }


        //Replace the Location Button
        if (mView != null &&
                mView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }

        // Change compass position
        if (mView != null &&
                mView.findViewById(Integer.parseInt("1")) != null) {
            // Get the view
            View locationCompass = ((View) mView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationCompass.getLayoutParams();

            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, 150);
            layoutParams.setMarginEnd(30);
        }

        //LoadClickedPositionList if not empty
        //Set Hamac positions from Current List

        Log.d("HAMAC LIST : ", "SIZE: " + hamacList.size());

        for (int i = 0; i < hamacList.size() && hamacList.size() > 0; i++)
        {
            //Get Current position from the hamac list
            LatLng position = new LatLng(hamacList.get(i).getLat(), hamacList.get(i).getLng());
            //Draw marker from list
            MarkerOptions marker = new MarkerOptions();
            marker.position(position).title(hamacList.get(i).getId());
            marker.position(position).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_hamac));
            mMap.addMarker(marker);
            //Draw a circle (area) around a marker to define an area TO NOT put another marker on the same area, about 10m2
            mCircle = mMap.addCircle(new CircleOptions()
                    .center(position)
                    .radius(10)
                    .strokeWidth(3)
                    .strokeColor(Color.argb(60, 105, 160, 245))
                    .fillColor(Color.argb(40, 105, 160, 245))
                    .clickable(true));
            mMap.addCircle(new CircleOptions()
                    .center(position)
                    .radius(0.5)
                    .strokeWidth(1)
                    .strokeColor(Color.argb(200, 35, 110, 25)) //cc236e19 50 35 110 25
                    .fillColor(Color.argb(40, 35, 110, 25)) //f0236e19 240 35 110 25
                    .clickable(true));
        }
    }

    public LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (firstLaunch_flag) {
                firstLaunch_flag = false;
                Log.e("Google", "Location Changed");
                location.setAccuracy(100);

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                else if (location == null)
                    //Get LastLocation Recorded on device
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                //Location get
                if (dialog.isShowing())
                    dialog.dismiss();
                LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
                // Construct a CameraPosition focusing on Paris and animate the camera to that position.
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentPosition)      // Sets the center of the map to Mountain View
                        .zoom(17)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            //Set currentLocation for the application
            currentLocation = location;
            //Add a circle to define the area of an Hamac marker
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(getApplicationContext(), "PERMISSION WAS NOT GRANTED", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location)
    {
        //        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        //Create current Hamac
        //Increment Hamac Count on Hamac Name
        hamacCounter = hamacCounter + 1;
        String currentHamacTitle = "Hamac_" + hamacCounter;
        //Add CurrentHamac to the HamacList
        Hamac currentHamac = new Hamac(currentHamacTitle,
                currentHamacTitle,
                currentHamacTitle + " ###### " + currentHamacTitle + " ###### " + currentHamacTitle,
                currentLocation.getLatitude(),
                currentLocation.getLongitude());

        addAndDrawHamacMarker(currentHamac, currentLocation);
        String hamacId = mDatabase.push().getKey();
        currentHamac.setId(hamacId);
        // pushing current to 'HamacListOnLine' node using the userId
        mDatabase.child(hamacId).setValue(currentHamac);
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        //Get the LocationChanged on the listener
    }

    @Override
    public void onMapClick(LatLng point)
    {
        //        Toast.makeText(getBaseContext(), "MapClick !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
        //When Map is clicked
        // Creating MarkerOptions
        //Increment Hamac Count on Hamac Name
        hamacCounter = hamacCounter + 1;
        String currentHamacTitle = "Hamac_" + hamacCounter;
        //Add CurrentHamac to the HamacList
        Hamac currentHamac = new Hamac(currentHamacTitle,
                currentHamacTitle,
                currentHamacTitle + " ###### " + currentHamacTitle + " ###### " + currentHamacTitle,
                point.latitude,
                point.longitude);

        Location location = new Location("");
        location.setLatitude(point.latitude);
        location.setLongitude(point.longitude);
        addAndDrawHamacMarker(currentHamac, location);
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        //onClick Launch a popup to show details or launch an activity for long details ==> to decide
        Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();

        // Get the application context
//        Context mContext = getApplicationContext();
        // Get the widgets reference from XML layout
//        RelativeLayout mapRelativeLayout = findViewById(R.id.map);
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) MapsActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        String popupDescription = "Je suis la description";

        //Create a Popup from class PopupMarker for marker description
//        markerPopup = new Popup(mView, inflater, mPopupWindow, R.layout.popup_marker,  marker.getTitle(), popupDescription);
        Intent myIntent = MapsActivity.this.getIntent();
        Log.i("SHOW POPUP : ", "Before show POPUP");
        showPopup(inflater, R.layout.popup_marker,  marker.getTitle(), popupDescription);
        Log.i("SHOW POPUP : ", "After show POPUP");
        return true;
    }

    public boolean checkIfCurrentLocationIsInRadiusOfAMarker(Location location, List<Hamac> hamacList)
    {
        boolean checkOk = true;
        float[] distance = new float[2];

    /*
    Location.distanceBetween( mMarker.getPosition().latitude, mMarker.getPosition().longitude,
    mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);
    */
        for (int i=0; i < hamacList.size(); i++)
        {
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(hamacList.get(i).getLat(), hamacList.get(i).getLng()))
                    .radius(5)
                    .strokeWidth(3)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(125, 0, 0, 255))
                    .clickable(true);
            Location.distanceBetween( location.getLatitude(), location.getLongitude(),
                    circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance);

            if( distance[0] > circleOptions.getRadius() )
            {
//                Toast.makeText(getBaseContext(), "Outside, distance from center: " + distance[0] + " radius: " + circleOptions.getRadius(), Toast.LENGTH_LONG).show();
            }
            else
            {
//                Toast.makeText(getBaseContext(), "Inside, distance from center: " + distance[0] + " radius: " + circleOptions.getRadius() , Toast.LENGTH_LONG).show();
                checkOk = false;
            }
        }
        return checkOk;
    }

    public void addAndDrawHamacMarker(Hamac currentHamac, Location location)
    {
        //Check if currentLocation doesn't already exist in HamaList if no ask confirmation else alert info
        //Check If current position is in an area of xxx meters of an existing hamac marker

//        Toast.makeText(getBaseContext(), "Already Exist into a radius of a marker : " + checkIfCurrentLocationIsInRadiusOfAMarker(location, hamacList), Toast.LENGTH_LONG).show();
        if (checkIfCurrentLocationIsInRadiusOfAMarker(location, hamacList))
        {
            final Hamac f_currentHamac = currentHamac;
            final Location f_location = location;

            //Launch an alert Popup to confirm adding a marker
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

            builder.setTitle("Add a Hamac Marker");
            builder.setMessage("Do you confirm ?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // Do do my action here

                    //Get Unique ID from Firebase
                    String hamacId = mDatabase.push().getKey();
                    f_currentHamac.setId(hamacId);
                    //Add currentHamc To HamacList
                    hamacList.add(f_currentHamac);
                    // pushing current to 'HamacListOnLine' node using the userId
                    mDatabase.child(hamacId).setValue(f_currentHamac);

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();
                    try
                    {
                        //Get LatLng from touched point
//                    double touchLat = currentLocation.getLatitude();
//                    double touchLong = currentLocation.getLongitude();
                        LatLng position = new LatLng(f_location.getLatitude(), f_location.getLongitude());

                        //Draw marker on click
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(position).title(f_currentHamac.getId());
                        marker.position(position).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_hamac));
                        mMap.addMarker(marker);

                        //Draw a circle (area) around a marker to define an area TO NOT put another marker on the same area, about 10m2
                        mCircle = mMap.addCircle(new CircleOptions()
                                .center(position)
                                .radius(10)
                                .strokeWidth(3)
                                .strokeColor(Color.argb(60, 105, 160, 245))
                                .fillColor(Color.argb(40, 105, 160, 245))
                                .clickable(true));
                        mMap.addCircle(new CircleOptions()
                                .center(position)
                                .radius(0.5)
                                .strokeWidth(1)
                                .strokeColor(Color.argb(200, 35, 110, 25)) //cc236e19 50 35 110 25
                                .fillColor(Color.argb(40, 35, 110, 25)) //f0236e19 240 35 110 25
                                .clickable(true));

                        //Add Hamac On Local DB
//                        hamacDatabaseHelper.addData(f_currentHamac.getName(),
//                                f_currentHamac.getName(),
//                                f_currentHamac.getDescription(),
//                                String.valueOf(f_currentHamac.getLat()),
//                                String.valueOf(f_currentHamac.getLng()));


                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // I do not need any action here you might
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_alert_info,
                    (ViewGroup) findViewById(R.id.toast_alert_info_layout));

            ImageView image = layout.findViewById(R.id.iv_toast_alert_info);
            image.setImageResource(R.drawable.ic_hamac);
            TextView text = layout.findViewById(R.id.tv_toast_alert_info);
            text.setText("The Hamac Marker already exists on the area pointed !");

            Toast toast = new Toast(getApplicationContext());
            toast.setView(layout);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void showPopup(LayoutInflater inflater,int popupLayout, String hamacId, String description)
    {
        // Get the application context
//        Context mContext = getApplicationContext();
        // Get the widgets reference from XML layout
//        RelativeLayout mapRelativeLayout = findViewById(R.id.map);
        // Initialize a new instance of LayoutInflater service
//         = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(popupLayout,null);

        /*
            public PopupWindow (View contentView, int width, int height)
                Create a new non focusable popup window which can display the contentView.
                The dimension of the window must be passed to this constructor.

                The popup does not provide any background. This should be handled by
                the content view.

            Parameters
                contentView : the popup's content
                width : the popup's width
                height : the popup's height
        */

        // Initialize a new instance of popup window
        if (mPopupWindow != null)
            mPopupWindow.dismiss();
        mPopupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(5.0f);
        }

        // Get a reference for the custom view close button
        ImageButton closeButton = customView.findViewById(R.id.ib_popup_marker_close);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        //Get hamac selected
        Hamac selectedHamac = new Hamac();
        for (int i=0; i < hamacList.size(); i++)
        {
            if (hamacId.equalsIgnoreCase(hamacList.get(i).getId()))
                selectedHamac = hamacList.get(i);
        }

        //Set current Popup-textView with title of current clicked HamacMarker
        TextView popupMarkerTitle = customView.findViewById(R.id.tv_popup_marker_title);
        popupMarkerTitle.setText(selectedHamac.getDescription());

        //Select a photo which exists on firebase Bucket: storageReference + currentDir
        //currentDir = currentDir + "/20180810_142509.jpg";
//        currentDir =  selectedHamac.getId() + "/OriginalPhotos/" + selectedHamac.getPhotoUrl_1();
//
//        //Manage auhorisation to FireBase Storage
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null)
//        {
//            // do your stuff
//        }
//        else
//        {
//            Log.i("SIGNING : ", "Before Signing !");
//            signInAnonymously();
//        }
//        //Manage FireBase storage for photos view
//        storage = FirebaseStorage.getInstance();
//        storageReference = storage.getReference();
//        // Create a reference with an initial file path and name
//        StorageReference pathReference = storageReference.child(currentDir);
//
//        Log.i("CHECK > ", "Current DIR : " + currentDir);
        //Toast.makeText(MapsActivity.this, "Current DIR : " + currentDir, Toast.LENGTH_SHORT).show();
        //Download Image and set into photoView1
//        popupMarkerPhotoView = new ImageView(this);
        popupMarkerPhotoView = customView.findViewById(R.id.iv_popup_photo1);
        displayFirstPhoto(popupMarkerPhotoView, selectedHamac);

        final Hamac f_selectedHamac = selectedHamac;
        // Get a reference for the custom view close button
        ImageButton moreDetailsButton = customView.findViewById(R.id.ib_popup_more_details);
        // Set a click listener for the popup window close button
        moreDetailsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Dismiss the popup window
                mPopupWindow.dismiss();
                Intent myIntent = new Intent(MapsActivity.this, HamacActivity.class);
                myIntent.putExtra("HAMAC_SELECTED", f_selectedHamac);
                startActivity(myIntent);
            }
        });

        /*
            public void showAtLocation (View parent, int gravity, int x, int y)
                Display the content view in a popup window at the specified location. If the
                popup window cannot fit on screen, it will be clipped.
                Learn WindowManager.LayoutParams for more information on how gravity and the x
                and y parameters are related. Specifying a gravity of NO_GRAVITY is similar
                to specifying Gravity.LEFT | Gravity.TOP.

            Parameters
                parent : a parent view to get the getWindowToken() token from
                gravity : the gravity which controls the placement of the popup window
                x : the popup's x location offset
                y : the popup's y location offset
        */
        // Finally, show the popup window at the center location of root relative layout
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAtLocation(mView, Gravity.CENTER,0,0);
    }

    private void displayFirstPhoto(final ImageView mImageView, Hamac selectedHamac)
    {

        File extStDir = new File(MapsActivity.this.getFilesDir(), "HamacApp");
        String currentDirLocal_small = extStDir + "/" +selectedHamac.getId() + "/SmallPhotos/";

        File smallPhotosLocalDirectory = new File(currentDirLocal_small);
        File localPhoto = null;
        String smallPhotosLocalDirectory_name = "";
        String localPhoto_name = "";

        try
        {
            if (smallPhotosLocalDirectory.exists())
            {
                smallPhotosLocalDirectory_name = smallPhotosLocalDirectory.getAbsolutePath();
                Log.i("CHECK CURRENT DIR:", "Current DIR for selected Hamac exixts: " + smallPhotosLocalDirectory_name);
                Log.i("CHECK CURRENT PHOTO:", "Current PHOTO SEARCHED:\nPATH: " + smallPhotosLocalDirectory_name + "\nNAME: " + selectedHamac.getPhotoUrl_1().substring(0, selectedHamac.getPhotoUrl_1().length() - 4) + "\nEXT: " + selectedHamac.getPhotoUrl_1().substring(selectedHamac.getPhotoUrl_1().length() - 4));

                localPhoto = new File(smallPhotosLocalDirectory.getAbsolutePath(),  selectedHamac.getPhotoUrl_1());
                if (localPhoto.exists())
                {
                    localPhoto_name = localPhoto.getAbsolutePath();
                    Log.i("CHECK CURRENT PHOTO:",
                            "Current PHOTO AbsolutePath: " + localPhoto_name);
                    //Set to imageView PHOTO 1
                    Bitmap myBitmap = BitmapFactory.decodeFile(localPhoto.getAbsolutePath());
                    mImageView.setImageBitmap(myBitmap);
                }
                else
                {
                    Log.w("CHECK CURRENT PHOTO:", "Current PHOTO for selected Hamac doesn't exixt: " + localPhoto_name);
                }
            }
            else
            {
                Log.w("CHECK CURRENT DIR:", "Current DIR for selected Hamac doesn't exixt: " + smallPhotosLocalDirectory_name);
            }
        }
        catch (Exception e)
        {
            Log.e("CHECK CURRENT:", "Current DIR or PHOTO for selected Hamac doesn't exixt:\nDIR: " + smallPhotosLocalDirectory_name + "\nFILE: " + localPhoto_name);
        }
//
//
//        Log.i("DOWNLOAD PHOTO : ", "Current REF : " + mStorageReference + " Current DIR : " + mCurrentDir);
//        final long ONE_MEGABYTE = 4096 * 4096;
//        mStorageReference.child(mCurrentDir).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>()
//        {
//            @Override
//            public void onSuccess(byte[] bytes)
//            {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                if (bitmap != null)
//                    mImageView.setImageBitmap(bitmap);
//                else
//                    Log.i("Set Image View", "Set downloaded photo to photoView ! Bytes length : " + bytes.length);
//            }
//        });
    }

    private void signInAnonymously()
    {
        Log.i("SIGNING : ", "Inside Signing anonyously !");
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>()
        {
            @Override
            public void onSuccess(AuthResult authResult)
            {
                // do your stuff
                Log.i("SIGNING ; ", "signInAnonymously:SUCCESS User TOKEN > " + authResult.getUser().getDisplayName());
            }
        }).addOnFailureListener(this, new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("SIGNING ; ", "signInAnonymously:FAILURE", exception);
            }
        });
    }
}