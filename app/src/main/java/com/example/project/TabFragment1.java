package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class TabFragment1 extends Fragment {
    private static ArrayList<Item> itemArrayList;

    public TabFragment1(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        itemArrayList = new ArrayList<>();

        itemArrayList.add(new Item("약1", "email1", "m1"));
        itemArrayList.add(new Item("약2", "email2", "m2"));
        itemArrayList.add(new Item("약3", "email1", "m3"));
        itemArrayList.add(new Item("약4", "email1", "m4"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tabfragment1, container, false);

        RecyclerView mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        MyAdapter mAdapter = new MyAdapter(itemArrayList);
        mRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return view;
    }
}
