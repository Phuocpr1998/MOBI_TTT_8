package com.hcmus.dreamers.foodmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.hcmus.dreamers.foodmap.View.GridViewItem;
import com.hcmus.dreamers.foodmap.adapter.ImageCheckInListAdapter;

import java.util.ArrayList;
import java.util.List;

public class CheckInActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MAX_SELECTED_IMAGE = 3;
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    GridViewItem grdCheckInImage;
    ImageView imgCamera;
    ImageView imgFacebook;

    List<Bitmap> bitmapList = new ArrayList<Bitmap>();
    List<Bitmap> selectedList = new ArrayList<Bitmap>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_check_in);

        //set header toolbar in the layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlbCheckIn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        grdCheckInImage = (GridViewItem) findViewById(R.id.grdCheckInImage);
        imgCamera = (ImageView) findViewById(R.id.imgCamera);
        imgFacebook = (ImageView) findViewById(R.id.imgFacebook);

        final ImageCheckInListAdapter adapter = new ImageCheckInListAdapter(CheckInActivity.this,
                R.layout.adapter_image_check_in_list,
                bitmapList);

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
                    grdCheckInImage.setAdapter(adapter);
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
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            bitmapList.add(imageBitmap);
        }
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
