package com.bicyle.bicycle.Map;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bicyle.bicycle.R;

import java.util.ArrayList;

public class RouteBoardAdapter extends BaseAdapter {

    Context mContext;
    int layout;
    ArrayList<Route_boardDTO> route_boardList;
    LayoutInflater inflater;



    public RouteBoardAdapter(Context context, int layout, ArrayList<Route_boardDTO> route_boardList) //layout int는 id값
    {
        mContext=context;
        this.layout=layout;
        this.route_boardList=route_boardList;
        inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return route_boardList.size();
    }

    @Override
    public Object getItem(int position) {
        return route_boardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView startPointTV;
        TextView endPointTV;
        if(convertView==null)
        {
            convertView=inflater.inflate(layout,null);

        }
//        Log.d("dddd",convertView.toString());
        startPointTV=convertView.findViewById(R.id.map_route_startPoint);
        endPointTV=convertView.findViewById(R.id.map_route_endPoint);

        Log.d("dddd","변경전 "+startPointTV.getText().toString());

        Route_boardDTO dto=route_boardList.get(position);
//        Log.d("MapActivity2","startPoint"+dto.getStartPoint());
//
        startPointTV.setText(dto.getStartPoint());

        endPointTV.setText(dto.getEndPoint());
        Log.d("dddd","변경후 "+startPointTV.getText().toString());
        return convertView;
    }
}
