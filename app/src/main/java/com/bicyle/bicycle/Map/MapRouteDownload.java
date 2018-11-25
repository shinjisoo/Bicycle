package com.bicyle.bicycle.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bicyle.bicycle.R;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MapRouteDownload extends AppCompatActivity {

    TMapView tmap_downloadview = null;
    Route_boardDTO route_board;
    int downLogNum=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_route_download);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.map_download_view);
        final TextView source_txt_view = (TextView) findViewById(R.id.map_download_source_txt);
        final TextView dest_txt_view = (TextView) findViewById(R.id.map_download_dest_txt);
        final TextView distance_txt_view = (TextView) findViewById(R.id.map_download_distance_txt);
        final TextView body_txt_view = (TextView) findViewById(R.id.map_download_body_txt);
        final Button load_btn = (Button) findViewById(R.id.map_download_load_btn);
        final Button cancel_btn = (Button) findViewById(R.id.map_download_cancel_btn);


        tmap_downloadview = new TMapView(this);
        linearLayout.addView(tmap_downloadview);
        tmap_downloadview.setSKTMapApiKey("651558bc-d5a6-4dd3-9e96-524bfc2d59b8");
        tmap_downloadview.setCompassMode(true);
        tmap_downloadview.setZoomLevel(15);
        tmap_downloadview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmap_downloadview.setLanguage(TMapView.LANGUAGE_KOREAN);

        route_board = (Route_boardDTO) getIntent().getSerializableExtra("route");
        source_txt_view.setText("출발지 주소 : " +route_board.getStartPoint());
        dest_txt_view.setText("도착지 주소 : "+route_board.getEndPoint());
        int distance = (int) route_board.getDistance();
        distance_txt_view.setText("거리 : "+Integer.toString(distance) +" m");
        body_txt_view.setText("한줄평 : "+route_board.getBody());
        route_board.getRoute_tmap();
        //지도 위치를 로그 찍어준 곳으로 이동시켜줌
        double sightpoint_lat = (route_board.getRoute_tmap().get(0).getLatitude()+route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLatitude())/2;
        double sightpoint_long = (route_board.getRoute_tmap().get(0).getLongitude()+route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLongitude())/2;;
        tmap_downloadview.setCenterPoint(sightpoint_long,sightpoint_lat);

        for(int a=0; a<route_board.getRoute_tmap().size(); )
        {
            TMapPoint tMapPoint1 = new TMapPoint(route_board.getRoute_tmap().get(a).getLatitude(),route_board.getRoute_tmap().get(a).getLongitude()); //longitude가 뒤로감..
            TMapPoint tMapPoint2 = new TMapPoint(route_board.getRoute_tmap().get(a+1).getLatitude(),route_board.getRoute_tmap().get(a+1).getLongitude()); //longitude가 뒤로감..

            TMapPolyLine tpolyline = new TMapPolyLine();
            tpolyline.setLineColor(Color.GREEN);
            tpolyline.setLineWidth(10);
            tpolyline.addLinePoint(tMapPoint1);
            tpolyline.addLinePoint(tMapPoint2);
            tmap_downloadview.addTMapPolyLine(downLogNum+"",tpolyline);
            downLogNum++;
            a+=2;
        }
        Log.d("log_test","MapRouteDownload에 들어옴");

        load_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("log_test","로드버튼 클릭");
                Intent intent = new Intent();
                intent.putExtra("result", "Load");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        cancel_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("log_test","취소버튼 클릭");
                Intent intent = new Intent();
                intent.putExtra("result", "Cancel");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
