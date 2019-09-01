package com.example.synced;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class explore extends AppCompatActivity {;

    static String moreComing = "no";
    ArrayList<String> dirList;
    ArrayList<Integer> directoryImagesArrayList;
    Boolean folderClicked = false;
    myAdapter myAdapter;
    Socket socketPublic;
    Boolean requestSent;
    Boolean firstRequestSent = false;
    Boolean backed = false;
    Boolean inRoot = true;
    ListView mListView;
    TextView selectedTextview;
    static Boolean transferActive = false;
    static String selectedFileName;
    info.hoang8f.widget.FButton noButton;
    info.hoang8f.widget.FButton yesButton;
    info.hoang8f.widget.FButton noCancelButton;
    info.hoang8f.widget.FButton yesCancelButton;
    Socket socket;
    String ip;
    Dialog doneDialog;


    public void setSocket(Socket socket){
        socketPublic = socket;
    }

    public void fadeInListView(final ListView mListView){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mListView.getAlpha() < 1f){
                    mListView.setAlpha(mListView.getAlpha()+0.05f);
                    handler.postDelayed(this, 1);
                }
                else if(mListView.getAlpha() >= 1f){
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 1);
    }

    public void moveUpDirectory(){

        final Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try{
                    if(backed){
                        PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(socketPublic.getOutputStream()));
                        printWriter.println("back");
                        printWriter.flush();
                        backed = false;
                        Thread.currentThread().interrupt();
                    }
                    else if(!backed){
                            Thread.currentThread().interrupt();
                        }

                }catch (Exception e){
                    System.out.println(e);
                }
            }
        };
        Thread thread = new Thread(runnable1);
        thread.start();
    }

    public void downloadFile(){
        if(Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);
                }
            } else {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        try{
                            byte[] bytes = new byte[4194];
                            socket = new Socket(InetAddress.getByName(ip), 8889);
                            InputStream inputStream = socket.getInputStream();
                            File file = new File("/storage/emulated/0/Synced/" + selectedFileName);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                            int bytesRead;
                            while((bytesRead = inputStream.read(bytes, 0, bytes.length)) > 0){
                                bufferedOutputStream.write(bytes, 0, bytesRead);
                            }
                            bufferedOutputStream.close();
                            socket.close();
                            transferActive = false;

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    doneDialog.show();                                }
                            });

                            Thread.currentThread().interrupt();
                        } catch (Exception e){
                            System.out.println(e);
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            try{
                                byte[] bytes = new byte[1000000000];
                                socket = new Socket(InetAddress.getByName(ip), 8889);
                                InputStream inputStream = socket.getInputStream();
                                File file = new File("/storage/emulated/0/Synced/" + selectedFileName);
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                                int bytesRead;
                                while((bytesRead = inputStream.read(bytes, 0, bytes.length)) > 0){
                                    bufferedOutputStream.write(bytes, 0, bytesRead);
                                }

                                bufferedOutputStream.close();
                                socket.close();
                                transferActive = false;

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        doneDialog.show();
                                    }
                                });

                                Thread.currentThread().interrupt();
                            } catch (Exception e){
                                System.out.println(e);
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                } else {
                    //All permissions requested were denied...
                }
                return;
            }
        }
    }

    public static void setMoreComing(String flag){
        moreComing = flag;
    }

    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

    @Override
    public void onBackPressed(){
        vibrate();
        directoryImagesArrayList.clear();
        dirList.clear();
        mListView.setAlpha(0);
        mListView.setAdapter(null);
        myAdapter.notifyDataSetChanged();
        backed = true;
        requestSent = true;
        moveUpDirectory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explore_ui);
        ip = getIntent().getStringExtra("ip");
        mListView = findViewById(R.id.listView);
        final Dialog transferDialog = new Dialog(this);
        final Dialog cancelDialog = new Dialog(this);
        doneDialog = new Dialog(this);
        doneDialog.setContentView(R.layout.transferdonedialog);
        doneDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        doneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancelDialog.setContentView(R.layout.cancelpopuplayout);
        transferDialog.setContentView(R.layout.popuplayout);
        noCancelButton = cancelDialog.findViewById(R.id.no_button);
        yesCancelButton = cancelDialog.findViewById(R.id.yes_button);
        noButton = transferDialog.findViewById(R.id.no_button);
        yesButton = transferDialog.findViewById(R.id.yes_button);
        selectedTextview = transferDialog.findViewById(R.id.fileName);
        noButton.setButtonColor(Color.parseColor("#4DCCBD"));
        yesButton.setButtonColor(Color.parseColor("#4DCCBD"));
        noCancelButton.setButtonColor(Color.parseColor("#4DCCBD"));
        yesCancelButton.setButtonColor(Color.parseColor("#4DCCBD"));

        yesCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        try {
                            socket.close();
                            transferActive = false;
                            Thread.currentThread().interrupt();
                        }catch (Exception e){
                            System.out.println(e);
                            Thread.currentThread().interrupt();
                        }

                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        noCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferDialog.dismiss();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transferActive){
                    transferDialog.dismiss();
                }
                else if(!transferActive){
                    try{
                        transferActive = true;
                        transferDialog.dismiss();
                        PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(socketPublic.getOutputStream()));
                        printWriter.println(selectedFileName);
                        printWriter.flush();
                        downloadFile();
                    }catch (Exception e){
                        System.out.println(e);
                    }

                }
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try{

                    dirList = new ArrayList<>();
                    directoryImagesArrayList = new ArrayList<>();


                    while(true){

                        if(requestSent == null || (!requestSent && !firstRequestSent)){
                            Socket socket = new Socket(InetAddress.getByName(ip), 8888);
                            PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                            printWriter.println("C:\\Users");
                            printWriter.flush();
                            firstRequestSent = true;
                            requestSent = true;
                            if(socketPublic == null){
                                setSocket(socket);
                            }
                        }

                        if(requestSent = true){
                            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socketPublic.getInputStream()));
                            Object[] objects = (Object[]) objectInputStream.readObject();
                            for (Object o: objects){
                                if(o.toString().contains("Folder") && !dirList.contains(o.toString())){
                                    dirList.add(o.toString().replace("Folder", ""));
                                    directoryImagesArrayList.add(R.drawable.folder);
                                }
                                if(o.toString().contains("File") && !dirList.contains(o.toString())){
                                    dirList.add(o.toString().replace("File", ""));
                                    directoryImagesArrayList.add(R.drawable.file);
                                }
                            }
                        }



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(dirList.size() + " Size!");
                                final ArrayList<String> newDirList = new ArrayList<>();
                                for (String string: dirList){
                                    if(!newDirList.contains(string)){
                                        newDirList.add(string);
                                    }
                                }
                                final String[] dirArray = newDirList.toArray(new String[newDirList.size()]);
                                final Integer[] IntegerArray = directoryImagesArrayList.toArray(new Integer[directoryImagesArrayList.size()]);
                                myAdapter = new myAdapter(getApplicationContext(), dirArray, IntegerArray);
                                mListView.setAdapter(myAdapter);
                                fadeInListView(mListView);
                                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                        vibrate();
                                        if(directoryImagesArrayList.get(position) == R.drawable.folder){
                                            try{
                                                PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(socketPublic.getOutputStream()));
                                                printWriter.println(newDirList.get(position).trim());
                                                printWriter.flush();
                                            }catch (Exception e){
                                                System.out.println(e);
                                            }
                                            folderClicked = true;
                                            inRoot = false;
                                            mListView.setAlpha(0);
                                            mListView.setAdapter(null);
                                            dirList.clear();
                                            newDirList.clear();
                                            directoryImagesArrayList.clear();
                                            myAdapter.notifyDataSetChanged();
                                        }
                                        else if (directoryImagesArrayList.get(position) == R.drawable.file){

                                            if(!transferActive){
                                                try{
                                                    transferDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                                    transferDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                    selectedTextview.setText(newDirList.get(position).trim());
                                                    selectedFileName = newDirList.get(position).trim();
                                                    transferDialog.show();
                                                    folderClicked = false;
                                                }catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            }
                                            else if(transferActive){
                                                cancelDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                                cancelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                cancelDialog.show();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        requestSent = false;
                    }
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }
}
