package com.ampereplus.homeplus;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends ArrayAdapter<String>  {

    int resourceLayout;
    Context mContext;

    customButtonListener customListner;

    public interface customButtonListener {
        public void onButtonClickListener(int position,String value);
        public void onButtonLongClickListener(int position,String value);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }


    public ListAdapter(@NonNull Context context, int resource, @NonNull List<String> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        final int pos=position;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        final String p = getItem(position);
        if (p != null) {
            TextView list_label = (TextView) v.findViewById(R.id.firstTextView);
            if(list_label!=null)
                list_label.setText(p);
        }
        TextView item_label=(TextView)v.findViewById(R.id.firstTextView);
        //ImageView btn=(ImageView)v.findViewById(R.id.firstImageView);
      /*  btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customListner != null) {
                    customListner.onButtonClickListener(pos,p);
                }

            }
        });*/

        item_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customListner != null) {
                    customListner.onButtonClickListener(pos,p);
                }

            }
        });

        item_label.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (customListner != null) {
                    customListner.onButtonLongClickListener(pos, p);
                }
                return true;
            }
        });

        return v;


    }
}
