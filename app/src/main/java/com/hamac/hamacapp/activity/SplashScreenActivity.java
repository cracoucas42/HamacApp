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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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
import java.util.Collection;
import java.util.List;

import static java.io.File.createTempFile;

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
    ProgressDialog progressDialog;
    //Create task for permission
//    private TaskCompletionSource<DataSnapshot> permissionSource = new TaskCompletionSource<>();
//    private Task permissionTask = permissionSource.getTask();
    //Create task for get data from Firebase
    private TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
    private Task dbTask = dbSource.getTask();
    //Create a task to download photos from storage
//    private Collection<Task> downloadTasks = null;
//    private TaskCompletionSource<FileDownloadTask.TaskSnapshot> downloadPhotosSource = new TaskCompletionSource<FileDownloadTask.TaskSnapshot>();
//    private Task downloadPhotosSourceTask = downloadPhotosSource.getTask();
    //Create task for displaying the splashscreen
//    private TaskCompletionSource<Void> delaySource = new TaskCompletionSource<>();
//    private Task<Void> delayTask = delaySource.getTask();
    //Create a global task to handle event of all of them to launch process after all is complete
    private Task<Void> allTask;
    List<FileDownloadTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        splashScreenView = this.findViewById(R.id.splash_screen_view);

        //Get Data Offline persistent
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

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
                        hamacList.add(currentHamac);
                    }

                }
                Toast.makeText(getBaseContext(), "Read FireBase After HamacList SIZE : " + hamacList.size(), Toast.LENGTH_LONG).show();
                //Permet de declencher le Download une fois la liste remplie sinon on execute la fonction sans liste remplie


//                Here, we're registering a listener for the data we need to continue launching the app.
//                That listener will then trigger dbTask to success or failure via dbSource depending
//                on the callback it received.

                dbSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
//                Here, we're registering a listener for the data we need to continue launching the app.
//                That listener will then trigger dbTask to success or failure via dbSource depending
//                on the callback it received.
                dbSource.setException(databaseError.toException());
                // Failed to read value
                Log.e("Error Reading Firebase", "onCancelled: Failed to read message");
            }
        };
        mDatabase.addValueEventListener(messageListener);
        // copy for removing at onStop()
        mMessageListener = messageListener;


        dbTask.addOnSuccessListener(new OnSuccessListener<DataSnapshot>()
        {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot)
            {
                //Manage progress Dialog
                progressDialog = new ProgressDialog(SplashScreenActivity.this);
                progressDialog.setTitle("Downloading...");
                progressDialog.setMessage(null);
                progressDialog.show();

                downloadPhotosToLocalFolder();
                Log.i("DOWNLOAD PHOTOS:", "INTO SUCCESS AFTER DBTASK |HAMACLIST SIZE: " + hamacList.size());
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                // One of the tasks in the chain failed with an exception.
                // ...
                Log.e("ERROR DB TASKS:", "EXCEPTION DBTASKS: " + e.getMessage() + " / STACKTRACE:" + e.getStackTrace());
            }
        });
    }

    private void askForPermission()
    {
        ActivityCompat.requestPermissions(this,
                new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
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

    private void downloadPhotosToLocalFolder()
    {
        //Manage auhorization to FireBase Storage
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

        // - 1 because of indice i which start to 0
        final int counter = countPhotoToDownload(hamacList) - 1;

        Log.i("INTO DOWNLOAD:", "HAMACLIST SIZE: " + hamacList.size());

        for (int i=0; i < hamacList.size(); i++)
        {
            Hamac currentHamac = hamacList.get(i);
            currentDir_original =  currentHamac.getId() + "/OriginalPhotos/";
            currentDir_small =  currentHamac.getId() + "/SmallPhotos/";
            //If there are some photos, get the smallFormat and put them into local directory
            //Create original (for splash view) and small/resized one for display without a large consommation
            //Create folder !exist

            isExternalStorageReadable();
            isExternalStorageWritable();

//            File extStDir = getPublicAlbumStorageDir("HamacApp");
            File extStDir = new File(SplashScreenActivity.this.getFilesDir(), "HamacApp");

            File smallPhotosDirectory = null;
            try
            {
//                String smallPhotos_folderPath = Environment.getExternalStorageDirectory() + "/" + currentDir_small;
                String smallPhotos_folderPath = extStDir + "/" + currentDir_small;
                smallPhotosDirectory = new File(smallPhotos_folderPath);
                if (!smallPhotosDirectory.exists())
                {
                    smallPhotosDirectory.mkdirs();
                    if (smallPhotosDirectory.exists())
                        Log.i("CREATE DIR:", "Current DIR creating: " + smallPhotosDirectory.getAbsolutePath());
                }
            }
            catch (Exception e)
            {
                Log.e("ERROR DIR:", "CREATING DIRE EXCEPTION: " + e.getMessage() + " | STACKTRACE: " + e.getStackTrace());
            }

//            String originalPhotos_folderPath = Environment.getExternalStorageDirectory() + currentDir_original;
//            File originalPhotosDirectory = new File(originalPhotos_folderPath);
//            if (!originalPhotosDirectory.exists())
//                originalPhotosDirectory.mkdirs();

            final String[] photos = {currentHamac.getPhotoUrl_1(),
                    currentHamac.getPhotoUrl_2(),
                    currentHamac.getPhotoUrl_3(),
                    currentHamac.getPhotoUrl_4(),
                    currentHamac.getPhotoUrl_5()};

            // Create a reference with an initial file path and name
//            StorageReference pathReference = storageReference.child(currentDir_small);
            if (storageReference != null)
            {
                for (int j = 0; j < photos.length; j++)
                {
                    //Log.i("BEFORE DOWNLOAD : ", " Current DIR : " + currentDir_small + photos[j] + " INDEX > " + j + " Counter > " + counter);
                    if (photos[j].length() > 1) {
                        Log.i("DOWNLOAD PHOTO: ", "Current Dir : " + smallPhotosDirectory.getAbsolutePath() + " | Current Photo: " + photos[j] + " INDEX > " + j + " Counter > " + counter);
                        File localFile = null;

                        try
                        {
                            Log.i("PREPARE FILE: ", "Current path : " + smallPhotosDirectory + " | Current Name: " + photos[j].substring(0, photos[j].length() - 4) + "  Current Extension: " + photos[j].substring(photos[j].length() - 4));
                            localFile = new File(smallPhotosDirectory.getAbsolutePath(), photos[j]);
                            if (!localFile.exists())
                            {
//                                localFile.mkdirs();
                                Log.i("FILE CREATED: ", "Current file : " + localFile.getAbsolutePath());
                                final int finalJ = j;
                                storageReference.child(currentDir_small + photos[j]).getFile(localFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                    {
                                        Log.i("DOWNLOAD PHOTO: ", "SUCCESS Current Name : " + photos[finalJ] + " INDEX > " + finalJ + " Counter > " + counter);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception exception)
                                    {
                                        Log.e("DOWNLOAD PHOTO:", "EXCEPTION : " + exception.getMessage());
                                        //                                Toast.makeText(SplashScreenActivity.this, , Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot)
                                    {
                                        // progress percentage
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        // percentage in progress dialog
                                        progressDialog.setMessage("Current Photo: " + photos[finalJ] + " - " + ((int) progress) + "%...");
                                    }
                                });
                            }
                            else
                            {
                                String localFile_Name = localFile.getAbsolutePath();
                                Log.w("FILE EXISTS: ", "File already exists into folder: " + localFile_Name);
                                boolean del = false;
//                                del = localFile.delete();
                                if (del)
                                {
                                    Log.i("FILE DELETED: ", "File has been deleted: " + localFile_Name);
                                }
                                else
                                {
                                    Log.e("FILE DELETED: ", "FAILED TO DELETE FILE: " + localFile_Name);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            Log.e("ERROR FILE:", "CREATING FILE EXCEPTION: " + e.getMessage() + " | STACKTRACE: " + e.getStackTrace());
                        }
                    }
                }
            }
            else
            {
                Toast.makeText(SplashScreenActivity.this, "Upload file before downloading", Toast.LENGTH_LONG).show();
            }
        }
        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
//                List<FileDownloadTask> tasks = storageReference.getActiveDownloadTasks();
        tasks = storageReference.getActiveDownloadTasks();

        Log.i("DOWNLOAD TASK NB:", "NUMBER OF DOWNLOAD TASKS: " + tasks.size());
//                if (tasks.size() > 0)
//                {
//                    // Get the task monitoring the download
//                    FileDownloadTask task = tasks.get(0);
//
//                    // Add new listeners to the task using an Activity scope
//                    task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
//                    {
//                        @Override
//                        public void onSuccess(FileDownloadTask.TaskSnapshot state) {
//                            // Success!
//                            // ...
//                        }
//                    });
//                }


//                Task<Void> allTask;
        allTask = Tasks.whenAll(tasks);
        allTask.addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
//                        DataSnapshot data = dbTask.getResult();
//                        // do something with db data?
//                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                progressDialog.dismiss();
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        //Launch next activity after all is complete
                        Intent i = new Intent(SplashScreenActivity.this, MapsActivity.class);
                        i.putExtra("HAMAC_LIST_FROM_ONLINE_DB", hamacList);
                        startActivity(i);
                        finish();
//                                delaySource.setResult(null);
                    }
                }, SPLASH_TIME_OUT);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                // apologize profusely to the user!
                Log.e("ALL TASK FAILURE:", "ALL TASK FAILURE EXCEPTION: " + e.getMessage() + " | STACKTRACE: " + e.getStackTrace());
                e.printStackTrace();
            }
        });
//        return downloadPhotosSource.getTask();
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
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("DIRECTORY PICTURES:", "Directory not created");
        }
        return file;
    }
}
