package com.bicyle.bicycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bicyle.bicycle.Board.BoardActivity;
import com.bicyle.bicycle.Data.DataManager;
import com.bicyle.bicycle.Map.MapRouteConfirm;
import com.bicyle.bicycle.Map.MyPoint;
import com.bicyle.bicycle.Map.OthersLocationDTO;
import com.bicyle.bicycle.Map.RouteBoardActivity;
import com.bicyle.bicycle.Map.Route_boardDTO;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.bicyle.bicycle.util.MyUtil.calDistance;

public class MapActivity extends AppCompatActivity {

    double recentLongitude = 0.0;
    double recentlatitude = 0.0;
    int logNum = 0;
    boolean logFlag = false;
    double route_distance = 0;
    String start_address;
    String end_address;
    int downLogNum =0;

    //UI를 위한 객체
    ListView listView;
    Toolbar mainToolbar;
    DrawerLayout dlDrawer;
    ActionBarDrawerToggle dtToggle;

    LocationManager lm;
    Context context = this;
    TMapView tmapview = null;       //tmap
    private double latitude;        //내 위치
    private double longitude;       //내 위치
    private double source_lat = 0;      //출발지 위치
    private double source_long = 0;     //출발지 위치
    private double dest_lat = 0;        //도착지 위치
    private double dest_long = 0;       //도착지 위치
    public int state_flag_bit = 0; //0일때는 지도 검색 1일 때 경로탐색
    public static final int search_popup = 1;       //위치 탐색 인자
    public static final int source_popup = 2;       //경로 출발지 인자
    public static final int dest_popup = 3;         //경로 도착지 인자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapview);
        final LinearLayout search_bar_layout = (LinearLayout) findViewById(R.id.search_bar_layout);
        final LinearLayout route_bar_layout = (LinearLayout) findViewById(R.id.route_bar_layout);
        final TMapData tmapdata = new TMapData();
        final EditText search = (EditText) findViewById(R.id.map_search);
        final Button enter = (Button) findViewById(R.id.map_enter);
        final Button search_mygps = (Button) findViewById(R.id.map_search_mygps);
        final Button route_mygps = (Button) findViewById(R.id.map_route_mygps);
        final Button route = (Button) findViewById(R.id.map_route);
        final Button findroute_enter = (Button) findViewById(R.id.findroute_enter);
        final Button source_enter = (Button) findViewById(R.id.source_enter);
        final Button dest_enter = (Button) findViewById(R.id.dest_enter);
        final Button log_btn = (Button) findViewById(R.id.map_log_btn);
        final EditText source_edt = (EditText) findViewById(R.id.source_edt);
        final EditText dest_edt = (EditText) findViewById(R.id.dest_edt);

        //firebaseDatabase 설정
        DataManager.getInstance().firebaseDatabase = FirebaseDatabase.getInstance();
        DataManager.getInstance().databaseReference = DataManager.getInstance().firebaseDatabase.getReference();


        //티맵 설정
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey("651558bc-d5a6-4dd3-9e96-524bfc2d59b8");
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapview.setSightVisible(true);

        tmapview.setCenterPoint(127.046408,37.283044 );

        setGps();
        setMyGps();

        //DB 넣는 부분
        OthersLocationDTO locationDTO = new OthersLocationDTO(DataManager.getInstance().uid, DataManager.getInstance().userName, 0, 0);
        DataManager.getInstance().databaseReference.child("otherLocation").child(DataManager.getInstance().uid).setValue(locationDTO);


        //DB 부르는 부분
        DataManager.getInstance().databaseReference.child("otherLocation").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //처음에 한번 다읽고, 그후 추가되면 여기로 들어옴
                Log.d("MapActivity", "onChildAdded");
                Log.d("MapActivity", dataSnapshot.toString());
                Log.d("MapActivity", dataSnapshot.getValue().toString());
                OthersLocationDTO locationData = dataSnapshot.getValue(OthersLocationDTO.class);//

                //자신의 uid가 아닌경우만 맵에 표시해줌.
                if (equalsUfid(locationData.getUid())) {
                    //자신의것.
                } else {
                    Log.d("MapActivity", locationData.getUid() + " : " + locationData.getLatitude() + " : " + locationData.getLongitude());
                    MakeOthersLocationCircle(locationData.getUid(), locationData.getNickname(), locationData.getLatitude(), locationData.getLongitude());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //그룹안에 child가 변경되면 circle을 다시 그려줘야함.
                Log.d("MapActivity", "onChildChanged");
                Log.d("MapActivity", dataSnapshot.toString());
                Log.d("MapActivity", dataSnapshot.getValue().toString());
                OthersLocationDTO locationData = dataSnapshot.getValue(OthersLocationDTO.class);//
                Log.d("MapActivity", locationData.getUid() + " : " + locationData.getLatitude() + " : " + locationData.getLongitude());

                //자신의 uid가 아닌경우만 맵에 표시해줌.
                if (equalsUfid(locationData.getUid())) {
                    //자신의것.
                } else {
                    Log.d("MapActivity", locationData.getUid() + " : " + locationData.getLatitude() + " : " + locationData.getLongitude());
                    MakeOthersLocationCircle(locationData.getUid(), locationData.getNickname(), locationData.getLatitude(), locationData.getLongitude());
                }

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

        //툴바 네비게이션
        mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(mainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        dtToggle = new ActionBarDrawerToggle(this, dlDrawer, R.string.app_name, R.string.app_name);
        dlDrawer.addDrawerListener(dtToggle);

        final String[] naviMenuList = getResources().getStringArray(R.array.naviMapMenu);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, naviMenuList);

        listView = (ListView) findViewById(R.id.drawer_menulist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent mainIntent = new Intent(getApplicationContext(), MainScreen.class);
                        startActivity(mainIntent);
                        break;
                    case 1:
                        Intent boardIntent = new Intent(getApplicationContext(), BoardActivity.class);
                        startActivity(boardIntent);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "동호회선택", Toast.LENGTH_SHORT).show();
                        break;
                }
                dlDrawer.closeDrawer(Gravity.LEFT);
            }
        });


        //내위치 버튼
        search_mygps.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state_flag_bit == 0) {     //지도 검색이었을 때 내위치 버튼
                    Log.d("test", "내 위치");
                    setMyGps();
                    tmapview.setCenterPoint(longitude, latitude);
                }
            }
        });
        //주행 시작 버튼
        log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MapActivity", "logBtn");

                //logFlag 는 처음에 false상태
                if (logFlag)     //주행 종료
                {
                    logFlag = false;
                    log_btn.setText("주행 시작");
                    ArrayList<MyPoint> myRouteList = new ArrayList<MyPoint>();
                    //1. 찍은로그 불러옴
                    for (int a = 0; a < logNum; a++) {
                        TMapPolyLine aa = tmapview.getPolyLineFromID(a + "myLog");
                        ArrayList<TMapPoint> arPoint = aa.getLinePoint();
                        myRouteList.add(new MyPoint(arPoint.get(0).getLatitude(), arPoint.get(0).getLongitude()));
                        myRouteList.add(new MyPoint(arPoint.get(1).getLatitude(), arPoint.get(1).getLongitude()));
//                        myRouteList.add(arPoint.get(0));
//                        myRouteList.add(arPoint.get(1));
//                        Toast.makeText(getApplicationContext(),""+arPoint.get(0).getLatitude()+" : "+arPoint.get(0).getLongitude() ,Toast.LENGTH_SHORT).show();

                    }
                    //할거

//                    tmapdata.convertGpsToAddress(myRouteList.get(0).getLatitude(), myRouteList.get(0).getLongitude(),
//                            new TMapData.ConvertGPSToAddressListenerCallback() {
//                                @Override
//                                public void onConvertToGPSToAddress(String strAddress) {
//                                    start_address = strAddress;
//
//                                }
//                            });
//                    tmapdata.convertGpsToAddress(myRouteList.get(logNum-1).getLatitude(), myRouteList.get(logNum-1).getLongitude(),
//                            new TMapData.ConvertGPSToAddressListenerCallback() {
//                                @Override
//                                public void onConvertToGPSToAddress(String strAddress) {
//                                    end_address = strAddress;
//
//                                }
//                            });

                    Intent intent = new Intent(getApplicationContext(), MapRouteConfirm.class);
                    intent.putExtra("route_list", myRouteList);
                    intent.putExtra("distance", route_distance);
                    intent.putExtra("start_address", start_address);
                    intent.putExtra("end_address", end_address);
                    route_distance = 0;
                    startActivity(intent);
                } else {
                    //실행시 logFlag = > True 로 변경
                    if(logNum!=0) {
                        for (int a = 0; a < logNum; a++) {
                            tmapview.removeTMapPolyLine(a + "myLog");
                        }
                    }
                    logNum=0;

                    recentLongitude = longitude;
                    recentlatitude = latitude;
                    tmapview.setTrackingMode(true);
                    logFlag = true;
                    log_btn.setText("주행 종료");
                }

            }
        });

        //출발지에서 내위치 버튼 눌렀을 때
        source_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                if (gainFocus) {
                    route_mygps.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            source_edt.setText("내위치");
                            setMyGps();
                            source_lat = latitude;
                            source_long = longitude;
                            //키보드 내리기
                            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mInputMethodManager.hideSoftInputFromWindow(source_edt.getWindowToken(), 0);
                            source_edt.clearFocus();
                        }
                    });
                }
            }
        });

        //도착지에서 내위치 버튼 눌렀을 때
        dest_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                if (gainFocus) {
                    route_mygps.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dest_edt.setText("내위치");
                            setMyGps();
                            dest_lat = latitude;
                            dest_long = longitude;
                            //키보드 내리기
                            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mInputMethodManager.hideSoftInputFromWindow(dest_edt.getWindowToken(), 0);
                            dest_edt.clearFocus();
                        }
                    });
                }
            }
        });

        //경로탐색 & 지도탐색 버튼
        route.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state_flag_bit == 0) {     //경로 탐색으로 넘어갈 때
                    search_bar_layout.setVisibility(View.GONE);
                    route_bar_layout.setVisibility(View.VISIBLE);
                    findroute_enter.setVisibility(View.VISIBLE);
                    search_mygps.setVisibility(View.GONE);
                    route_mygps.setVisibility(View.VISIBLE);
                    log_btn.setVisibility(View.GONE);
                    route.setText("지도 검색");
                    tmapview.removeAllMarkerItem();
                    search.clearFocus();
                    state_flag_bit = 1;
                } else if (state_flag_bit == 1) {     //지도 검색으로 넘어갈 때
                    search_bar_layout.setVisibility(View.VISIBLE);
                    route_bar_layout.setVisibility(View.GONE);
                    findroute_enter.setVisibility(View.GONE);
                    search_mygps.setVisibility(View.VISIBLE);
                    route_mygps.setVisibility(View.GONE);
                    log_btn.setVisibility(View.VISIBLE);
                    route.setText("경로 탐색");
                    tmapview.removeAllMarkerItem();
                    source_edt.setText(null);
                    source_edt.setHint("출발지");
                    source_edt.clearFocus();
                    dest_edt.setText(null);
                    dest_edt.setHint("도착지");
                    dest_edt.clearFocus();
                    state_flag_bit = 0;
                    source_lat = 0;
                    source_long = 0;
                    dest_lat = 0;
                    dest_long = 0;
                    tmapview.removeTMapPolyLine("findroute");
                }
            }
        });
        //경로 찾기에서 출발지 버튼 눌렀을 때
        source_enter.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("route_test", "출발지 엔터 누름");
                String temp = source_edt.getText().toString();
                searchMapData(temp, source_popup);
            }
        });

        dest_enter.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("route_test", "도착지 엔터 누름");
                String temp = dest_edt.getText().toString();
                searchMapData(temp, dest_popup);

            }
        });
        //경로 탐색 버튼 눌렀을 때
        findroute_enter.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (source_lat == 0 || source_long == 0 || dest_lat == 0 || dest_long == 0) {
                    Toast.makeText(MapActivity.this, "경로를 설정하세요", Toast.LENGTH_SHORT).show();
                } else {
                    // 경로 탐색
                    TMapPoint tMapPointStart = new TMapPoint(source_lat, source_long);
                    TMapPoint tMapPointEnd = new TMapPoint(dest_lat, dest_long);

                    Log.d("route_test", "출발지: " + Double.toString(source_lat) + ", " + Double.toString(source_long));
                    Log.d("route_test", "도착지: " + Double.toString(dest_lat) + ", " + Double.toString(dest_long));
                    /*try {
                        TMapData temp = new TMapData();
                        TMapPolyLine tMapPolyLine = temp.findPathData(tMapPointStart, tMapPointEnd);
                        tMapPolyLine.setLineColor(Color.BLUE);
                        tMapPolyLine.setLineWidth(2);
                        tmapview.addTMapPolyLine("Line1", tMapPolyLine);
                        Log.d("route_test", "경로탐색 시작");
                        Toast.makeText(MapActivity.this, "경로 탐색"+"거리 : "+Double.toString(tMapPolyLine.getDistance()), Toast.LENGTH_SHORT).show();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }*/
                    TMapData data = new TMapData();
                    data.findPathData(tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
                        @Override
                        public void onFindPathData(final TMapPolyLine path) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    path.setLineWidth(5);
                                    path.setLineColor(Color.RED);
                                    tmapview.addTMapPolyLine("findroute", path);

                                }
                            });
                        }
                    });

                }

            }
        });
        //검색 했을 시
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                final String temp = editable.toString();
                //enter를 눌렀을때
                enter.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        search.setText(null);
                        search.setHint("검색");
                        searchMapData(temp, search_popup);
                        //키보드 내리기
                        InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mInputMethodManager.hideSoftInputFromWindow(search.getWindowToken(), 0);

                    }
                });
            }
        });
    }
    //gps 위치에 관한 리스너

    //gps 위치에 관한 리스너
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            if (location != null) {

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    tmapview.setLocationPoint(longitude, latitude);
                    //Toast.makeText(MainActivity.this, "[Gps_Provider]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                    Log.d("test", "[Gps_Provider]gps위치 리스너\n" + latitude + " " + longitude);
//                    tmapview.setTrackingMode(true);  //trackingmode는 한번만?
//                    tmapview.setCenterPoint(longitude, latitude); //윗놈이랑 차이를모르겟고..
                }
//                else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                    tmapview.setLocationPoint(longitude, latitude);
//                    //Toast.makeText(MainActivity.this, "[NETWORK_PROVIDER]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
//                    Log.d("test", "[NETWORK_PROVIDER]gps위치 리스너\n" + latitude + " " + longitude);
////                    tmapview.setTrackingMode(true);
////                    tmapview.setCenterPoint(longitude, latitude);
//                }

                double distance = calDistance(recentlatitude, recentLongitude, latitude, longitude);

                if (distance >= 10) {
                    if (logFlag) {
                        TMapPoint tMapPoint1 = new TMapPoint(recentlatitude, recentLongitude);
                        TMapPoint tMapPoint2 = new TMapPoint(latitude, longitude);

                        TMapPolyLine tpolyline = new TMapPolyLine();
                        tpolyline.setLineColor(Color.RED);
                        tpolyline.setLineWidth(10);
                        tpolyline.addLinePoint(tMapPoint1);
                        tpolyline.addLinePoint(tMapPoint2);
                        tmapview.addTMapPolyLine(logNum + "myLog", tpolyline);
                        route_distance += calDistance(recentlatitude, recentLongitude, latitude, longitude);
//                        Toast.makeText(getApplicationContext(), "선긋기 " + logNum, Toast.LENGTH_SHORT).show();

                        ++logNum;
                        recentLongitude = longitude;
                        recentlatitude = latitude;


                    }

                    //자신의 위치가 바뀔때마다 디비에 올려줘야함.
                    Map<String, Object> taskMap = new HashMap<String, Object>();
                    taskMap.put("latitude", latitude);
                    taskMap.put("longitude", longitude);
                    DataManager.getInstance().databaseReference.child("otherLocation").child(DataManager.getInstance().uid).updateChildren(taskMap);

                }

            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    //현재 위치받기
    public void setGps() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


//        //locationUpdates 는 한번만 등록되어야함 ?
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
//                1000, // 통지사이의 최소 시간간격 (miliSecond)
//                1, // 통지사이의 최소 변경거리 (m)
//                mLocationListener);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // GPS_PROVIDER 권장
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
    }

    //Popup Activity로 데이터 전송
    public void mOnPopupClick(String msg, int requestCode, double temp_lat, double temp_long) {
        if (requestCode == search_popup) {     //검색
            Intent intent = new Intent(this, MapPopupActivity.class);
            intent.putExtra("data", msg);
            intent.putExtra("mode_flag", "검색");
            startActivityForResult(intent, requestCode);
        } else if (requestCode == source_popup) {        //출발지
            Intent intent = new Intent(this, MapPopupActivity.class);
            intent.putExtra("data", msg);
            intent.putExtra("mode_flag", "출발지");
            intent.putExtra("lat", temp_lat);
            intent.putExtra("long", temp_long);
            startActivityForResult(intent, requestCode);
        } else if (requestCode == dest_popup) {          //도착지
            Intent intent = new Intent(this, MapPopupActivity.class);
            intent.putExtra("data", msg);
            intent.putExtra("mode_flag", "도착지");
            intent.putExtra("lat", temp_lat);
            intent.putExtra("long", temp_long);
            startActivityForResult(intent, requestCode);
        }
    }

    //Popup Activity로부터 데이터 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            if (result.equals("OK_source")) {
                Log.d("route_test", "출발지 확인");
                source_lat = data.getDoubleExtra("lat", 0);
                source_long = data.getDoubleExtra("long", 0);
            }
            if (result.equals("OK_dest")) {
                Log.d("route_test", "도착지 확인");
                dest_lat = data.getDoubleExtra("lat", 0);
                dest_long = data.getDoubleExtra("long", 0);
            }
            if (result.equals("NO_source")) {
                Log.d("route_test", "출발지 취소");
                tmapview.removeMarkerItem(Integer.toString(source_popup));
                source_lat = 0;
                source_long = 0;
            }
            if (result.equals("NO_dest")) {
                Log.d("route_test", "도착지 취소");
                tmapview.removeMarkerItem(Integer.toString(dest_popup));
                dest_lat = 0;
                dest_long = 0;
            }

            if(requestCode==35)
            {
                Route_boardDTO route_board = (Route_boardDTO) data.getSerializableExtra("route");
                double sightpoint_lat = (route_board.getRoute_tmap().get(0).getLatitude()+route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLatitude())/2;
                double sightpoint_long = (route_board.getRoute_tmap().get(0).getLongitude()+route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLongitude())/2;;
                tmapview.setCenterPoint(sightpoint_long,sightpoint_lat);

                // 로그 지우기
                if(downLogNum!=0){
                    for (int a = 0; a < downLogNum; a++) {
                        tmapview.removeTMapPolyLine(a + "load_log");
                    }
                    downLogNum=0;
                }

                for(int a=0; a<route_board.getRoute_tmap().size(); )
                {
                    TMapPoint tMapPoint1 = new TMapPoint(route_board.getRoute_tmap().get(a).getLatitude(),route_board.getRoute_tmap().get(a).getLongitude()); //longitude가 뒤로감..
                    TMapPoint tMapPoint2 = new TMapPoint(route_board.getRoute_tmap().get(a+1).getLatitude(),route_board.getRoute_tmap().get(a+1).getLongitude()); //longitude가 뒤로감..

                    TMapPolyLine tpolyline = new TMapPolyLine();
                    tpolyline.setLineColor(Color.GREEN);
                    tpolyline.setLineWidth(10);
                    tpolyline.addLinePoint(tMapPoint1);
                    tpolyline.addLinePoint(tMapPoint2);
                    tmapview.addTMapPolyLine(downLogNum+"load_log",tpolyline);
                    downLogNum++;
                    a+=2;
                }
                makeLoadRoutePin(route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLatitude(),route_board.getRoute_tmap().get(route_board.getRoute_tmap().size()-1).getLongitude());

            }
        }
    }


    //지도 데이터 검색
    public void searchMapData(String temp, final int map_flag) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(temp, new TMapData.FindAllPOIListenerCallback() {
            @Override
            //지역 검색
            public void onFindAllPOI(ArrayList poiItem) {
                //검색결과가 있을 때
                if (poiItem.size() != 0) {
                    TMapPOIItem search_place = (TMapPOIItem) poiItem.get(0);
                    double temp_lat = search_place.getPOIPoint().getLatitude();
                    double temp_long = search_place.getPOIPoint().getLongitude();
                    tmapview.setCenterPoint(temp_long, temp_lat);

                    //핀꽂기
                    makePin(temp_lat, temp_long, search_place, map_flag);


                    mOnPopupClick("검색결과: " + search_place.getPOIName().toString() + "\n"
                            + "주소: " + search_place.getPOIAddress().replace("null", ""), map_flag, temp_lat, temp_long);

                }
                //검색결과가 없을 때
                else {
                    mOnPopupClick("해당 결과가 없습니다.", map_flag, 0, 0);
                }

                                /*          example
                                for(int i = 0; i < poiItem.size(); i++) {
                                    TMapPOIItem  item = (TMapPOIItem) poiItem.get(i);

                                    Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                                            "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                                            "Point: " + item.getPOIPoint().toString());
                                }
                                */
            }
        });
    }

    public void makePin(double temp_lat, double temp_long, TMapPOIItem search_place, int pin_id) {
        //핀꽂기
        TMapPoint dest_point = new TMapPoint(temp_lat, temp_long);
        TMapMarkerItem dest_marker = new TMapMarkerItem();
        dest_marker.setTMapPoint(dest_point);
        dest_marker.setName(search_place.getPOIName().toString());
        dest_marker.setVisible(TMapMarkerItem.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin);
        dest_marker.setIcon(bitmap);
        dest_marker.setCanShowCallout(true);
        if (pin_id == search_popup) {
            dest_marker.setCalloutTitle(search_place.getPOIName().toString());
            dest_marker.setCalloutSubTitle(search_place.getPOIAddress().replace("null", ""));
        } else if (pin_id == source_popup) {
            dest_marker.setCalloutTitle("출발지");
            dest_marker.setCalloutSubTitle(search_place.getPOIAddress().replace("null", ""));
        } else if (pin_id == dest_popup) {
            dest_marker.setCalloutTitle("도착지");
            dest_marker.setCalloutSubTitle(search_place.getPOIAddress().replace("null", ""));
        }
        tmapview.addMarkerItem(Integer.toString(pin_id), dest_marker);
    }

    public void setMyGps() {
        String locationProvider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = lm.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {

            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            //Toast.makeText(MainActivity.this, "[수동gps]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
            Log.d("test", "[수동gps]gps위치 리스너\n" + latitude + " " + longitude);
        }

    }

    public void MakeOthersLocationCircle(String uid, String nickname, double target_lat, double target_long) {
        //핀꽂기
        TMapPoint dest_point = new TMapPoint(target_lat, target_long);
        TMapMarkerItem dest_marker = new TMapMarkerItem();
        dest_marker.setTMapPoint(dest_point);
        dest_marker.setName(nickname);
        dest_marker.setVisible(TMapMarkerItem.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.other_location);
        dest_marker.setIcon(bitmap);
        dest_marker.setCanShowCallout(true);
        dest_marker.setCalloutTitle(nickname);
        tmapview.addMarkerItem(uid, dest_marker);

    }

    public void makeLoadRoutePin(double target_lat, double target_long){
        //핀꽂기
        TMapPoint dest_point = new TMapPoint(target_lat, target_long);
        TMapMarkerItem dest_marker = new TMapMarkerItem();
        dest_marker.setTMapPoint(dest_point);

        dest_marker.setName("도착지");
        dest_marker.setCalloutTitle("도착지");

        dest_marker.setVisible(TMapMarkerItem.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin);
        dest_marker.setIcon(bitmap);
        dest_marker.setCanShowCallout(true);
        dest_marker.setAutoCalloutVisible(true);
        tmapview.addMarkerItem("load_pin", dest_marker);

    }


    public boolean equalsUfid(String targetUid) {
        if (DataManager.getInstance().uid.equals(targetUid)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        dtToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        dtToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (dtToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.logBoard:
                //수정 startActivityForeResult로
                Intent mapIntent = new Intent(getApplicationContext(), RouteBoardActivity.class);
                startActivityForResult(mapIntent,35);
                break;
            case R.id.removeLog:
                for (int a = 0; a < logNum; a++) {
                    tmapview.removeTMapPolyLine(a + "myLog");
                }
                logNum=0;
                for (int a = 0; a < downLogNum; a++) {
                    tmapview.removeTMapPolyLine(a + "load_log");
                }
                downLogNum=0;

        }
        return super.onOptionsItemSelected(item);
    }
}