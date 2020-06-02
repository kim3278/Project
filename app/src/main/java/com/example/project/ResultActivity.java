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
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;

public class ResultActivity extends AppCompatActivity {
    String server_ip = "";
    String token = "3932f3b0-cfab-11dc-95ff-0800200c9a663932f3b0-cfab-11dc-95ff-0800200c9a66"; // api token for hipaaspace.com
    String rev = "69945-068-20"; // 소켓통신을 통해 서버로부터 받은 약품코드이름
    StringBuilder result_json = null; // API를 통해 얻은 JSON 파일
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

        Button get_json_button = findViewById(R.id.get_json);
        get_json_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rev != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String req = "https://www.hipaaspace.com/api/ndc/getcode?q="+ rev + "&rt=json&token="+ token;
                            request(req);
                        }
                    }).start();
                } else {
                    Log.d("api", "No rev");
                    Toast.makeText(getApplicationContext(), "서버로부터 받은 약품코드가 없습니다.", Toast.LENGTH_LONG).show();
                }
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
            rev = reader.readLine();
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

    public void request(String urlStr){
        StringBuilder output = new StringBuilder();
        try{
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                int resCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while (true){
                    line = reader.readLine();
                    if(line == null){
                        break;
                    }
                    output.append(line + "\n");
                }
                reader.close();
                conn.disconnect();
            }
        } catch(Exception ex){
            System.out.println("Err at request Method in resultActivity.java: " + ex.toString());
        }
        printClientLog(output.toString());
        result_json = output;
    }
}
