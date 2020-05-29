package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<Item> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mName;
        public TextView mTime;
        public TextView mMemo;

        public ViewHolder(View itemView){
            super(itemView);

            mName = itemView.findViewById(R.id.textView1);
            mTime = itemView.findViewById(R.id.textView2);
            mMemo = itemView.findViewById(R.id.textView3);
        }
    }

    public MyAdapter(ArrayList<Item> myDataset){
        mDataset = myDataset;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mName.setText(mDataset.get(position).getName());
        holder.mTime.setText(mDataset.get(position).getTime());
        holder.mMemo.setText(mDataset.get(position).getMemo());
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }
}
