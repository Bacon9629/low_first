package com.bacon.low_work;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import androidx.annotation.NonNull;

class FireStore_getVersion {

    private int done_flag = 0;  //-1 = fail、0 = 未完成、1 = 已取得version、2 = 已取得version & describe & important
    private String version;
    private boolean important = false;
    private String describe;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FireStore_getVersion(){
        getVersion();
    }

    private void getVersion(){

        db.collection("version").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot temp : queryDocumentSnapshots){

                            if (temp.getId().equals("version")){

                                version = temp.getString("version");
                                Log.d("logg","internet version = "+version);
                                done_flag = 1;

                            }

                            if (temp.getId().equals("version"+"_"+version)){

                                describe = temp.getString("describe");
                                important = temp.getBoolean("important");
                                Log.d("logg","internet describe = "+describe);
                                done_flag = 2;

                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        done_flag = -1;
                    }
                });

    }

    public void retry(){
        done_flag = 0;
        version = "";
        important = false;
        describe = "";
        getVersion();
    }

    public int check_flag(){
        /*
        -1 = fail
        0 = 未完成
        1 = 已取得version
        2 = 已取得version & describe & important
         */
        return done_flag;
    }

    public String getInternet_Version(){
        return version;
    }

    public boolean getImportant(){
        return important;
    }

    public String getDescribe(){
        return describe;
    }

}
