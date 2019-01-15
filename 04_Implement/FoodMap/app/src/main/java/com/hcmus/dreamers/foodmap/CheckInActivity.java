package com.hcmus.dreamers.foodmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.View.GridViewItem;
import com.hcmus.dreamers.foodmap.adapter.ImageCheckInListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckInActivity extends AppCompatActivity {

    static final String TAG = "CheckInActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MAX_SELECTED_IMAGE = 3;
    int restID;
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    GridView grdCheckInImage;
    ImageView imgCamera;
    ImageView imgFacebook;

    List<Bitmap> bitmapList = new ArrayList<Bitmap>();
    List<Bitmap> selectedList = new ArrayList<Bitmap>();

    String mCurrentPhotoPath;
    Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get restID moved from RestaurantInfoActivity
        Intent intent = getIntent();
        restID = intent.getIntExtra("restID", 0);
        if(restID == 0){
            Log.e(TAG, "Fail to get restID");
        }

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_check_in);

        //set header toolbar in the layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlbCheckIn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        grdCheckInImage = (GridView) findViewById(R.id.grdCheckInImage);
        imgCamera = (ImageView) findViewById(R.id.imgCamera);
        imgFacebook = (ImageView) findViewById(R.id.imgFacebook);

        final ImageCheckInListAdapter adapter = new ImageCheckInListAdapter(CheckInActivity.this,
                R.layout.adapter_image_check_in_list,
                bitmapList);

        grdCheckInImage.setAdapter(adapter);

        grdCheckInImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView imageView = (ImageView) view.findViewById(R.id.imgCheckIn);

                if(!selectedList.contains(bitmapList.get(position))){
                    if(selectedList.size() != MAX_SELECTED_IMAGE) {
                        selectedList.add(bitmapList.get(position));
                        imageView.setImageResource(R.drawable.ic_tick_frame);
                    }
                    else{
                        Toast.makeText(CheckInActivity.this, "Maximum number of photos selected", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    selectedList.remove(bitmapList.get(position));
                    imageView.setImageResource(0);
                }

            }
        });


        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dispatchTakePictureIntent();
                    adapter.notifyDataSetChanged();
            }
        });

        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedList.size() != 0)
                {
                    //Init Facebook
                    callbackManager = CallbackManager.Factory.create();
                    shareDialog = new ShareDialog(CheckInActivity.this);

                    shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                        @Override
                        public void onSuccess(Sharer.Result result) {
                            Toast.makeText(CheckInActivity.this, "success", Toast.LENGTH_LONG).show();
                            if(restID != 0)
                            {
                                FoodMapApiManager.addCheckIn(restID, Guest.getInstance().getEmail(), new TaskCompleteCallBack() {
                                    @Override
                                    public void OnTaskComplete(Object response) {
                                        int code = (int) response;
                                        if(code == FoodMapApiManager.SUCCESS) {
                                            FoodMapManager.addCheckIn(restID);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(CheckInActivity.this, "cancel", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(CheckInActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    List<SharePhoto> photoList = new ArrayList<SharePhoto>();
                    SharePhotoContent shareContent;
                    for(int i = 0; i < selectedList.size() && i < 3; i++)
                    {
                        photoList.add(new SharePhoto.Builder()
                                .setBitmap(selectedList.get(i))
                                .build()
                        );
                    }

                    switch (photoList.size()) {
                        case 1:
                            shareContent = new SharePhotoContent.Builder()
                                    .addPhoto(photoList.get(0))
                                    .build();
                            break;
                        case 2:
                            shareContent = new SharePhotoContent.Builder()
                                    .addPhoto(photoList.get(0))
                                    .addPhoto(photoList.get(1))
                                    .build();
                            break;
                        default:
                            shareContent = new SharePhotoContent.Builder()
                                    .addPhoto(photoList.get(0))
                                    .addPhoto(photoList.get(1))
                                    .addPhoto(photoList.get(2))
                                    .build();
                            break;

                    }

                    if (ShareDialog.canShow(SharePhotoContent.class)) {
                        shareDialog.show(CheckInActivity.this, shareContent);
                    }
                }
                else{
                    Toast.makeText(CheckInActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    //Take phato function
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("CreateImageFile", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.hcmus.dreamers.foodmap",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bitmap imageBitmap;
            try
            {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                bitmapList.add(imageBitmap);
            }catch (Exception e)
            {
                //THis line should never run
                Log.e("Get Bitmap",e.getMessage());
            }
        }
/*        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){
            e.printStackTrace();
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
