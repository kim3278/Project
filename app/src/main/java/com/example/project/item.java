package com.example.project;

public class item {
    String mText1;
    String mText2;
    String mText3;

    public item(String name, String time, String memo){
        mText1 = name;
        mText2 = time;
        mText3 = memo;
    }

    public String getName(){
        return mText1;
    }

    public String getTime(){
        return mText2;
    }

    public String getMemo(){
        return mText3;
    }
}
