package com.example.navermapex_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoadResultActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private ImageButton exit_btn;
    private Button src_search, dst_search;
    private UiSettings uiSettings;
    private int[][] route; // json에서 읽어온 경로

    private double latitude = 35.83609; // 위도
    private double longitude = 128.75290; // 경도
    private int defaultZoom = 14; // 줌
    private int defaultTilt = 0; // 기울기
    private int defaultBearing = 150; // 베어링(회전)
    private CameraPosition cameraPosition;

    //지도 위치 상수
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000; //네이버 위치 상수
    private FusedLocationSource locationSource;
    private NaverMap naverMap;

    private GpsTracker gpsTracker;
    private double gps_latitude; // gps에서 받아온 위도
    private double gps_longitude; // gps에서 받아온 경도
    private String address; //좌표 -> 주소 변환
    private boolean isRoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_road_result);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        src_search = findViewById(R.id.src_search);
        dst_search = findViewById(R.id.dst_search);
        exit_btn = findViewById(R.id.exit_button);

        /* Intent 전달값 받기 */
        Intent iData = getIntent();
        // 장소 검색(ResultActivity)에서 온 목적지 좌표
        int[] dst_coordinate = iData.getIntArrayExtra("result_coordinate");
        // 위 좌표로 서버에서 이 장소의 정식 명칭을 찾고 목적지에 출력해야할듯

        // 길찾기 목적지 검색(RoadDstSearchActivity)에서 온 출발지 좌표
        double[] src_coordinate = iData.getDoubleArrayExtra("src_coordinate");

        if (src_coordinate == null) { // 장소 검색->목적지 선택한 경우 출발지는 GPS로 현재위치 가져오기

            isRoad = false;
            // 현재 위치 받아오기
            if (!checkLocationServicesStatus()) {
                showDialogForLocationServiceSetting();
            }else {
                checkRunTimePermission();
            }

            gpsTracker = new GpsTracker(RoadResultActivity.this); //gps에서 현재 위치 받기
            gps_latitude = gpsTracker.getLatitude(); //gps에서 위도 저장
            gps_longitude = gpsTracker.getLongitude(); //gps에서 경도 저장
            address = getCurrentAddress(gps_latitude, gps_longitude); // 좌표 -> 주소변환
            src_search.setText(address); // 현위치에 주소 셋팅
        }
        else { // 출발지를 이전 페이지에서 받아온 경우 (길찾기 사용한경우)
            address = getCurrentAddress(src_coordinate[0], src_coordinate[1]); // 출발지 좌표 -> 주소변환
            src_search.setText(address); // 출발지 주소 셋팅
            // 목적지 좌표는 서버에 요청했던 json 파일에서 가져오기
        }

        // 네이버 지도 관련 설정
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 서버에 경로 요청하기 위한 출발지, 목적지 셋팅
        if(isRoad) { // 길찾기 이용
            // 길찾기 이용한 경로 : 전달받은 출발지(src_coordinate) -> 목적지는 서버에서 온 JSON에서
        }
        else { //길찾기 이용x
            // 장소 검색 이용한 경로 : gps 현재주소변환 -> 전달받은 목적지
        }

        // JSON에서 경로 읽어오기
        // 출발지, 도착지 좌표를 서버에 넘겨주는방법?
        String path = "local/test2.json";
        JsonConverter jc = new JsonConverter(RoadResultActivity.this, path);
        route = jc.strTo2DArray();

        // X버튼 눌렀을 때
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RoadResultActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Main액티비티위에 있던 액티비티들 모두 삭제
                startActivity(intent);
                finish(); //현재 Activity 종료
            }
        });
        // 출발지 검색바 눌렀을 때
        src_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //장소 검색
            }
        });
        // 목적지 검색바 눌렀을 때
        dst_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //장소 검색
            }
        });

    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        //네이버 위치버튼 인증
        if (locationSource.onRequestPermissionsResult(
                permsRequestCode, permissions, grandResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료함.2가지 경우가 있음

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(RoadResultActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(RoadResultActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크함
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(RoadResultActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(RoadResultActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식함)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요함. 2가지 경우(3-1, 4-1)가 있음

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(RoadResultActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(RoadResultActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoadResultActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoadResultActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더. GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    //GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RoadResultActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

        // 로케이션 버튼 설정
        naverMap.setLocationSource(locationSource);
        LocationButtonView locationButtonView = findViewById(R.id.location_button);
        locationButtonView.setMap(naverMap);

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

        //경로 그리기
        List<LatLng> findpoints=new ArrayList<LatLng>();
        for(int r[] : route){
            //latlng(-y좌표차이, +x좌표차이)

            System.out.println(r[0]+" "+r[1]);
            findpoints.add(new LatLng(startfindy - r[1]*0.016300/div_ns, startfindx + r[0]*0.018300/div_ew));
        }
        PathOverlay findpath = new PathOverlay();
        findpath.setCoords(findpoints);

        findpath.setMap(naverMap);
        findpath.setWidth(10);

    }

    //격자 그려주는 함수
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