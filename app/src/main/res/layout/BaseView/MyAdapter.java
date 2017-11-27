package com.example.user.glad_passenager.BaseView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.glad_passenager.R;

/**
 * Created by USER on 2017/11/24.
 */

public class MyAdapter extends BaseAdapter {
    private String[] arrylist;
    private LayoutInflater myInflater;

    public MyAdapter (Context c, String[] str){
        myInflater = LayoutInflater.from(c);
        arrylist = str;
    }

    @Override
    public int getCount() {
        return arrylist.length;
    }

    @Override
    public Object getItem(int position) {
        return arrylist[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = myInflater.inflate(R.layout.mylayout, null);

        ImageView imgLogo = (ImageView)convertView.findViewById(R.id.navIcon);
        TextView navtxt=(TextView)convertView.findViewById(R.id.navText);
        navtxt.setText(arrylist[position]);
        return convertView;
    }
}
