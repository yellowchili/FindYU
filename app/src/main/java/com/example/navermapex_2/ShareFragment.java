package com.example.navermapex_2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.widget.LocationButtonView;

import java.util.Vector;
import java.util.concurrent.Executor;

public class ShareFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private MapView mapView;
    private UiSettings uiSettings;

    private GpsTracker gpsTracker;
    private double gps_latitude; // gps에서 받아온 위도
    private double gps_longitude; // gps에서 받아온 경도

    private Button shareBtn;
    private Marker marker = new Marker();
    private int defaultZoom = 14; // 줌
    private int defaultTilt = 0; // 기울기
    private int defaultBearing = 150; // 베어링(회전)
    private CameraPosition cameraPosition;
    private static final String TAG = "ShareFragment"; //Logcat Tag

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shrare, container, false);

        // 네이버 지도 관련 설정
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        shareBtn = view.findViewById(R.id.share_button); //위치공유버튼

        gpsTracker = new GpsTracker(getActivity()); //gps에서 현재 위치 받기
        gps_latitude = gpsTracker.getLatitude(); //gps에서 위도 저장
        gps_longitude = gpsTracker.getLongitude(); //gps에서 경도 저장

        // 위치공유 버튼 클릭 리스너
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                shareDynamicLink();
            }
        });

        return view;
    }

    // 딥링크 만들고 송신
    private void shareDynamicLink() {
        String shareLink = "https://searchyu.page.link/share?lat="+gps_latitude+"&lon="+gps_longitude;
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(shareLink))
                .setDomainUriPrefix("https://searchyu.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.example.navermapex_2")
                                .setMinimumVersion(0)
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            try {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "share"));
                            } catch (ActivityNotFoundException ignored) {}
                        }else{
                                Log.w(TAG, task.toString());
                            }
                        }
                });
    }


    // NaverMap 객체가 준비되면 호출되는 메소드
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        // 네이버 지도 관련 ui 설정
        uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(false); // 좌측 상단 나침반 제거
        uiSettings.setTiltGesturesEnabled(false); // 틸트 제스처 해제
        uiSettings.setRotateGesturesEnabled(true); // 회전 제스처 false -> true 변경
        uiSettings.setLocationButtonEnabled(false); // 기본 로케이션 버튼 해제

        // 카메라 영역 범위 제한 : 영남대에서 테스트 할 때 주석 해제
        //LatLng southWest = new LatLng(35.822000, 128.746500); //서남단
        //LatLng northEast = new LatLng(35.838300, 128.764800); //동북단
        //naverMap.setExtent(new LatLngBounds(southWest, northEast));

        //zoom 범위 제한
        naverMap.setMinZoom(14.0); //최소
        naverMap.setMaxZoom(19.0); //최대

        // 현재 위치에 마커 찍기
        LatLng cur_location = (new LatLng(gps_latitude, gps_longitude));
        marker = new Marker();
        marker.setPosition(cur_location);
        marker.setMap(naverMap);

        // 마커가 중앙에 오게 카메라 위치 조절
        cameraPosition = new CameraPosition(
                new LatLng(gps_latitude, gps_longitude),
                defaultZoom,
                defaultTilt,
                defaultBearing
        );
        naverMap.setCameraPosition(cameraPosition);
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
