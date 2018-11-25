package com.bicyle.bicycle.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bicyle.bicycle.Data.DataManager;
import com.bicyle.bicycle.MapActivity;
import com.bicyle.bicycle.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MapRouteConfirm extends Activity {

    private ArrayList<MyPoint> myRouteList;
    private double distance;
    private Route_boardDTO route_dto = new Route_boardDTO();
    private String start_address;
    private String end_address;
    private EditText source_edt;
    private EditText dest_edt;
    private EditText body_edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.map_route_confirm);

        source_edt = (EditText) findViewById(R.id.map_route_confirm_source_edt);
        dest_edt = (EditText) findViewById(R.id.map_route_confirm_dest_edt);
        body_edt = (EditText) findViewById(R.id.map_route_confirm_body_edt);

        //firebaseDatabase 설정
        DataManager.getInstance().firebaseDatabase = FirebaseDatabase.getInstance();
        DataManager.getInstance().databaseReference = DataManager.getInstance().firebaseDatabase.getReference();


        //데이터 가져오기
        myRouteList= new ArrayList<MyPoint>();
        myRouteList = (ArrayList<MyPoint>) getIntent().getSerializableExtra("route_list");
        distance = getIntent().getDoubleExtra("distance",0);
        start_address = getIntent().getStringExtra("start_address");
        end_address = getIntent().getStringExtra("end_address");
    }

    //확인 버튼 클릭
    public void mOnRouteConfirmYes(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        route_dto.setBody(body_edt.getText().toString());
        route_dto.setDistance(distance);
        route_dto.setRoute_tmap(myRouteList);
        route_dto.setUid(DataManager.getInstance().uid);
        route_dto.setStartPoint(source_edt.getText().toString());
        route_dto.setEndPoint(dest_edt.getText().toString());
        DataManager.getInstance().databaseReference.child("route_Board").push().setValue(route_dto);

        //액티비티(팝업) 닫기
        finish();
    }

    //취소 버튼 클릭
    public void mOnRouteConfirmNo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}


