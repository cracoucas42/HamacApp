package com.hamac.hamacapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hamac.hamacapp.R;
import com.hamac.hamacapp.data.Hamac;
import com.hamac.hamacapp.data.Navigation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class HamacActivity extends AppCompatActivity {

    private boolean firstLaunch_flag = true;
    private final int PICK_IMAGE_REQUEST = 42;
    private String EXTRA_STORAGE_REFERENCE_KEY = "";
    private ImageView addPhotoBtn;
    private ImageView saveHamacBtn;
    private ImageView closeHamacBtn;
    private ImageView photoView1;
    private ImageView photoView2;
    private ImageView photoView3;
    private ImageView photoView4;
    private ImageView photoView5;
    private Uri filePath;
    private String currentDir;
    private Hamac selectedHamac = new Hamac();
    private TextView hamacTitleView;
    private TextView hamacDescriptionView;
    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hamac);

        //Get ShareData
        Intent myIntent = new Intent();
        myIntent = HamacActivity.this.getIntent();
        firstLaunch_flag = myIntent.getBooleanExtra("FIRST_LAUNCH", true);
        selectedHamac = (Hamac) myIntent.getSerializableExtra("HAMAC_SELECTED");

//        //Manage FireBase storage for photos view
//        storage = FirebaseStorage.getInstance();
//        storageReference = storage.getReference();

        //Configure addPhotoBtn add_image_hamac_details
        addPhotoBtn = findViewById(R.id.iv_add_image_hamac_details);
        addPhotoBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                choosePhoto();
            }
        });

        //Configure closeHamacBtn save_hamac_details
        closeHamacBtn = findViewById(R.id.iv_save_hamac_details);
        closeHamacBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uploadPhoto(storageReference, filePath, currentDir);
            }
        });

        //Configure saveHamacBtn save_hamac_details
        saveHamacBtn = findViewById(R.id.ib_hamac_details_close);
        saveHamacBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(HamacActivity.this, MapsActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            }
        });

        //########### Populate Data to Hamac Layout
        //Title textView
        hamacTitleView = findViewById(R.id.tv_hamac_title);
        hamacTitleView.setText(selectedHamac.getName());

        //Description
        hamacDescriptionView = findViewById(R.id.tv_hamac_description);
        hamacDescriptionView.setText(selectedHamac.getDescription());

        //Take data view from activity to ui view into layout
        photoView1 = findViewById(R.id.iv_hamac_photo1);
        photoView2 = findViewById(R.id.iv_hamac_photo2);
        photoView3 = findViewById(R.id.iv_hamac_photo3);
        photoView4 = findViewById(R.id.iv_hamac_photo4);
        photoView5 = findViewById(R.id.iv_hamac_photo5);

        ImageView[] imageViews = {photoView1, photoView2, photoView3, photoView4, photoView5};

        String[] photos = {selectedHamac.getPhotoUrl_1(),
                selectedHamac.getPhotoUrl_2(),
                selectedHamac.getPhotoUrl_3(),
                selectedHamac.getPhotoUrl_4(),
                selectedHamac.getPhotoUrl_5()};
        
        File extStDir = new File(HamacActivity.this.getFilesDir(), "HamacApp");
        String currentDirLocal_small = extStDir + "/" +selectedHamac.getId() + "/SmallPhotos/";

        File smallPhotosLocalDirectory = new File(currentDirLocal_small);
        File localPhoto = null;
        try
        {
            if (smallPhotosLocalDirectory.exists())
            {
                Log.i("CHECK CURRENT DIR:", "Current DIR for selected Hamac exixts: " + smallPhotosLocalDirectory.getAbsolutePath());
                Log.i("CHECK CURRENT PHOTO:", "Current PHOTO SEARCHED:\nPATH: " + smallPhotosLocalDirectory.getAbsoluteFile() + "\nNAME: " + selectedHamac.getPhotoUrl_1().substring(0, selectedHamac.getPhotoUrl_1().length() - 4) + "\nEXT: " + selectedHamac.getPhotoUrl_1().substring(selectedHamac.getPhotoUrl_1().length() - 4));
                
                for(int i=0; i < photos.length; i++)
                {
                    localPhoto = new File(smallPhotosLocalDirectory.getAbsolutePath(),  photos[i]);
                    if (localPhoto.exists())
                    {
                        Log.i("CHECK CURRENT PHOTO:",
                                "Current PHOTO AbsolutePath: " + localPhoto.getAbsolutePath()
                                        + "\nCanonicalPath: " + localPhoto.getCanonicalPath()
                                        + "\nPath: " + localPhoto.getPath()
                                        + "\nName: " + localPhoto.getName());
                        //Set to imageView PHOTO 1
                        Bitmap myBitmap = BitmapFactory.decodeFile(localPhoto.getAbsolutePath());
                        imageViews[i].setImageBitmap(myBitmap);
                    }
                    else
                    {
                        Log.e("CHECK CURRENT PHOTO:", "Current PHOTO for selected Hamac doesn't exixt: " + localPhoto.getAbsolutePath());
                    }
                }
            }
            else
            {
                Log.e("CHECK CURRENT DIR:", "Current DIR for selected Hamac doesn't exixt: " + smallPhotosLocalDirectory.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            Log.e("CHECK CURRENT:", "Current DIR or PHOTO for selected Hamac doesn't exixt:\nDIR: " + smallPhotosLocalDirectory.getAbsolutePath() + "\nFILE: " + localPhoto.getAbsolutePath());
        }


        //Start Manage Toolbar > how to share this code into several Activities
        Toolbar hamacToolBar = findViewById(R.id.hamac_toolbar);
        HamacActivity.this.setSupportActionBar(hamacToolBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        /*MenuItem item = menu.findItem(R.id.button_item);
        Button btn = item.getActionView().findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "Toolbar Button Clicked!", Toast.LENGTH_SHORT).show();
            }
        });*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Navigation nav = new Navigation();
        Intent myIntent = nav.getIntentfromSelectedActivity(HamacActivity.this, item.getItemId(), firstLaunch_flag);

        //Can share another Bundle if necessary
        //myIntent.putExtra("lastName", "Your Last Name Here");
        myIntent.putExtra("FIRST_LAUNCH", firstLaunch_flag);
//        if (currentActivity)
        startActivity(myIntent);

        return true;
    }

    private void choosePhoto()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void downloadPhoto(final ImageView mImageView, StorageReference mStorageReference, String mCurrentDir)
    {
        final long ONE_MEGABYTE = 4096 * 4096;
        mStorageReference.child(mCurrentDir).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mImageView.setImageBitmap(bitmap);
            }
        });
    }

    private void uploadPhoto(StorageReference mStorageReference, Uri mFilePath, String mCurentDir)
    {
        if(mFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageReference.child(mCurentDir + "/"+ UUID.randomUUID().toString());
            ref.putFile(mFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            progressDialog.dismiss();
//                        Toast.makeText(HamacActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
//                        Toast.makeText(HamacActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                photoView1.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (storageReference != null) {
            outState.putString(EXTRA_STORAGE_REFERENCE_KEY, storageReference.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final String stringRef = savedInstanceState.getString(EXTRA_STORAGE_REFERENCE_KEY);

        if (stringRef == null) {
            return;
        }

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
        List<FileDownloadTask> tasks = storageReference.getActiveDownloadTasks();
        for( FileDownloadTask task : tasks ) {
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Tuts+", "download successful!");
                }
            });
        }
    }
}
