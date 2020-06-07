package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Iterator;

public class ResultActivity extends AppCompatActivity {
    String server_ip = "";
    String token = "3932f3b0-cfab-11dc-95ff-0800200c9a663932f3b0-cfab-11dc-95ff-0800200c9a66"; // api token for hipaaspace.com
    String rev = null; // 소켓통신을 통해 서버로부터 받은 약품코드이름
    StringBuilder result_json = null; // API를 통해 얻은 JSON 파일
    TextView json_result_view; // JSON에서 필요한 데이터만 보여줄 view
    ImageView imageView;
    byte[] image_byte = null;
    Handler handler = new Handler();
    String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        imageView = findViewById(R.id.cropImageView);
        final Intent intent = getIntent();
        if(intent.hasExtra("image_byte")){
            image_byte = intent.getByteArrayExtra("image_byte");

            Bitmap bmp = BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length);
            imageView.setImageBitmap(bmp);
        } else if(intent.hasExtra("selectedImageUri")){
            Uri img_uri = intent.getParcelableExtra("selectedImageUri");
            imageView.setImageURI(img_uri);
            image_byte = convertImageToByte(img_uri);
        }

        json_result_view = findViewById(R.id.json_result_view);


        Button send_button = findViewById(R.id.send);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(image_byte, view);
                    }
                }).start();
            }
        });

        final Button retake_button = findViewById(R.id.retake);
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
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                if(rev != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String req = "https://www.hipaaspace.com/api/ndc/search?q="+ rev + "&rt=json&token="+ token;
                            request(req);
                        }
                    }).start();
                } else {
                    Log.d("api", "No rev");
                    Snackbar.make(v, "서버로부터 받은 약품코드가 없습니다.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button calender_button = findViewById(R.id.calendar_button);
        calender_button.setOnClickListener(new View.OnClickListener() { // 캘린더


            @Override
            public void onClick(View v) {

                if(code != null){
                    Intent intent2 = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

                    intent2.putExtra(CalendarContract.Events.TITLE, "약품코드 : "+ code);
                    startActivity(intent2);
                }
                else{
                    Snackbar.make(v, "서버로부터 받은 약품코드가 없습니다.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public byte[] convertImageToByte(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    public void printClientLog(final String data){
        Looper.prepare();
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    @SuppressLint("WrongConstant")
    public void send(byte[] data, View view){
        try {
            int portNumber = 5003;
            Socket sock = new Socket(server_ip, portNumber);
            Snackbar.make(view, "소켓 연결함.", Snackbar.LENGTH_LONG).show();

            DataOutputStream outstream = new DataOutputStream(sock.getOutputStream());
            outstream.write(data);
            outstream.flush();
            Snackbar.make(view, "데이터 전송함.", Snackbar.LENGTH_LONG).show();

            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String t_rev = reader.readLine();
            Snackbar.make(view, "서버로부터 받음: " + t_rev, Snackbar.LENGTH_LONG).show();
            rev = t_rev.substring(9);
            sock.close();
        } catch(ConnectException e){
            Looper.prepare();
            Snackbar.make(view, "Please check sever ip or server state", Snackbar.LENGTH_LONG).show();
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
                JSONObject jsonObj = new JSONObject(output.toString());
                if(!jsonObj.isNull("error")){
                    printClientLog("약품 코드가 잘못되었습니다.");
                    output = null;
                } else {
                    JSONArray jsonArray = jsonObj.getJSONArray("NDC");
                    if(jsonArray.getJSONObject(0).isNull("NDCCode")){
                        printClientLog("API에 없는 약품코드입니다.");
                        output = null;
                    }else{
//                        printClientLog(output.toString());
                        JSONObject jsd = jsonArray.getJSONObject(0);
//                        printClientLog(jsd.toString());
                        final String jsb = "약품코드 : " + jsd.getString("NDCCode") + "\n" +
                                "성분 : " + jsd.getString("SubstanceName") + "\n" +
                                "제형 : " + jsd.getString("DosageFormName") + "\n" +
                                "제조 회사 : " + jsd.getString("LabelerName") + "\n";
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                json_result_view.setText(jsb);
                            }
                        });
                        result_json = output;
                        code=jsd.getString("NDCCode");
                    }
                }
                reader.close();
                conn.disconnect();
            }
        } catch(Exception ex){
            System.out.println("Err at request Method in resultActivity.java: " + ex.toString());
        }
    }
}
