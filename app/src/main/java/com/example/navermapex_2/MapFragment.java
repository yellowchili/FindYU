package com.example.navermapex_2;

import java.util.Vector;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private MapView mapView;
    private UiSettings uiSettings;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private double latitude = 35.83609; // 위도
    private double longitude = 128.75290; // 경도
    private int defaultZoom = 14; // 줌
    private int defaultTilt = 0; // 기울기
    private int defaultBearing = 150; // 베어링(회전)

    private MainActivity mactivity;
    private Button mainSearchBtn;
    private ImageButton imageBtn, roadBtn;
    private NaverMap naverMap;
    private CameraPosition cameraPosition;

    private Marker marker = new Marker();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mactivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mactivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        // 네이버 지도 관련 설정
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        //장소 검색, 이미지 검색, 길찾기
        mainSearchBtn = view.findViewById(R.id.searchPlace);
        imageBtn = view.findViewById(R.id.searchImage);
        roadBtn = view.findViewById(R.id.searchRoad);


        // 검색창 클릭 리스너
        mainSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mactivity, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        // 사진 검색 클릭 리스너
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mactivity, CameraSearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        // 길찾기 클릭 리스너
        roadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //현위치가 출발지로 설정된 검색페이지로 전환
                Intent intent = new Intent(mactivity, RoadActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        return view;

    }


    // NaverMap 객체가 준비되면 호출되는 메소드
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
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
        LocationButtonView locationButtonView = getActivity().findViewById(R.id.location_button);
        locationButtonView.setMap(naverMap);

        // 지도 클릭 시 마커 찍기
        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                if (marker != null) {
                    marker.setMap(null);
                }
                marker = new Marker();
                marker.setPosition(latLng);
                marker.setMap(naverMap);
            }
        });

        //시작!!!!!!
        //편의시설 메뉴 생성
        ChipGroup chipGroup = (ChipGroup) view.findViewById(R.id.chipgroup);
        Chip cafeChip = setChip("카페");
        Chip convenienceChip =setChip("편의점");
        Chip cafeteriaChip =setChip("식당");
        Chip restroomChip = setChip("화장실");
        Chip libraryChip = setChip("도서관");
        Chip pressroomChip = setChip("인쇄실");
        Chip etc1Chip = setChip("etc1");
        Chip etc2Chip = setChip("etc2");
        Chip etc3Chip =setChip("etc3");
        Chip etc4Chip =setChip("etc4");

        chipGroup.addView(cafeChip);
        chipGroup.addView(convenienceChip);
        chipGroup.addView(cafeteriaChip);
        chipGroup.addView(restroomChip);
        chipGroup.addView(libraryChip);
        chipGroup.addView(pressroomChip);
        chipGroup.addView(etc1Chip);
        chipGroup.addView(etc2Chip);
        chipGroup.addView(etc3Chip);
        chipGroup.addView(etc4Chip);

        //해당 카테고리의 모든 좌표값 띄우기
        Vector<LatLng> cafeMarkersPosition = new Vector<LatLng>();
        cafeMarkersPosition.add(new LatLng(35.8319,128.7532));
        cafeMarkersPosition.add(new LatLng(31.833, 128.7581));
        cafeMarkersPosition.add(new LatLng(35.8325, 128.7576));
        cafeMarkersPosition.add(new LatLng(35.8324,128.7576));
        cafeChip.setOnClickListener(new Chip.OnClickListener() {
            public void onClick(View v) {
                for (LatLng markerPosition: cafeMarkersPosition) {
                    Marker marker = new Marker();
                    marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_24));
                    marker.setPosition(markerPosition);
                    marker.setMap(naverMap);
                }
            }
        }); 
        /////끝//////
    }

    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    public void getLS(FusedLocationSource ls) {
        locationSource = ls;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Map onCreate : ", "Enter");

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        Log.e("Map onStart : ", "Enter");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Log.e("Map onResume : ", "Enter");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        Log.e("Map onPause : ", "Enter");
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        Log.e("Map onStop : ", "Enter");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        Log.e("Map onDestroyView : ", "Enter");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /////////////시작//////////////
    //편의시설 메뉴 추가 함수
    private Chip setChip(String chipname) {
        Chip chip = new Chip(getContext());
        chip.setText(chipname);
        chip.setCheckable(false);
        chip.setChipBackgroundColorResource(R.color.white);
        return chip;
    }
    /////////////끝/////////////
}
