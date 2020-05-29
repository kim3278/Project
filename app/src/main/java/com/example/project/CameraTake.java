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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class CameraTake extends AppCompatActivity implements AutoPermissionsListener {
    public static final int REQUEST_CODE_MENU = 101;
    String server_ip = "";
    CameraSurfaceView cameraView;
    ImageView imageview;
    TextView textView;
    Handler handler = new Handler();
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
        textView = findViewById(R.id.textView);
        imageview = findViewById(R.id.guide_line_view);

        Button button = findViewById(R.id.pill_detect_submit);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                takePicture();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();

                        }
                        send(image_byte);
                    }
                }).start();

//                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
//                intent.putExtra("image_byte", image_byte);
//                startActivityForResult(intent, REQUEST_CODE_MENU);
            }
        });
        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    public void printClientLog(final String data){
        Log.d("ResultActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.append(data + "\n");
            }
        });
    }

    public void send(byte[] data){
        try {
            int portNumber = 5003;
            Socket sock = new Socket(server_ip, portNumber);
            printClientLog("소켓 연결함.");

            DataOutputStream outstream = new DataOutputStream(sock.getOutputStream());
            outstream.write(data);
            outstream.flush();
            printClientLog("데이터 전송함.");

            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String rev = reader.readLine();
            printClientLog("서버로부터 받음: " + rev);
            sock.close();
        } catch(ConnectException e){
            Looper.prepare();
            Toast.makeText(this, "Please Check sever Ip or State", Toast.LENGTH_LONG).show();
            Looper.loop();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    // onCreate에서 아래 문장 실행시, window가 activity에 붙기전에 계산하므로 0이 된다.
    // onWindowFocusChanged는 onCreate 이후에 실행되어 정상값으로 나옴
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // get variation for crop bitmap
        int[] guide_location = new int[2];
        guide_width = imageview.getWidth();
        guide_height = imageview.getHeight();
        guide_top = imageview.getTop();
        guide_left = imageview.getLeft();
//        imageview.getLocationOnScreen(guide_location);
//        guide_left = guide_location[0];
//        guide_top = guide_location[1];

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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_SHORT).show();
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback(){
            public void onPictureTaken(byte[] data, Camera camera){
                try {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(90);
//                    Bitmap r_bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//
//
//                    // crop the image
//                    Bitmap cropped_bitmap = Bitmap.createBitmap(r_bitmap, guide_left, guide_top, guide_width, guide_height);
//                    // save the cropped image
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
                    // converse bitmap to byte[]
//                    int size = guide_width * guide_height;
//                    ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//                    cropped_bitmap.copyPixelsToBuffer(byteBuffer);
//                    byte[] croped_data = byteBuffer.array();
                    image_byte = Arrays.copyOf(data, data.length);
                    camera.startPreview();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
