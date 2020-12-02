package com.bacon.low_work;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

class Firestore_sutdent {

    private String name, ID;
    private int question_count = 0;
    private FirebaseFirestore db;
    private Context context;
    private boolean doneOrNot_flag = false;
    SharedPreferences preferences;

    Firestore_sutdent(Context context,SharedPreferences preferences) {
        this(context, "", "",0, preferences);

    }

    Firestore_sutdent(Context context, String ID, String name ,SharedPreferences preferences) {
        this(context, ID, name, 0,preferences);
    }

    Firestore_sutdent(Context context, String ID, String name, int question_count , SharedPreferences preferences) {
        this.ID = ID;
        this.name = name;
        this.question_count = question_count;
        this.context = context;
        db = FirebaseFirestore.getInstance();
        this.preferences = preferences;
    }

    void upload_data(boolean upORnot_count) {

        Calendar calendar = Calendar.getInstance();
        CharSequence time = DateFormat.format("yyyy/MM/dd-kk:mm", calendar.getTime());

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("time", time.toString());

//        Log.d("logg", "upLoading  " + name + " " + ID);

        if (upORnot_count) {

            map.put("count", question_count+"");

            db.collection("user").document(ID).set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("logg", "upLoad success  " + name + " " + ID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("logg", "upLoad fail  " + name + " " + ID);
                        }
                    });

        }else{

            db.collection("user").document(ID).set(map,SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("logg", "upLoad success  " + name + " " + ID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("logg", "upLoad fail  " + name + " " + ID);
                        }
                    });

        }


    }

    private HashMap<String, String> map;
    private ArrayList<HashMap<String, String>> data_list = new ArrayList<>();

    void download_ALL_Data() {

        db.collection("user").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot temp : queryDocumentSnapshots.getDocuments()) {

                            if (temp.get("count") != null)
                                map = makeMap(temp.getId(), temp.getString("name"), temp.getString("time"), temp.getString("count"));
                            else
                                map = makeMap(temp.getId(), temp.getString("name"), temp.getString("time"), "0");


                            data_list.add(map);
//                            Log.d("logg",temp.getId()+"  "+temp.getString("name"));

                        }

                        Log.d("logg", "downLoad finish!!");

                        doneOrNot_flag = true;


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "下載失敗，請重啟程式", Toast.LENGTH_LONG).show();
                    }
                });


    }

    void download_student_Data(){

        db.collection("user").document(ID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int count=0;
                        if (documentSnapshot.contains("count")) {
//                            Log.d("logg","true");
                            count = documentSnapshot.getString("count").charAt(0) - '0';
                            Log.d("logg", "download count = " + count);
                        }
                        preferences.edit().putInt("count", count).apply();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("logg","fail");
                Toast.makeText(context,"取得前次作業進度失敗",Toast.LENGTH_SHORT).show();
            }
        });

    }


    boolean check_doneOrNot_flag() {

        if (doneOrNot_flag) {
            doneOrNot_flag = false;
            return true;
        } else
            return false;

    }


    ArrayList<HashMap<String, String>> getData() {

        return data_list;
    }

    private HashMap<String, String> makeMap(String ID, String name, String date, String count) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ID", ID);
        map.put("name", name);
        map.put("date", date);
        map.put("count", count);

        return map;
    }

}
