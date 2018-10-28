package com.hamac.hamacapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.hamac.hamacapp.R;
import com.hamac.hamacapp.data.Hamac;

import java.io.File;
import java.util.ArrayList;

public class SplashScreenActivity extends Activity {
    private static int SPLASH_TIME_OUT = 3000;
//    private LocationManager lm;
    private boolean firstLaunch_flag = true;
    private View splashScreenView;
    private DatabaseReference mDatabase;
    private ValueEventListener mMessageListener;
    private ArrayList<Hamac> hamacList = new ArrayList<Hamac>();
    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private String currentDir_original = "";
    private String currentDir_small = "";
//    private Location currentLocation;
    ProgressDialog progressDialog;
    private int photosToDownload_counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        splashScreenView = this.findViewById(R.id.splash_screen_view);

        //Get Data Offline persistent
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Manage FireBase From here and pass HamacList To MapsActivity by Intent

        //Manage PERMISSIONS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED |
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == true |
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) == true |
                    shouldShowRequestPermissionRationale(Manifest.permission.INTERNET) == true |
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) == true)
                {
                    explain();
                }
                else
                {
                    //Ask for Permission
                    askForPermission();
                    Toast.makeText(getApplicationContext(), "Request PERMISSION", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            //REQUEST PERMISSION NOT NEEDED
            Toast.makeText(getApplicationContext(), "PERMISSION OK", Toast.LENGTH_LONG).show();
        }

        //Manage DataBase
        mDatabase = FirebaseDatabase.getInstance().getReference("HAMAC_LIST_ONLINE");
        //Manage auhorisation to FireBase Storage
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            // do your stuff
        }
        else
        {
            Log.i("SIGNING : ", "Before Signing !");
            signInAnonymously();
        }
        //Manage FireBase storage for photos view
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Get Data From FireBase DataBase
        ValueEventListener messageListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    Toast.makeText(getBaseContext(), "Read FireBase Before", Toast.LENGTH_SHORT).show();
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        Hamac currentHamac = new Hamac(
                                ds.child("id").getValue(String.class),
                                ds.child("name").getValue(String.class),
                                ds.child("description").getValue(String.class),
                                ds.child("lat").getValue(Double.class),
                                ds.child("lng").getValue(Double.class),
                                ds.child("user").getValue(String.class),
                                ds.child("photoUrl_1").getValue(String.class),
                                ds.child("photoUrl_2").getValue(String.class),
                                ds.child("photoUrl_3").getValue(String.class),
                                ds.child("photoUrl_4").getValue(String.class),
                                ds.child("photoUrl_5").getValue(String.class));

                        Log.d("Reading Firebase", "Current Hamac name: " + ds.child("name").getValue(String.class)
                                + " LNG :" + ds.child("lng").getValue(Double.class)
                                + " LAT :" + ds.child("lat").getValue(Double.class));



                        //Original ## keep for splash view
                        // Create a reference with an initial file path and name
//                        pathReference = storageReference.child(currentDir_original);
//                        downloadPhotos(storageReference, currentDir_original, photos, originalPhotosDirectory);
//
//                        String arrival = ds.child("Arrival").getValue(String.class);
//                        String departure = ds.child("Departure").getValue(String.class);
//                        String time = ds.child("Time").getValue(String.class);
//                        Log.d("TAG", arrival + " / " + departure  + " / " + time);
                        // Get Post object and use the values to update the UI
//                        Hamac currentHamac = ds.child(ds.getKey()).getValue(Hamac.class);

//                        Log.d("Reading Firebase", "Current Hamac : " + ds.getKey());

                        hamacList.add(currentHamac);
                    }
                }
                Toast.makeText(getBaseContext(), "Read FireBase After HamacList SIZE : " + hamacList.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.e("Error Reading Firebase", "onCancelled: Failed to read message");
            }
        };
        mDatabase.addValueEventListener(messageListener);
        // copy for removing at onStop()
        mMessageListener = messageListener;

        downloadPhotosToLocalFolder(storageReference, hamacList);

//        Toast.makeText(getApplicationContext(), "Start to delay time...", Toast.LENGTH_LONG).show();
        //Manage Timing
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i = new Intent(SplashScreenActivity.this, MapsActivity.class);

                i.putExtra("HAMAC_LIST_FROM_ONLINE_DB", hamacList);

                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);

//        Toast.makeText(getApplicationContext(), "Stop to delay time...", Toast.LENGTH_LONG).show();
    }

    private void askForPermission()
    {
        ActivityCompat.requestPermissions(this,
                new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                1);
    }

    private void explain()
    {
        Snackbar.make(splashScreenView, "Cette permission est nécessaire pour appeler", Snackbar.LENGTH_LONG).setAction("Activer", new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                askForPermission();
            }
        }).show();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (mMessageListener != null)
        {
            mDatabase.removeEventListener(mMessageListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PackageManager.PERMISSION_GRANTED)
        {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
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

            }
            else
            {
                // Permission was denied. Display an error message.
                Toast.makeText(getApplicationContext(), "PERMISSION WAS NOT GRANTED", Toast.LENGTH_LONG).show();
            }
        }
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

    private void downloadPhotos(StorageReference mStorageReference, String mCurrentDir, String[] photos, File folder)
    {
        final long ONE_MEGABYTE = 4096 * 4096;

        for (int i = 0; i < photos.length; i++)
        {
            Log.i("DOWNLOAD PHOTOSS: ", "Current REF : " + mStorageReference + " Current DIR : " + mCurrentDir + photos[i]);
            if (photos[i].length() > 1)
            {
                mStorageReference.child(mCurrentDir + photos[i]).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>()
                {
                    @Override
                    public void onSuccess(byte[] bytes)
                    {
                        //dialog dismiss

                    }
                });
            }
        }
    }
    private void downloadPhotosToLocalFolder(StorageReference mStorageReference, ArrayList<Hamac> hamacList)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading...");
        progressDialog.setMessage(null);
        progressDialog.show();

        // - 1 because of indice i which start to 0
        final int counter = countPhotoToDownload(hamacList) - 1;

        for (int i=0; i < hamacList.size(); i++)
        {
            Hamac currentHamac = hamacList.get(i);
            currentDir_original =  currentHamac.getId() + "/OriginalPhotos/";
            currentDir_small =  currentHamac.getId() + "/SmallPhotos/";
            //If there are some photos, get the smallFormat and put them into local directory
            //Create original (for splash view) and small/resized one for display without a large consommation
            //Create folder !exist
            String smallPhotos_folderPath = Environment.getExternalStorageDirectory() + currentDir_small;
            File smallPhotosDirectory = new File(smallPhotos_folderPath);
            if (!smallPhotosDirectory.exists())
                smallPhotosDirectory.mkdirs();

            String originalPhotos_folderPath = Environment.getExternalStorageDirectory() + currentDir_original;
            File originalPhotosDirectory = new File(originalPhotos_folderPath);
            if (!originalPhotosDirectory.exists())
                originalPhotosDirectory.mkdirs();

            String[] photos = {currentHamac.getPhotoUrl_1(),
                    currentHamac.getPhotoUrl_2(),
                    currentHamac.getPhotoUrl_3(),
                    currentHamac.getPhotoUrl_4(),
                    currentHamac.getPhotoUrl_5()};

            // Create a reference with an initial file path and name
            StorageReference pathReference = storageReference.child(currentDir_small);

            //Download small photos from Firebase storage
            if (mStorageReference != null)
            {
                for (int j = 0; j < photos.length; j++)
                {
                    if (photos[j].length() > 1)
                    {
                        Log.i("DOWNLOAD PHOTOSS: ", "Current REF : " + mStorageReference + " Current DIR : " + currentDir_small + photos[j] + " INDEX > " + j + " Counter > " + counter);
                        final File localFile = new File (smallPhotosDirectory, photos[j]);

                        final int finalI = j;
                        mStorageReference.child(currentDir_small + photos[j]).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                            {
    //                        Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
    //                        imageView.setImageBitmap(bmp);
                                if (finalI == counter)
                                    progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception exception)
                            {
                                if (finalI == counter)
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(SplashScreenActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot)
                            {
                                // progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                // percentage in progress dialog
                                progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                            }
                        });
                    }
                }
            }
            else
            {
                Toast.makeText(SplashScreenActivity.this, "Upload file before downloading", Toast.LENGTH_LONG).show();
            }
        }
    }
    private int countPhotoToDownload(ArrayList<Hamac> hamacList)
    {
        int counter = 0;

        for (int j=0; j < hamacList.size(); j++)
        {
            String[] photos = {hamacList.get(j).getPhotoUrl_1(),
                    hamacList.get(j).getPhotoUrl_2(),
                    hamacList.get(j).getPhotoUrl_3(),
                    hamacList.get(j).getPhotoUrl_4(),
                    hamacList.get(j).getPhotoUrl_5()};

            for (int i = 0; i < photos.length; i++)
            {
                if (photos[i].length() > 1)
                {
                    counter = counter + 1;
                }
            }
        }
        return counter;
    }
}
