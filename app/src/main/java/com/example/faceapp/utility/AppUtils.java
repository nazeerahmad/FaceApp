package com.example.faceapp.utility;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.faceapp.R;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {


    public static final int PERMISSION_REQUEST_CODE =1 ;

    public static void launchMediaScanIntent(Activity activity, Uri imageUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public static void checkAndRequestPermissions(Activity context) {
        if (context != null) {

            int storagewritePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA);

            List<String> listPermissionsNeeded = new ArrayList<>();

            if (storagewritePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(cameraPermission!= PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(context,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    public static ProgressDialog getPrpgress(String msg, Context context){
        ProgressDialog progressDialog =new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private static android.app.AlertDialog exitDialog;
    public static void alertDialog(String msg,final Activity activity) {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.exit_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        TextView ok =dialogView.findViewById(R.id.ok);
        TextView alertMsg = dialogView.findViewById(R.id.alertMsg);
        alertMsg.setText(msg);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    exitDialog.dismiss();
                } catch (Exception e) {
                }
            }
        });

        exitDialog = dialogBuilder.create();
        exitDialog.show();

    }

}
