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
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Select extends AppCompatActivity {

    Context context = this;

    Question_Data question_data;

    HashMap<String, Boolean> answer = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.select);


        setData();

        setView();


    }

    private void read_csv() throws IOException {

        CSVReader reader = new CSVReader(new InputStreamReader(context.getAssets().open("test.csv")));


        String[] nextLine;
        String number;

        ArrayList<String> question = new ArrayList<>();
        ArrayList<String> small_item;
        ArrayList<ArrayList<String>> all_item = new ArrayList<>();

        boolean flag = true;


        try {

            while ((nextLine = reader.readNext()) != null) {

                //執行掃描第一行，第一行固定是題號，charAt => 把數字轉成ASCII，-48 => ASCII轉成一般數字 ， -1 => 符合index要-1
                number = nextLine[0];
                if (flag) {
                    number = "1";
                    flag = false;
                }


                //執行掃描第2行，第2行固定是題目
                nextLine = reader.readNext();

                for (String temp : nextLine) {
                    if (!temp.equals("")) {
                        question.add(temp);
                    } else {
                        break;
                    }
                }


                //第三行開始是item
                nextLine = reader.readNext();
                small_item = new ArrayList<>();

                for (String temp : nextLine) {
                    if (!temp.equals("")) {
                        small_item.add(temp);
                    } else
                        break;
                }
                all_item.add(small_item);


                //第四行是解答

                nextLine = reader.readNext();


                for (int i = 0; i < nextLine.length; i++) {

                    if (nextLine[i].equals("y")) {
                        String mapID = "Select" + number + "-" + (i + 1);
                        answer.put(mapID, true);

                    } else if (nextLine[i].equals(""))
                        break;

                }

            }

            reader.close();


        } catch (IOException e) {

            e.printStackTrace();

        }

        question_data = new Question_Data(question, all_item);



    }

    private void setData() {

        clear_shareprefer();

        try {
            read_csv();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "file no found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void setView() {

        Toolbar toolbar = findViewById(R.id.toolbar5);
        toolbar.setTitle("學號 : " + getSharedPreferences("data", 0).getString("ID", ""));

        RecyclerView recyclerView = findViewById(R.id.select_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new Recycler_Select_question(context, question_data, getSharedPreferences("data", 0)));

    }

    public void done(View view) {

        int count = 3;
        final Class<MainActivity> nextClass = MainActivity.class;

//        Log.d("logg", "done touch"+getSharedPreferences("data", 0).getInt("count", -1));
        SharedPreferences preferences = getSharedPreferences("data", 0);

        if (checkAnswer() || count <= preferences.getInt("count", -1)) {

            if (count > preferences.getInt("count", -1)) {

//                Log.d("logg",  "hereee  "+preferences.getString("ID", "")+preferences.getString("name", ""));

                Firestore_sutdent db = new Firestore_sutdent(context, preferences.getString("ID", ""),
                        preferences.getString("name", ""), count , preferences);

                db.upload_data(true);

                preferences.edit().putInt("count", count).apply();

                new AlertDialog.Builder(context).setTitle("闖關成功!!!").setMessage("破完全部關卡了!!\n")
                        .setPositiveButton("結束...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, nextClass);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("先等等...", null).show();


            } else {

//                Log.d("logg", "false");

                new AlertDialog.Builder(context).setTitle("已經破過了!!").setMessage("到下一關去")
                        .setPositiveButton("下一關", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(context, nextClass));
                            }
                        }).setNegativeButton("先等等", null).show();

            }


        } else {

            new AlertDialog.Builder(context).setTitle("闖關失敗...").setMessage("好像哪邊搞錯了...\n快把他找出來!!")
                    .setNegativeButton("返回", null).show();

        }

    }

    private boolean checkAnswer() {

        HashMap<String, Boolean> custom = new HashMap<>();
        boolean temp;

        for (int question = 0; question < question_data.get_All_question().size(); question++) {
            for (int item = 0; item < question_data.get_item(question).size(); item++) {
                String mapID = "Select" + (question + 1) + "-" + (item + 1);
                temp = getSharedPreferences("data", 0).getBoolean(mapID, false);

                if (temp) {
                    custom.put(mapID, true);
                }
            }
        }

        return custom.equals(answer);

    }

    private void clear_shareprefer() {

        SharedPreferences preferences = getSharedPreferences("data", 0);

        String ID = preferences.getString("ID", "");
        String name = preferences.getString("name", "");

        preferences.edit().clear().apply();

        preferences.edit().putString("ID", ID).apply();
        preferences.edit().putString("name", name).apply();

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

class Question_Data {

    private ArrayList<String> question;
    private ArrayList<ArrayList<String>> item;

    Question_Data(ArrayList<String> question, ArrayList<ArrayList<String>> item) {
        this.question = question;
        this.item = item;
    }

    String get_question(int pos) {
        return question.get(pos);
    }

    ArrayList<String> get_item(int question_pos) {
        return item.get(question_pos);
    }

    String get_item(int question_pos, int item_pos) {
        return item.get(question_pos).get(item_pos);
    }

    ArrayList<String> get_All_question() {
        return question;
    }

}