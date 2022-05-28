package com.example.navermapex_2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

//길찾기 목적지 검색 화면
public class RoadDstSearchActivity extends AppCompatActivity {

    private EditText search; // 검색바
    private ImageButton search_btn, prev_btn; // 검색버튼, 뒤로가기 버튼
    private String user_input; // 유저입력내용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_road_dst_search);

        search = findViewById(R.id.search_bar);
        search_btn = findViewById(R.id.search_button);
        prev_btn = findViewById(R.id.prev_button);

        search.setFocusable(true);

        //Intent 전달값 받기
        Intent iData = getIntent();
        double[] src_coordinate = iData.getDoubleArrayExtra("src_coordinate"); //출발지 좌표

        // 뒤로가기 버튼 눌렀을 때
        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 검색버튼 눌렀을 때
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_input = search.getText().toString(); //검색창에 입력한 내용
                if(!(user_input == null || user_input.isEmpty())) { //입력값이 공백일 때 화면전환x
                    // 서버에 유저가 입력한 목적지의 좌표 요청 -> json 파일 다운/갱신


                    // 검색결과 경로안내 화면으로 이동
                    Intent intent = new Intent(RoadDstSearchActivity.this, RoadResultActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra("src_coordinate", src_coordinate); //출발지 좌표 전달
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}