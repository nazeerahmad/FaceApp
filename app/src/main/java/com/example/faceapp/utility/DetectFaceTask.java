package com.example.faceapp.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.IOException;

public class DetectFaceTask extends AsyncTask<Bitmap, Void, Bitmap> {


    private ProgressDialog progressDialog;
    private ImageView imageView;
    private TextView txtTakenPictureDesc;
    private FaceDetector faceDetector;
    private Bitmap editedBitmap;
    private Context context;
    private SparseArray<Face> faces;
    private boolean isFaceDetected= false;
    private String path;

    public DetectFaceTask(ProgressDialog progressDialog, ImageView imageView, TextView textView, FaceDetector faceDetector, Bitmap editedBitmap, Context context,String path) {
        this.progressDialog = progressDialog;
        this.imageView = imageView;
        this.txtTakenPictureDesc = textView;
        this.faceDetector = faceDetector;
        this.editedBitmap = editedBitmap;
        this.context = context;
        this.path = path;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        Bitmap bitmap = bitmaps[0];

        if (faceDetector.isOperational() && bitmap != null) {
            isFaceDetected = true;
            editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), bitmap.getConfig());
            float scale = context.getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setTextSize((int) (16 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            Canvas canvas = new Canvas(editedBitmap);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            faces = faceDetector.detect(frame);
//            txtTakenPictureDesc.setText(null);

            for (int index = 0; index < faces.size(); ++index) {
                Face face = faces.valueAt(index);
                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);

                canvas.drawText("Face " + (index + 1), face.getPosition().x + face.getWidth(), face.getPosition().y + face.getHeight(), paint);

//                for (Landmark landmark : face.getLandmarks()) {
//                    int cx = (int) (landmark.getPosition().x);
//                    int cy = (int) (landmark.getPosition().y);
//                }
            }

        } else {
           isFaceDetected = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        progressDialog.dismiss();
        imageView.setImageBitmap(editedBitmap);
        if(isFaceDetected){
            if (faces.size() == 0) {
                txtTakenPictureDesc.setText("Face not found");
            } else {
                imageView.setImageBitmap(editedBitmap);
                txtTakenPictureDesc.setText(txtTakenPictureDesc.getText() + "No of Faces Detected: " + " " + faces.size());
            }
        }else {
            txtTakenPictureDesc.setText("Could not set up the detector!");
        }
    }
    public Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }
}
