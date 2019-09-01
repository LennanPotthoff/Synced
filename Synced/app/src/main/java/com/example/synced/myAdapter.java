package com.example.synced;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class myAdapter extends ArrayAdapter<String> {

    String[] directories;
    Integer[] imagecodes;
    Context mContext;

    public myAdapter(Context context, String[] directoryNames, Integer[] directoryIcons){
        super(context, R.layout.listview);
        this.mContext = context;
        this.directories = directoryNames;
        this.imagecodes = directoryIcons;
    }

    @Override
    public int getCount(){
        return directories.length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.listview, parent, false);
            mViewHolder.directoryImage = convertView.findViewById(R.id.imageView);
            mViewHolder.directoryName =  convertView.findViewById(R.id.textView);
            convertView.setTag(mViewHolder);
        }else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.directoryImage.setImageResource(imagecodes[position]);
        mViewHolder.directoryName.setText(directories[position]);
        return convertView;
    }
    static class ViewHolder{
        ImageView directoryImage;
        TextView directoryName;
    }
}
