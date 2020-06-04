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

        itemArrayList.add(new Item("약품코드 25139-682-13", "매일 20:00", "혈압약"));
        itemArrayList.add(new Item("약품코드 48329-328-34", "매일 7:00", "비타민C"));
        itemArrayList.add(new Item("약품코드 35082-093-29", "매일 8:00", "비타민D"));
        itemArrayList.add(new Item("약품코드 10144-602-15", "매일 12:00", "근육이완제"));
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
