package com.bacon.low_work;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.prefs.Preferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PositionCourt extends AppCompatActivity {

    Context context = PositionCourt.this;
    String touch_name;

    HashMap<String, TextView> answer_map = new HashMap();
    HashMap<String, TextView> filled_map = new HashMap();

    TextView temp_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.court_position);

        setFirst();

        setToolBar();

        int a = 0;


        setGridList();

        setSitMap();


    }

    private void setFirst() {

        new AlertDialog.Builder(context)
                .setMessage("1. 請在網路良好的地方作答，若是沒有網路，可能會造成資料無法上傳\n\n" +
                        "2. 總共有三關，會記錄過關數，但不會記錄作答狀況" +
                        "，因此若是半途而廢必須要重新作答\n\n" +
                        "3. 若之前已經有破過關卡，可以直接按 \"完成\"")
                .setPositiveButton("OK",null).setCancelable(false).show();

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("學號 : " + getSharedPreferences("data", 0).getString("ID", "???"));
    }

    private void setSitMap() {

        TextView text = findViewById(R.id.sit1);
        text.setOnClickListener(sit_touch);
        answer_map.put("法官", text);

        text = findViewById(R.id.sit2);
        text.setOnClickListener(sit_touch);
        answer_map.put("書記官", text);

        text = findViewById(R.id.sit3);
        text.setOnClickListener(sit_touch);
        answer_map.put("應訊台", text);

        text = findViewById(R.id.sit_left1);
        text.setOnClickListener(sit_touch);
        answer_map.put("辯護人", text);

        text = findViewById(R.id.sit_left2);
        text.setOnClickListener(sit_touch);
        answer_map.put("被告人", text);

        text = findViewById(R.id.sit_right1);
        text.setOnClickListener(sit_touch);
        answer_map.put("檢察官", text);

        text = findViewById(R.id.sit_right2);
        text.setOnClickListener(sit_touch);
        answer_map.put("自訴人", text);

        text = findViewById(R.id.sit_down_left);
        text.setOnClickListener(sit_touch);
        answer_map.put("證人", text);

        text = findViewById(R.id.sit_down_right);
        text.setOnClickListener(sit_touch);
        answer_map.put("被害人", text);
    }

    private void setGridList() {

        GridView gridView = findViewById(R.id.gridview);

        ArrayList<String> pos = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.court_name)));

        BaseAdapter adapter = new NameGridAdapter(context, pos);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (temp_text != null) {
                    temp_text.setTextColor(context.getResources().getColor(R.color.black));
                }

                temp_text = view.findViewById(R.id.name);
                temp_text.setTextColor(context.getResources().getColor(R.color.mark));
                touch_name = temp_text.getText().toString();


            }
        });

    }

    private final View.OnClickListener sit_touch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (touch_name != null) {

                TextView text = (TextView) v;

                //刪除之前點選的
                if (filled_map.containsKey(touch_name)) {
                    filled_map.get(touch_name).setText("");
                    filled_map.remove(touch_name);
                }

                //刪除被覆蓋格子內的對應map
                filled_map.remove(text.getText().toString());

                text.setText(touch_name);
                filled_map.put(touch_name, text);

            }
        }
    };


    public void done(View view) {

        int count = 1;
        final Class<ClothesCourt> nextClass = ClothesCourt.class;

//        Log.d("logg", "done touch"+getSharedPreferences("data", 0).getInt("count", -1));

        SharedPreferences preferences = getSharedPreferences("data", 0);

        if (checkAnswer() || count <= preferences.getInt("count", -1)) {



            if (count > preferences.getInt("count", -1)) {

//                Log.d("logg",  "hereee  "+preferences.getString("ID", "")+preferences.getString("name", ""));

                Firestore_sutdent db = new Firestore_sutdent(context, preferences.getString("ID", ""),
                        preferences.getString("name", ""), count , preferences);

                db.upload_data(true);

                preferences.edit().putInt("count", count).apply();

                new AlertDialog.Builder(context).setTitle("闖關成功!!!").setMessage("恭喜你闖關成功了!!\n快去下一關吧，GOGO")
                        .setPositiveButton("GOGO~", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context,  nextClass);
                                startActivity(intent);
                            }
                        }).setNegativeButton("先等等...", null).show();


            } else {

//                Log.d("logg", "false");

                new AlertDialog.Builder(context).setTitle("已經破過了!!").setMessage("到下一關去")
                        .setPositiveButton("下一關", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                startActivity(new Intent(context,  nextClass));
                            }
                        }).setNegativeButton("先等等", null).show();

            }


        } else {

            new AlertDialog.Builder(context).setTitle("闖關失敗...").setMessage("好像哪邊搞錯了...\n快把他找出來!!")
                    .setNegativeButton("返回", null).show();

        }

    }

    private boolean checkAnswer() {

        return filled_map.equals(answer_map);
//        return true;

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
