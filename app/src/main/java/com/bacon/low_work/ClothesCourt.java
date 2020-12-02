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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ClothesCourt extends AppCompatActivity {

    Context context = ClothesCourt.this;

    ArrayList<String> map_key = new ArrayList<>();

    HashMap<String ,Integer> res_map = new HashMap<String ,Integer>();
    HashMap<String ,Integer> filled_answer = new HashMap<String ,Integer>();

    TextView temp_text;

    int last_touch = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.court_clothes);

        setToolBar();

        loadData();


        setGrid();

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar2);

        toolbar.setTitle("學號 : " + getSharedPreferences("data",0).getString("ID","???"));
    }


    private void loadData() {


        String name = "法官";

        res_map.put(name,R.drawable.first);
        map_key.add(name);
        ImageView imageView = findViewById(R.id.sit1_image);
        imageView.setOnClickListener(listener);
        imageView.setTag(name);


        name = "書記官";

        res_map.put(name,R.drawable.center);
        map_key.add(name);
        imageView = findViewById(R.id.sit2_image);
        imageView.setOnClickListener(listener);
        imageView.setTag(name);



        name = "辯護人";

        res_map.put(name,R.drawable.left);
        map_key.add(name);
        imageView = findViewById(R.id.sit_left1_image);
        imageView.setOnClickListener(listener);
        imageView.setTag(name);


        name = "檢察官";

        res_map.put(name,R.drawable.right);
        map_key.add(name);
        imageView = findViewById(R.id.sit_right1_image);
        imageView.setOnClickListener(listener);
        imageView.setTag(name);

    }


    private void setGrid() {

        GridView gridView = findViewById(R.id.gridview);

        BaseAdapter adapter = new ClothesGridAdapter(context,map_key,res_map);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textview = view.findViewById(R.id.back);

                if (temp_text != null){
                    temp_text.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                }

                last_touch = position;
                temp_text = textview;
                textview.setBackgroundColor(context.getResources().getColor(R.color.mark));

            }
        });

    }


    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (last_touch != -1) {

                ImageView imageView = (ImageView) v;

                int temp = res_map.get(map_key.get(last_touch));

                imageView.setImageResource(temp);
                filled_answer.put(imageView.getTag().toString(),temp);

            }

        }
    };


    public void done(View view) {

        int count = 2;
        final Class<Select> nextClass = Select.class;

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
                                Intent intent = new Intent(context, nextClass);
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
                                startActivity(new Intent(context, nextClass));

                            }
                        }).setNegativeButton("先等等", null).show();

            }


        } else {

            new AlertDialog.Builder(context).setTitle("闖關失敗...").setMessage("好像哪邊搞錯了...\n快把他找出來!!")
                    .setNegativeButton("返回", null).show();

        }

    }

    private boolean checkAnswer(){

        return res_map.equals(filled_answer);
//        return  true;
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
