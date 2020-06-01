package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class ResultActivity extends AppCompatActivity {
    String server_ip = "";
    TextView textView;
    ImageView imageView;
    byte[] image_byte = null;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        if(intent.hasExtra("image_byte")){
            image_byte = intent.getByteArrayExtra("image_byte");

            Bitmap bmp = BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length);
            imageView = findViewById(R.id.cropImageView);
            imageView.setImageBitmap(bmp);
        }

        textView = findViewById(R.id.textView);
        Button send_button = findViewById(R.id.send);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(image_byte);
                    }
                }).start();
            }
        });

        Button retake_button = findViewById(R.id.retake);
        retake_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(100, intent);
                finish();
            }
        });

        Button close_button = findViewById(R.id.close);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(200, intent);
                finish();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

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
}
