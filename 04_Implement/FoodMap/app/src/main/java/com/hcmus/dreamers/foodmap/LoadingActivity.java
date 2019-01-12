package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;


public class LoadingActivity extends AppCompatActivity {

    private static final int PERMISSION_CODEREQUEST = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);

        FoodMapApiManager.getRestaurant(LoadingActivity.this, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                int code = (int) response;
                if (code == FoodMapApiManager.SUCCESS){

                    FoodMapApiManager.getCatalog(LoadingActivity.this, new TaskCompleteCallBack() {
                        @Override
                        public void OnTaskComplete(Object response) {
                            int code = (int) response;
                            if (code == FoodMapApiManager.SUCCESS){
                                // Kiểm tra đã cấp các quyền truy cập.
                                checkPermission();
                            }
                            else if (code == ConstantCODE.NOTINTERNET){
                                getDataLocal("Kiểm tra kết nối internet");
                            }
                            else {
                                getDataLocal("Error");
                            }
                        }
                    });
                }
                else if (code == ConstantCODE.NOTINTERNET){
                    getDataLocal("Kiểm tra kết nối internet");
                }
                else {
                    getDataLocal("Error");
                }
            }
        });
    }

    private void checkPermission() {
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY};

        boolean isPermissionOK = true;

        for (String permission : permissions){
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED ) {
                isPermissionOK = false;
                break;
            }
        }

        // send request and open map
        if (isPermissionOK) {
            openMap();
        }
        else{
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODEREQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_CODEREQUEST)
        {
            // Kiểm tra tất cả các quyền đều được cấp thành công từ dùng

            boolean allPermissionAreGranted = true;

            for (int i = 0; i <grantResults.length;i++)
            {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    allPermissionAreGranted = false;
                    break;
                }
            }

            if (allPermissionAreGranted || Build.VERSION.SDK_INT < 23){
                openMap();
            } else {
                // Nếu tồn tại 1 quyền truy cập chưa được cấp, hiện thông báo, yêu cầu cấp lại?
                Toast.makeText(LoadingActivity.this,
                        R.string.map_permission_denied,
                        Toast.LENGTH_LONG)
                        .show();
                LoadingActivity.this.finish();
            }
        }
    }

    private void openMap() {

        // Chuyển tới bản đồ chính
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);

        LoadingActivity.this.finish();
    }

    void getDataLocal(final String msgError){
        FoodMapManager.getDataFromDatabase(LoadingActivity.this, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                if ((int)response == ConstantCODE.SUCCESS){
                    checkPermission();
                }
                else {
                    Toast.makeText(LoadingActivity.this, msgError, Toast.LENGTH_LONG).show();
                    LoadingActivity.this.finish();
                }
            }
        });
    }
}
