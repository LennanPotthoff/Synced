package com.example.synced;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.ekalips.fancybuttonproj.FancyButton;
import com.libizo.CustomEditText;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    String ip;
    int screenHeight;
    int screenWidth;
    int counter = 0;
    int desktopPosition;
    Boolean connected = false;
    CustomEditText editText;
    DatagramSocket ds;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        try{
            File file = new File("/storage/emulated/0/Synced/");
            if(file.exists() == false){
                file.mkdir();
            }
        }catch (Exception e){
            System.out.println(e);
        }

        info.hoang8f.widget.FButton connect_button = findViewById(R.id.connect_button);
        connect_button.setCornerRadius(500);
        connect_button.setButtonColor(Color.parseColor("#4DCCBD"));
        getDimension();
        final FancyButton loadingButton = findViewById(R.id.loading_button);
        loadingButton.collapse();

        editText = findViewById(R.id.customEditText);
        editText.setHintTextColor(Color.parseColor("#ffffff"));

        desktopPosition = findViewById(R.id.desktop).getRight();
        final ImageView crossImage = findViewById(R.id.cancelCross);

        final info.hoang8f.widget.FButton connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingButton.setVisibility(View.VISIBLE);
                crossImage.setVisibility(View.VISIBLE);
                connectButton.setVisibility(View.GONE);
                ImageView phoneImage = findViewById(R.id.phone);
                fadeInView(phoneImage);
                findViewById(R.id.customEditText).setVisibility(View.GONE);
                conncectAnimation(findViewById(R.id.desktop), findViewById(R.id.phone));

            }
        });


    }

    public void fadeInView(final View view) {
        final Handler fadeHandler = new Handler();
        Runnable fadeRunnable = new Runnable() {
            @Override
            public void run() {
                if(view.getAlpha()<1f){
                    findViewById(R.id.loading_bar).setAlpha(findViewById(R.id.loading_bar).getAlpha()+0.05f);
                    view.setAlpha(view.getAlpha()+0.05f);
                    fadeHandler.postDelayed(this, 1);
                }
                if(view.getAlpha()>=1f){
                    fadeHandler.removeCallbacks(this);
                    final FancyButton loadingButton = findViewById(R.id.loading_button);
                    final ImageView crossImage = findViewById(R.id.cancelCross);
                    final info.hoang8f.widget.FButton connectButton = findViewById(R.id.connect_button);
                    try{
                        loadingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadingButton.setVisibility(View.GONE);
                                crossImage.setVisibility(View.GONE);
                                connectButton.setVisibility(View.VISIBLE);
                                findViewById(R.id.phone).setAlpha(0);
                                findViewById(R.id.loading_bar).setAlpha(0);
                                ds.close();
                                counter = 400;
                                loadingButton.setOnClickListener(null);
                            }
                        });
                    }catch(Exception e){

                    }
                }

            }
        };
        fadeHandler.postDelayed(fadeRunnable, 1);
    }

    public void getDimension(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    public void startExploring(View view){
        Intent explore = new Intent(this, explore.class);
        explore.putExtra("ip", ip);
        startActivity(explore);
        ds.close();
        finish();
    }

    public void startQuickSync(View view){
        Intent quickSync = new Intent(this, quickSync.class);
        quickSync.putExtra("ip", ip);
        startActivity(quickSync);
        ds.close();
        finish();
    }

    public void connect(){
        Runnable runnableThread = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                ip = editText.getText().toString();
                try {
                    ds = new DatagramSocket(9999);
                    com.libizo.CustomEditText ipEditText = findViewById(R.id.customEditText);
                    DatagramPacket dp = new DatagramPacket("hello?".getBytes(), "hello?".getBytes().length, InetAddress.getByName(ipEditText.getText().toString()), 8888);
                    ds.send(dp);
                    byte[] bytes = new byte[6];
                    DatagramPacket dp2 = new DatagramPacket(bytes, bytes.length);
                    ds.setSoTimeout(8000);
                    try{
                        ds.receive(dp2);
                        String wakeMessage = new String(dp2.getData());
                        if (wakeMessage.contains("hello!")){
                            System.out.println(wakeMessage);
                            connected = true;
                        }
                    }catch (Exception SocketTimeoutException){
                        System.out.println(SocketTimeoutException);
                    }
                } catch (Exception e){
                    System.out.println(e);

                }
            }
        };
        Thread wakeThread = new Thread(runnableThread);
        wakeThread.start();
        final Handler fadeHandler = new Handler();
        Runnable fadeRunnable = new Runnable() {
            @Override
            public void run() {

                if (counter <400){
                    counter++;
                    System.out.println(counter);
                    fadeHandler.postDelayed(this, 1);
                }

                if (counter >= 400 && connected){
                    Intent intent = new Intent(getApplicationContext(), explore.class);
                    intent.putExtra("ip", ip);
                    startActivity(intent);
                    //findViewById(R.id.explore_button).setVisibility(View.VISIBLE);
                    //findViewById(R.id.quickSync_button).setVisibility(View.VISIBLE);
                    //findViewById(R.id.binoculars).setVisibility(View.VISIBLE);
                    //findViewById(R.id.sync).setVisibility(View.VISIBLE);
                    //findViewById(R.id.explore_button).setAlpha(findViewById(R.id.explore_button).getAlpha()+0.1f);
                    //findViewById(R.id.quickSync_button).setAlpha(findViewById(R.id.quickSync_button).getAlpha()+0.1f);
                    //findViewById(R.id.binoculars).setAlpha(findViewById(R.id.binoculars).getAlpha()+0.1f);
                    //findViewById(R.id.sync).setAlpha(findViewById(R.id.sync).getAlpha()+0.1f);
                    //findViewById(R.id.quickSync_button).setY(findViewById(R.id.quickSync_button).getY()-screenHeight/100);
                    //findViewById(R.id.explore_button).setY(findViewById(R.id.explore_button).getY()-screenHeight/100);
                    //findViewById(R.id.binoculars).setY(findViewById(R.id.binoculars).getY()-screenHeight/100);
                    //findViewById(R.id.sync).setY(findViewById(R.id.sync).getY()-screenHeight/100);
                    //findViewById(R.id.desktop).setVisibility(View.GONE);
                    //findViewById(R.id.phone).setVisibility(View.GONE);
                    //findViewById(R.id.cancelCross).setVisibility(View.GONE);
                    //findViewById(R.id.customEditText).setVisibility(View.GONE);
                    //findViewById(R.id.loading_bar).setVisibility(View.GONE);
                    //fadeHandler.postDelayed(this, 1);
                }

                if (counter >=400 && !connected){
                    findViewById(R.id.loading_button).setVisibility(View.GONE);
                    findViewById(R.id.cancelCross).setVisibility(View.GONE);
                    findViewById(R.id.connect_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.customEditText).setVisibility(View.VISIBLE);
                    findViewById(R.id.phone).setAlpha(0);
                    findViewById(R.id.loading_bar).setAlpha(0);
                    findViewById(R.id.loading_button).setOnClickListener(null);
                    System.out.println("testing");
                    counter = 0;
                    ds.close();
                    fadeHandler.removeCallbacks(this);
                }
                if(findViewById(R.id.quickSync_button).getAlpha() >= 1){
                    findViewById(R.id.loading_button).setVisibility(View.GONE);
                    counter = 0;
                    ds.close();
                    fadeHandler.removeCallbacks(this);
                }


            }
        };
        fadeHandler.postDelayed(fadeRunnable, 1);
    }

    public void conncectAnimation(final View desktop, final View phone){
        final Handler animationHandler = new Handler();
        Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                if (desktop.getLeft()>=screenWidth/10){
                    desktop.setLeft(desktop.getLeft()-screenHeight/100);
                }
                if (phone.getTop()>(screenHeight/100)*25){
                    phone.setTop(phone.getTop()-screenHeight/100);
                }
                if (phone.getTop()<=(screenHeight/100)*25 && desktop.getLeft()<=screenWidth/10){
                    connect();
                    animationHandler.removeCallbacks(this);
                }
                else{
                    animationHandler.postDelayed(this, 1);
                }
            }
        };
        animationHandler.postDelayed(animationRunnable, 1);
    }
}
