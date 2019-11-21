package com.example.faceapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faceapp.BuildConfig;
import com.example.faceapp.R;
import com.example.faceapp.utility.AppUtils;
import com.example.faceapp.utility.DetectFaceTask;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.imgCamera)
    Button capture;
    @BindView(R.id.btnProcess)
    Button btnProcessNext;

//    @BindView(R.id.txtSampleDescription)
//    TextView txtSampleDesc;
    @BindView(R.id.txtTakenPicture)
    TextView txtTakenPictureDesc;


    private FaceDetector faceDetector;
    private Bitmap editedBitmap;
    private static final int MY_CAMERA_PERMISSION_REQUEST_CODE = 102;
    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 103;

    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_BITMAP = "bitmap";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppUtils.checkAndRequestPermissions(this);

        faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        btnProcessNext.setOnClickListener(this);

        capture.setOnClickListener(this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppUtils.PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            case MY_CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "please allow permission", Toast.LENGTH_SHORT).show();
                }


        }
    }


    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file;
            try {
                file = getImageFile();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
            else
                uri = Uri.fromFile(file);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
        }
    }

    String currentPhotoPath = "";

    private File getImageFile() {
        String imageFileName = "JPEG_Croped"+ UUID.randomUUID()+".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/");

        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = new File(storageDir, imageFileName);

        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case AppUtils.PERMISSION_REQUEST_CODE:

                    break;
                case CAMERA_ACTION_PICK_REQUEST_CODE:

                        try {
                            processCameraPicture();
//                            Bitmap bitmap =  MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(currentPhotoPath));
//                            imageView.setImageBitmap(bitmap);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    AppUtils.launchMediaScanIntent(this, Uri.parse(currentPhotoPath));
                    break;

            }

        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (Uri.parse(currentPhotoPath) != null) {
            outState.putParcelable(SAVED_INSTANCE_BITMAP, editedBitmap);
            outState.putString(SAVED_INSTANCE_URI, Uri.parse(currentPhotoPath).toString());
        }
        super.onSaveInstanceState(outState);

    }

    private void processCameraPicture() throws Exception {
        Bitmap bitmap =  MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(currentPhotoPath));
        ProgressDialog progressDialog = AppUtils.getPrpgress("Please wait", this);
        progressDialog.show();
        if(bitmap!=null){
            new DetectFaceTask(progressDialog,imageView,txtTakenPictureDesc,faceDetector,editedBitmap,this,currentPhotoPath).execute(bitmap);
        }else {
            progressDialog.dismiss();
            AppUtils.alertDialog(getString(R.string.take_pic),this);
        }

//        imageView.setImageBitmap(bitmap);
//        if (faceDetector.isOperational() && bitmap != null) {
//            editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
//                    .getHeight(), bitmap.getConfig());
//            float scale = getResources().getDisplayMetrics().density;
//            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setColor(Color.RED);
//            paint.setTextSize((int) (16 * scale));
//            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(6f);
//            Canvas canvas = new Canvas(editedBitmap);
//            canvas.drawBitmap(bitmap, 0, 0, paint);
//            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
//            SparseArray<Face> faces = faceDetector.detect(frame);
//            txtTakenPictureDesc.setText(null);
//
//            for (int index = 0; index < faces.size(); ++index) {
//                Face face = faces.valueAt(index);
//                canvas.drawRect(
//                        face.getPosition().x,
//                        face.getPosition().y,
//                        face.getPosition().x + face.getWidth(),
//                        face.getPosition().y + face.getHeight(), paint);
//
//                canvas.drawText("Face " + (index + 1), face.getPosition().x + face.getWidth(), face.getPosition().y + face.getHeight(), paint);
//
////                txtTakenPictureDesc.setText("FACE " + (index + 1) + "\n");
////                txtTakenPictureDesc.setText(txtTakenPictureDesc.getText() + "Smile probability:" + " " + face.getIsSmilingProbability() + "\n");
////                txtTakenPictureDesc.setText(txtTakenPictureDesc.getText() + "Left Eye Is Open Probability: " + " " + face.getIsLeftEyeOpenProbability() + "\n");
////                txtTakenPictureDesc.setText(txtTakenPictureDesc.getText() + "Right Eye Is Open Probability: " + " " + face.getIsRightEyeOpenProbability() + "\n\n");
//
//                for (Landmark landmark : face.getLandmarks()) {
//                    int cx = (int) (landmark.getPosition().x);
//                    int cy = (int) (landmark.getPosition().y);
////
//                }
//            }
//
//            if (faces.size() == 0) {
//                txtTakenPictureDesc.setText("Face not found");
//            } else {
//                imageView.setImageBitmap(editedBitmap);
//                txtTakenPictureDesc.setText(txtTakenPictureDesc.getText() + "No of Faces Detected: " + " " + faces.size());
//            }
//        } else {
//            txtTakenPictureDesc.setText("Could not set up the detector!");
//        }
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnProcess:
                Intent intent = new Intent(this,FaceActivity.class);
                startActivity(intent);
                break;
            case R.id.imgCamera:
                openCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetector.release();
    }

}
