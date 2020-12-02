package com.bacon.low_work;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Explode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;
import ir.androidexception.filepicker.dialog.SingleFilePickerDialog;
import ir.androidexception.filepicker.interfaces.OnCancelPickerDialogListener;
import ir.androidexception.filepicker.interfaces.OnConfirmDialogListener;

public class TeacherUse extends AppCompatActivity {


    ArrayList<HashMap<String, String>> data = new ArrayList<>();
    ArrayList<HashMap<String, String>> search = new ArrayList<>();
    Firestore_sutdent db;

    Runnable UI_runnable;

    String folder;

    Context context = this;

    Recycler_TeacherUse adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.teacheruse);


        UI_runnable = new Runnable() {
            @Override
            public void run() {

                setRecycler();

            }
        };

        setView();

        setData();

        //setData 之後的動作都放在 runnable 裡面


    }

    private void setView() {

        Toolbar toolbar = findViewById(R.id.toolbar3);
        toolbar.inflateMenu(R.menu.techer_menu);


    }

    public void output_CSV(MenuItem item) {

        if (permissionGranted()) {

            DirectoryPickerDialog pickerDialog = new DirectoryPickerDialog(context
                    , () -> Toast.makeText(context,"取消",Toast.LENGTH_SHORT).show()
                    , files -> csv_output(files[0].getPath()));
            pickerDialog.show();

        } else {
            requestPermission();
            output_CSV(item);
        }

    }

    public void screen(MenuItem item) {

        View v = LayoutInflater.from(context).inflate(R.layout.number_picker, null, false);

        Log.d("logg","coming");

        new AlertDialog.Builder(context).setView(v).setPositiveButton("擷取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText from_V = v.findViewById(R.id.from);
                EditText to_V = v.findViewById(R.id.to);

                final String from_out = from_V.getEditableText().toString();
                final String to_out = to_V.getEditableText().toString();

                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                new Thread(() -> {

                    String from = from_out;
                    String to = to_out;

                    char[] from_char = from.toCharArray();
                    char[] to_char = to.toCharArray();

                    search.clear();

                    if (from.length() == to.length() && !from.equals(to)) {

                        Log.d("logg","from.len = "+from.length()+"  to.len = "+to.length());

                        // from 一定要比 to 小、否則交換
                        for (int i = 0; i < from_char.length; i++) {

                            if (from_char[i] > to_char[i]) {
                                Log.d("logg","進入交換");
                                String temp = from;
                                from = to;
                                to = temp;
                                from_char = from.toCharArray();
                                to_char = to.toCharArray();
                                break;
                            }else if (from_char[i] < to_char[i])
                                break;


                        }


                        char[] temp_char;
                        boolean flag = true;

                        outer:for (HashMap<String, String> temp : data) {

                            temp_char = temp.get("ID").toCharArray();

                            if (temp_char.length != from_char.length){
                                continue;
                            }



                            if (flag) {

                                if (Arrays.equals(from_char,temp_char)){
                                    search.add(temp);
                                    flag = false;
                                    continue ;
                                }
                                for (int i = 0; i < temp_char.length; i++) {

                                    Log.d("logg","from = "+from_char[i]
                                            +" temp = "+temp_char[i]);

                                    if (from_char[i] < temp_char[i]){
                                        search.add(temp);
                                        flag = false;
                                        break;
                                    }else if (from_char[i] > temp_char[i])
                                        break;

                                }

                            }else{


                                if (Arrays.equals(to_char, temp_char)){
                                    search.add(temp);
                                    break;
                                }
                                for (int i = 0; i < temp_char.length; i++) {

                                    Log.d("logg"," temp = "+temp_char[i]
                                            +" to = "+to_char[i]);

                                    if (to_char[i] > temp_char[i]){
                                        search.add(temp);
                                        break;
                                    }else if (to_char[i] < temp_char[i])
                                        break outer;

                                }

                            }

                        }

                        Log.d("logg","len = "+search.size());

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged(search);
                            Toast.makeText(context, "篩選完成", Toast.LENGTH_LONG).show();
                        });



                    } else {
                        runOnUiThread(() ->{
                            Toast.makeText(context, "格式錯誤", Toast.LENGTH_LONG).show();
                        });

                    }

                }).start();





            }
        }).show();


    }


    private void csv_output(String folder) {

        Log.d("logg",folder+"=");

        if (!folder.isEmpty()) {

            StringBuilder builder = new StringBuilder();

            builder.append("ID,").append("Name,").append("Count,").append("Date");

            for (HashMap<String, String> temp_map : data) {

                builder.append("\n");
                builder.append(temp_map.get("ID")).append(",").append(temp_map.get("name"))
                        .append(",").append(temp_map.get("count")).append(",")
                        .append(temp_map.get("date")).append(",");


            }

            try {
                Thread csv_output = new CSV_output_Thread(context, builder, folder);
                csv_output.start();
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "wrong", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


        } else {
            Toast.makeText(context, "請重新選擇資料夾", Toast.LENGTH_LONG).show();
        }
    }

    private boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }


    private void setData() {

        db = new Firestore_sutdent(this, getSharedPreferences("data", 0));
        db.download_ALL_Data();


        //  用handleu延遲1秒，3秒，5秒 db.check_doneOrNot_flag(); 為true沒，若超過則顯示下載失敗

        AlertDialog dialog = new AlertDialog.Builder(this).create();


        setData_count = 0;
        setData_small_method(dialog);

//        data.add(makeMap("3A812074", "namee0", "11/16-17:31", "3"));
//
//        data.add(makeMap("3A810000", "nccee1", "11/18-17:31", "5"));


    }

    int setData_count = 0;

    private void setData_small_method(final AlertDialog dialog) {

        dialog.setTitle("下載資料中...");
        dialog.setMessage("過了 " + setData_count + " 秒...");
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (setData_count < 8) {
                    if (db.check_doneOrNot_flag()) {
                        dialog.cancel();
                        data = db.getData();
                        runOnUiThread(UI_runnable);

                    } else {
                        setData_count++;

                        setData_small_method(dialog);
                    }
                } else {

                    dialog.cancel();
                    dialog.setMessage("下載時間過久，請確保網路正常後重新登入");
                    dialog.setCancelable(true);
                    dialog.setTitle("下載失敗...");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "返回登入頁面", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(TeacherUse.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                    dialog.show();


                }

            }
        }, 1000);


    }


    private HashMap<String, String> makeMap(String ID, String name, String date, String count) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ID", ID);
        map.put("name", name);
        map.put("date", date);
        map.put("count", count);

        return map;
    }

    private void setRecycler() {


        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        adapter = new Recycler_TeacherUse(data, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);


    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程式", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}

class CSV_output_Thread extends Thread {

    StringBuilder builder;
    String folder;
    String filename = "output.csv";
    Context context;

    FileOutputStream stream;


    CSV_output_Thread(Context context, StringBuilder data, String folder) throws FileNotFoundException {

        this.folder = folder;
        this.builder = data;
        this.context = context;
        stream = new FileOutputStream(new File(folder + File.separator + filename), false);

    }

    @Override
    public void run() {
        super.run();

        try {
            stream.write(builder.toString().getBytes());

            stream.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}