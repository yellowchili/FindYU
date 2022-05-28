package com.example.navermapex_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.util.FusedLocationSource;


public class MainActivity extends AppCompatActivity {


    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private MapFragment mapFragment;
    private ShareFragment shareFragment;
    private TourFragment tourFragment;
    private EventFragment eventFragment;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;

    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate","ENTER");

        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_main);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        bottomNavigationView = findViewById(R.id.bottomNavi);



        // 하단 네비게이션 뷰 아이템 선택 리스너
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_bar_map: // 지도
                        setFrag(0);
                        break;
                    case R.id.action_bar_share: // 위치 공유
                        setFrag(1);
                        break;
                    case R.id.action_bar_walk: // 투어
                        setFrag(2);
                        break;
                    case R.id.action_bar_concert: // 행사
                        setFrag(3);
                        break;
                }
                return true;
            }
        });

        mapFragment = new MapFragment();
        mapFragment.getLS(locationSource);
        shareFragment = new ShareFragment();
        tourFragment = new TourFragment();
        eventFragment = new EventFragment();

        setFrag(0); // 기본 화면 지정
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    // 하단 네비게이션에서 아이템 선택시 해당하는 프래그먼트로 이동
    public void setFrag(int num) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (num) {
            case 0:
                fragmentTransaction.replace(R.id.main_frame, mapFragment);
                break;
            case 1:
                fragmentTransaction.replace(R.id.main_frame, shareFragment);
                break;
            case 2:
                fragmentTransaction.replace(R.id.main_frame, tourFragment);
                break;
            case 3:
                fragmentTransaction.replace(R.id.main_frame, eventFragment);
                break;
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed","ENTER");

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 눌러 종료", Toast.LENGTH_SHORT).show();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume","ENTER");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("onStart","ENTER");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause","ENTER");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("onRestart","ENTER");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop","ENTER");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy","ENTER");
    }
}