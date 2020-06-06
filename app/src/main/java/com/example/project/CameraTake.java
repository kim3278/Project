package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class CameraTake extends AppCompatActivity {
    public static final int REQUEST_CODE_MENU = 101;
    public static final int GET_GALLERY_IMAGE = 201;
    CameraSurfaceView cameraView;
    ImageView imageview;
    byte[] image_byte = null;
    int guide_width, guide_height, guide_left, guide_top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_camera_take);
        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        cameraView = new CameraSurfaceView(this);
        previewFrame.addView(cameraView);
        imageview = findViewById(R.id.guide_line_view);

        Button button = findViewById(R.id.pill_detect_submit);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                takePicture();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (image_byte == null);
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("image_byte", image_byte);
                        startActivityForResult(intent, REQUEST_CODE_MENU);
                    }
                }).start();
            }
        });

        Button button_gallery = findViewById(R.id.access_to_gallery);
        button_gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });
    }

    // onCreate에서 아래 문장 실행시, window가 activity에 붙기전에 계산하므로 0이 된다.
    // onWindowFocusChanged는 onCreate 이후에 실행되어 정상값으로 나옴
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // get variation for crop bitmap
        guide_width = imageview.getWidth();
        guide_height = imageview.getHeight();
        guide_top = imageview.getTop();
        guide_left = imageview.getLeft();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_MENU){
            if(resultCode == 100){ // 다시찍기
                Toast.makeText(getApplicationContext(), "다시 찍어주세요.", Toast.LENGTH_LONG).show();
            } else if(resultCode == 200){ // 닫기
                finish();
            }
        } else if(requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri selectedImageUri = data.getData();
            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            intent.putExtra("selectedImageUri", selectedImageUri);
            startActivityForResult(intent, REQUEST_CODE_MENU);
        }
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback(){
            public void onPictureTaken(byte[] data, Camera camera){
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap r_bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    Bitmap rs_bitmap = resizeBitmap(r_bitmap, cameraView.getWidth(), cameraView.getHeight());

                    // crop the image
                    Bitmap cropped_bitmap = Bitmap.createBitmap(rs_bitmap, guide_left, guide_top, guide_width, guide_height);
                    // save the cropped image
//                    String outUriStr = MediaStore.Images.Media.insertImage(
//                            getContentResolver(),
//                            cropped_bitmap,
//                            "Captured Image",
//                            "Captured Image Using Camera.");
//
//                    if (outUriStr == null){
//                        Log.d("Capture", "Image insert failed.");
//                        return;
//                    } else {
//                        Uri outUri = Uri.parse(outUriStr);
//                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
//                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
                    cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) ;
                    image_byte = stream.toByteArray() ;
//                    image_byte = Arrays.copyOf(data, data.length);
                    camera.startPreview();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap.getWidth() != width || bitmap.getHeight() != height){
            float w_ratio = 1.0f * (float)width / (float)bitmap.getWidth();
            float h_ratio = 1.0f * (float)height / (float)bitmap.getHeight();

            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int)(((float)bitmap.getWidth()) * w_ratio), // Width
                    (int)(((float)bitmap.getHeight()) * h_ratio), // Height
                    false);
        }

        return bitmap;
    }

    class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;

        public CameraSurfaceView(Context context) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();
            setCameraOrientation();
            // 카메라 auto focus 설정, 지원하는지 확인 후 적용
            Camera.Parameters params = camera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);
            }



            try {
                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setCameraOrientation(){
            if(camera == null){
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);

            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int rotation = manager.getDefaultDisplay().getRotation();

            int degrees = 0;
            switch (rotation){
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }

            camera.setDisplayOrientation(result);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder){
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        public boolean capture(Camera.PictureCallback handler){
            if(camera != null){
                camera.takePicture(null, null, handler);
                return true;
            } else{
                return false;
            }
        }
    }
}
