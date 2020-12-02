package com.bacon.low_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Context context = MainActivity.this;

    EditText studentID_editText,name_editText;

    Runnable UIrunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.activity_main);

        checkInternet();

        //執行在確認版本後的動作都放在這裡
        UIrunnable = new Runnable() {
            @Override
            public void run() {
                //執行在確認版本後的動作都放在這裡

                setFirst();

                studentID_editText = findViewById(R.id.student_id);
                studentID_editText.setText(getSharedPreferences("data",0).getString("ID",""));
                name_editText = findViewById(R.id.name);
                name_editText.setText(getSharedPreferences("data",0).getString("name",""));


                Button enter = findViewById(R.id.enter);
                enter.setOnClickListener(v -> {

                    final String name = name_editText.getEditableText().toString();
                    final String ID = studentID_editText.getEditableText().toString();

                    if (check_formate(name,ID)){

                        SharedPreferences preferences = getSharedPreferences("data",0);


                        if (name.equals(ID) && name.equals("teacher")){
                            preferences.edit().putString("name",name).apply();
                            preferences.edit().putString("ID",ID).apply();
                            Intent intent = new Intent(context, TeacherUse.class);
                            startActivity(intent);

                        }else{

                            preferences.edit().putString("name",name).apply();
                            preferences.edit().putString("ID",ID.toUpperCase()).apply();

                            new AlertDialog.Builder(context).setTitle("確認你的資料 :")
                                    .setMessage("name : " + name +
                                            "\n學號 : " + ID.toUpperCase())
                                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            firebase_add(ID.toUpperCase(),name);



                                            Intent intent = new Intent(context,PositionCourt.class);

//                                        Intent intent = new Intent(context,Select.class);
                                            finish();
                                            startActivity(intent);

                                        }
                                    }).setNegativeButton("打錯了!!", null).show();

                        }

                    }else{
                        Toast.makeText(context,"格式錯誤",Toast.LENGTH_SHORT).show();
                    }

                });
            }
        };

        checkVersion();



    }

    private void checkVersion(){

        int this_version = getVersionCode();

        FireStore_getVersion fireStore_getVersion = new FireStore_getVersion();

        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);

        small_checkVersion(fireStore_getVersion,dialog,0,this_version);

    }

    private void small_checkVersion(FireStore_getVersion fireStore_getVersion,AlertDialog dialog,int count,int this_version){
        StringBuilder dot = new StringBuilder();
        for(int i=0;i<count/2;i++){
            dot.append(".");
        }

        dialog.setMessage("檢查版本中..."+dot.toString());
        dialog.show();

        int flag = fireStore_getVersion.check_flag();
        if (count/4 > 5)
            flag = -1;


        AlertDialog.Builder tempDialog;

        switch (flag){

            case -1 :
                //失敗
                dialog.cancel();

                tempDialog = new AlertDialog.Builder(context);
                tempDialog.setCancelable(false);
                tempDialog.setMessage("檢查版本失敗，請保持網路連線正常!!");

                tempDialog.setPositiveButton("重新確認版本",(dia, which) -> {
                    fireStore_getVersion.retry();
                    small_checkVersion(fireStore_getVersion, dialog,0,this_version);
                });

                tempDialog.show();

                break;

            case 0 :
            case 1 :
                //下載中...



                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        small_checkVersion(fireStore_getVersion, dialog,count+1,this_version);

                    }
                },250);
                break;

            case 2 :
                //成功
                String internet_version = fireStore_getVersion.getInternet_Version();
                boolean important = fireStore_getVersion.getImportant();

                if (!(this_version+"").equals(internet_version)){
                    dialog.cancel();
                    tempDialog = new AlertDialog.Builder(context);
                    tempDialog.setCancelable(false);

                    if (important) {

                        tempDialog.setMessage("當前版本 : " + this_version + "\n最新版本 : " + internet_version
                                +"\n\n最新版本描述 : \n"+fireStore_getVersion.getDescribe()
                                +"\n\n此版本為重大更新，請立即下載最新版本");

                        tempDialog.setPositiveButton( "前往下載"
                                , (dialog13, which) -> {
                                    Uri uri = Uri.parse("https://drive.google.com/drive/folders/1aYiMJOOHr8iXdXa3fKdojmPlvXe0a_fP");
                                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                    startActivity(intent);
                                    fireStore_getVersion.retry();
                                    small_checkVersion(fireStore_getVersion, dialog,0,this_version);
                                });


                    }else{
                        tempDialog.setMessage("當前版本 : " + this_version + "\n最新版本 : " + internet_version
                                +"\n\n最新版本描述 : \n"+fireStore_getVersion.getDescribe()
                                +"\n\n還不夠新!!請上網下載最新版本!!");

                        tempDialog.setPositiveButton("前往下載"
                                , (dialog12, which) -> {
                                    Uri uri = Uri.parse("https://drive.google.com/drive/folders/1aYiMJOOHr8iXdXa3fKdojmPlvXe0a_fP");
                                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                    startActivity(intent);
                                    small_checkVersion(fireStore_getVersion, dialog,0,this_version);
                                });


                        tempDialog.setNegativeButton("取消", (dialog1, which) -> {

                            AlertDialog dd = tempDialog.create();
                            dd.cancel();
                            runOnUiThread(UIrunnable);

                        });

                    }

                    tempDialog.show();

                }else{
                    dialog.cancel();
                    runOnUiThread(UIrunnable);
                }

                break;

        }

    }


    private int getVersionCode() {
        // 包管理器 可以获取清单文件信息
        PackageManager packageManager = getPackageManager();
        try {
            // 获取包信息
            // 参1 包名 参2 获取额外信息的flag 不需要的话 写0
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setFirst() {

        new AlertDialog.Builder(context)
                .setMessage("1. 請在網路良好的地方作答，若是沒有網路，可能會造成資料無法上傳\n\n" +
                        "2. 請確認你的學號沒有錯誤\n\n" +
                        "3. 總共有三關，會記錄過關數，但不會記錄作答狀況，因此若是半途而廢必須要重新作答")
                .setPositiveButton("OK",null).setCancelable(false).show();

    }

    private void checkInternet() {

        while(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){

            Log.d("logg","request internet");

            new AlertDialog.Builder(this).setTitle("取得網路權限!!!一定要同意!!!")
                    .setPositiveButton("前往同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.INTERNET},1);

                        }
                    }).setCancelable(false).show();

        }

        Log.d("logg","checked internet");

    }

    private boolean check_formate(String name,String ID){
        return  !ID.equals("");
    }

    private void firebase_add(String studentID,String name){

        Log.d("logg","fire now");

        Firestore_sutdent firestore = new Firestore_sutdent(context,studentID,name,getSharedPreferences("data",0));
        firestore.upload_data(false);
        firestore.download_student_Data();


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
