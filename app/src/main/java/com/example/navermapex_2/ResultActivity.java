package com.example.navermapex_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private ImageButton prev_btn, exit_btn;
    private Button search, dst_btn;
    private UiSettings uiSettings;
    private String user_input; //searchFragment에서 사용자가 입력한 값
    private int[] place; // json에서 읽어온 좌표
    private JsonConverter jc;

    private double latitude = 35.83609; // 위도
    private double longitude = 128.75290; // 경도
    private int defaultZoom = 14; // 줌
    private int defaultTilt = 0; // 기울기
    private int defaultBearing = 0; // 베어링(회전)

    private CameraPosition cameraPosition;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_result);

        search = findViewById(R.id.search_bar);
        prev_btn = findViewById(R.id.prev_button);
        exit_btn = findViewById(R.id.exit_button);
        dst_btn = findViewById(R.id.dst_button);

        //이전화면(SearchActivity)에서 유저입력값 받아오기
        Intent connect = getIntent();
        user_input = connect.getStringExtra("user_input");
        search.setText(user_input); //유저 입력결과 위에 표시

        // 네이버 지도 관련 설정
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // json에서 검색지 좌표 읽어오기
        String path = "local/test.json";
        jc = new JsonConverter(ResultActivity.this, path);
        place = jc.strToArray();

        // 유저가 검색결과창에서 다시 검색한 경우
        if(connect.getStringExtra("new_user_input") != null) {
            String new_user_input = connect.getStringExtra("new_user_input");
            search.setText(new_user_input); //유저가 다시 검색한 결과를 검색창에 표시

            //새로 검색한 위치에 대한 좌표 받아오기
            path = "local/test3.json";
            jc = new JsonConverter(ResultActivity.this, path);
            place = jc.strToArray();
        }

        // 뒤로가기 버튼 눌렀을 때
        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 이전화면으로 이동
                finish(); //디바이스 뒤로가기 버튼으로 못돌아오게하기
            }
        });

        // X버튼 눌렀을 때
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 홈화면으로 이동
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Main액티비티위에 있던 액티비티들 모두 삭제
                startActivity(intent);
                finish();
            }
        });
        // 검색바 눌렀을 때
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 간단한 검색화면으로 이동
                Intent intent = new Intent(ResultActivity.this, ModifyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish(); //새로운 검색결과창 띄워주기위해 현재 액티비티 종료

            }
        });
        // 목적지 설정 버튼 눌렀을 때
        dst_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //경로탐색 결과화면으로 이동
                //검색한 장소의 좌표 넘겨야함.(place 배열 전달할 것)
                Intent intent = new Intent(ResultActivity.this, RoadResultActivity.class);
                intent.putExtra("result_coordinate", place);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

    }

    // NaverMap 객체가 준비되면 호출되는 메소드
    @Override
    public void onMapReady (@NonNull NaverMap naverMap){
        // default 카메라 설정
        cameraPosition = new CameraPosition(
                new LatLng(latitude, longitude),
                defaultZoom,
                defaultTilt,
                defaultBearing
        );

        naverMap.setCameraPosition(cameraPosition);

        // 네이버 지도 관련 ui 설정
        uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(false); // 좌측 상단 나침반 제거
        uiSettings.setTiltGesturesEnabled(false); // 틸트 제스처 해제
        uiSettings.setRotateGesturesEnabled(true); // 회전 제스처 false -> true 변경
        uiSettings.setLocationButtonEnabled(false); // 기본 로케이션 버튼 해제

        // 카메라 영역 범위 제한
        LatLng southWest = new LatLng(35.822000, 128.746500); //서남단
        LatLng northEast = new LatLng(35.838300, 128.764800); //동북단
        naverMap.setExtent(new LatLngBounds(southWest, northEast));

        //zoom 범위 제한
        naverMap.setMinZoom(14.0); //최소
        naverMap.setMaxZoom(19.0); //최대

        //격자 그리기
        int div_ns = 100; //영남대 세로칸 수
        int div_ew = 100; //영남대 가로칸 수
        List<LatLng> points;
        points = drawGrid();
        PathOverlay path = new PathOverlay();
        path.setCoords(points);

        path.setMap(naverMap);
        path.setWidth(1);

        //(0,0)좌표의 위도경도
        double startfindy = 35.822000 + 0.016300 - (0.016300/div_ns/2);
        double startfindx = 128.746500 + (0.018300/div_ew/2);
        LatLng yuCord = (new LatLng(startfindy - place[1]*0.016300/div_ns, startfindx + place[0]*0.018300/div_ew));
        marker = new Marker();
        marker.setPosition(yuCord);
        marker.setMap(naverMap); // 좌표 상 장소를 마커로 표시
    }

    public List<LatLng> drawGrid() {
        // 격자 그리기
        List<LatLng> points=new ArrayList<LatLng>();
        points.add(new LatLng(35.822000, 128.746500)); //왼쪽아래 좌표
        points.add(new LatLng(35.822000, 128.764800));
        points.add(new LatLng(35.838300, 128.764800)); //오른쪽 위 좌표
        points.add(new LatLng(35.838300, 128.746500));
        points.add(new LatLng(35.822000, 128.746500));

        int div_ns = 100; //영남대 세로칸 수
        int div_ew = 100; //영남대 가로칸 수
        //-> 무조건 짝수로 맞추기!!!!!!!!!!!!!
        //가로로 쪼개기 (세로칸만들기
        for (int i = 0; i<=div_ns; i++){
            if(i%2==0) { //짝수번
                points.add(new LatLng(35.822000 + 0.016300/div_ns * i, 128.746500));//올리고
                points.add(new LatLng(35.822000 + 0.016300/div_ns * i, 128.746500 + 0.018300));//오른쪽으로

            }
            else{ //홀수번
                points.add(new LatLng(35.822000 + 0.016300/div_ns * i, 128.746500 + 0.018300));//올리고
                points.add(new LatLng(35.822000 + 0.016300/div_ns * i, 128.746500));//왼쪽으로
            }
        }

        //시작위치
        points.add(new LatLng(35.838300, 128.746500));
        points.add(new LatLng(35.822000, 128.746500));

        //세로로 쪼개기 (가로칸 만들기)
        for (int i = 0; i<=div_ew; i++){
            if(i%2==0) { //짝수번
                points.add(new LatLng(35.822000, 128.746500 + 0.018300/div_ew * i));//오른쪽으로
                points.add(new LatLng(35.822000 + 0.016300, 128.746500 + 0.018300/div_ew * i));//위로
            }
            else{ //홀수번
                points.add(new LatLng(35.822000 + 0.016300, 128.746500 + 0.018300/div_ew * i));//오른쪽으로
                points.add(new LatLng(35.822000, 128.746500 + 0.018300/div_ew * i));//아래
            }
        }

        return points;
    }

    //MapView 라이프사이클 메서드
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}