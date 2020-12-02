package com.bacon.low_work;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.rpc.context.AttributeContext;
import com.google.type.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class Recycler_Selest_item extends RecyclerView.Adapter<Recycler_Selest_item.ViewHolder>{

    private Question_Data question_data;
    private Context context;
    private int number;
    private SharedPreferences preferences;

    Recycler_Selest_item(Context context,Question_Data question_data,int number,SharedPreferences preferences){
        this.question_data = question_data;
        this.context = context;
        this.number = number;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.recycler_select_item,parent,false);
        ViewHolder holder = new ViewHolder(v);

        holder.checkBox = v.findViewById(R.id.checkBox);
        holder.item = v.findViewById(R.id.item);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

//        Log.d("logg",question_data.get_item(number,position));

        holder.item.setText(question_data.get_item(number,position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                preferences.edit().putBoolean("Select"+(number+1)+"-"+(position+1),isChecked).apply();

            }
        });

    }

    @Override
    public int getItemCount() {
        return question_data.get_item(number).size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;
        TextView item;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}


class Recycler_Select_question extends RecyclerView.Adapter<Recycler_Select_question.ViewHolder> {

    private Context context;
    private Question_Data question_data;
    private SharedPreferences preferences;

    Recycler_Select_question(Context context,Question_Data question_data,SharedPreferences preferences){

        this.context = context;
        this.question_data = question_data;
        this.preferences = preferences;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.recycle_select_question,parent,false);
        ViewHolder holder = new ViewHolder(v);

        holder.question = v.findViewById(R.id.question_content);
        holder.number = v.findViewById(R.id.number);
        holder.item_recycler = v.findViewById(R.id.recycler_item);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int temp = position+1;
        holder.number.setText(temp+".");

//        Log.d("logg",question_data.get_question(position));
        holder.question.setText(question_data.get_question(position));
        holder.item_recycler.setLayoutManager(new GridLayoutManager(context,4));
        holder.item_recycler.setAdapter(new Recycler_Selest_item(context,question_data,position,preferences));

    }

    @Override
    public int getItemCount() {
//        Log.d("logg","data_count = "+question_data.get_All_question().size());
        return question_data.get_All_question().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView number,question;
        RecyclerView item_recycler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}


class ClothesGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> map_key;
    private HashMap<String,Integer> answer;

    ClothesGridAdapter(Context context, ArrayList<String> map_key,HashMap<String,Integer> answer){

        this.context = context;
        this.map_key = map_key;
        this.answer = answer;

    }

    @Override
    public int getCount() {
        return map_key.size();
    }

    @Override
    public Object getItem(int position) {
        return map_key.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.clothes,parent,false);
        }

        TextView textView = convertView.findViewById(R.id.back);
        textView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        textView.setTag(map_key.get(position));

        ImageView view = convertView.findViewById(R.id.clothes);
        view.setImageResource(answer.get(map_key.get(position)));

        return convertView;
    }
}


class NameGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> data;

    NameGridAdapter(Context context, ArrayList<String> data){

        this.context = context;
        this.data = data;


    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.name,parent,false);;
        }

        TextView text = convertView.findViewById(R.id.name);
        text.setText(data.get(position));
        text.setTextColor(context.getResources().getColor(R.color.black));


        return convertView;
    }
}


class Recycler_TeacherUse extends RecyclerView.Adapter<Recycler_TeacherUse.ViewHolder> {

    private Context context;
    private ArrayList<HashMap<String,String>> list_data;

    Recycler_TeacherUse(ArrayList<HashMap<String,String>> data, Context context){
        list_data = data;
        this.context = context;
    }


    public void notifyDataSetChanged(ArrayList<HashMap<String,String>> data){
//        list_data.clear();
//        list_data.addAll(data);
        list_data = data;
        super.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recycler_teacher,parent,false);
        ViewHolder holder = new ViewHolder(view);

        holder.ID = view.findViewById(R.id.student_id);
        holder.name = view.findViewById(R.id.name);
        holder.count = view.findViewById(R.id.count);
        holder.date = view.findViewById(R.id.date);

        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.ID.setText(list_data.get(position).get("ID"));
        holder.name.setText(list_data.get(position).get("name"));

        String count = list_data.get(position).get("count");

        if (!"3".equals(count)) {
            holder.count.setTextColor(context.getColor(R.color.work_not));
        }else{
            holder.count.setTextColor(context.getColor(R.color.work_done));

        }

        holder.count.setText(count+"/3");
        holder.date.setText(list_data.get(position).get("date"));
//        Log.d("logg","in size = " + list_data.get(position).size());

    }

    @Override
    public int getItemCount() {
        return list_data.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder{

        TextView ID,name,date,count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
