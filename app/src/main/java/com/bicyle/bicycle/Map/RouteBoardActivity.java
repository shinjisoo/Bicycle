package com.bicyle.bicycle.Map;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bicyle.bicycle.Data.DataManager;
import com.bicyle.bicycle.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RouteBoardActivity extends AppCompatActivity {

    ListView listview;
    RouteBoardAdapter routeAdapter;
    ArrayList<Route_boardDTO> route_BoardList = new ArrayList<>();
    Route_boardDTO curItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_board);



        //firebaseDatabase 설정
        DataManager.getInstance().firebaseDatabase = FirebaseDatabase.getInstance();
        DataManager.getInstance().databaseReference = DataManager.getInstance().firebaseDatabase.getReference();

        listview= (ListView)findViewById(R.id.route_board);
        routeAdapter=new RouteBoardAdapter(RouteBoardActivity.this, R.layout.map_route_row, route_BoardList);
        listview.setAdapter(routeAdapter);


        //listview 클릭이벤트
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                curItem = (Route_boardDTO)routeAdapter.getItem(position);

                //수정 curItem을 intent로 보내시면됩니다 다음화면으로

                //저는 확인눌럿다치고 바로 꺼지면서 map_activity로 intent를 보내겠습니다. 그리고나서 map_activity에서 result_ok부분에서 curitem받아서 바로 그려줍니당.
                //그리는코드는 result_ok부분에잇습니다.
                //Map에서 Result받는 부분 추가했음.
                Intent intent = new Intent(getApplicationContext(), MapRouteDownload.class);
                intent.putExtra("result", "route");
                intent.putExtra("route",curItem);
                startActivityForResult(intent, 35);

            }
        });



        DataManager.getInstance().databaseReference.child("route_Board").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("MapActivity2", "onChildAdded");
                Log.d("MapActivity2", dataSnapshot.toString());
                Log.d("MapActivity2", dataSnapshot.getValue().toString());
                Route_boardDTO dto = dataSnapshot.getValue(Route_boardDTO.class);//
                route_BoardList.add(dto);

                routeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("log_test","activityresult에 돌아옴");
        if (resultCode == RESULT_OK) {
            Log.d("log_test", "RESULT_OK");
            String result = data.getStringExtra("result");
            if (result.equals("Load")) {
                Intent intent = new Intent();
                intent.putExtra("result", "route");
                intent.putExtra("route",curItem);
                setResult(RESULT_OK, intent);
                Log.d("log_test","RouteBoardActivity로 넘어옴");
                //액티비티(팝업) 닫기
                finish();
            }
        }
    }
}
