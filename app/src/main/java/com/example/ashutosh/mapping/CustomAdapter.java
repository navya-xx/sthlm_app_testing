package com.example.ashutosh.mapping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class CustomAdapter extends ArrayAdapter<String> {
    Context mContext;

    CustomAdapter(Context context, String[] tr) {
        super(context, R.layout.custom_row, tr);
        mContext= context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater list_Inflater = LayoutInflater.from(getContext());
        View customView = list_Inflater.inflate(R.layout.custom_row, parent, false);
        TextView list__Text = (TextView) customView.findViewById(R.id.Text);
        ImageView list_Image = (ImageView) customView.findViewById(R.id.Image);
        String singleTrItem = getItem(position);
        list__Text.setText(singleTrItem);

        /*String uri = "@drawable/tr"+String.valueOf(singleTrItem.id);
        int imageResource = mContext.getResources().getIdentifier(uri, null, mContext.getPackageName());
        list_Image.setImageResource(imageResource);*/
        list_Image.setImageResource(R.drawable.mapper4);

        return customView;
    }


}