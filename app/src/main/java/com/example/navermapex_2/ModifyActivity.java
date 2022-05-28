package com.example.navermapex_2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class ModifyActivity extends AppCompatActivity {

    private EditText search; // 검색바
    private ImageButton search_btn, prev_btn; // 검색버튼, 뒤로가기 버튼
    private String user_input; // 유저입력내용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        
        setContentView(R.layout.activity_modify);

        // 검색 결과에서 목적지 수정하는 검색창.
        // 전환될 때 finish() 해줘야함
        search = findViewById(R.id.search_bar);
        search_btn = findViewById(R.id.search_button);
        prev_btn = findViewById(R.id.prev_button);

        search.setFocusable(true);

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
                //입력값이 공백일 때 이동x

                user_input = search.getText().toString(); //검색창에 입력한 내용
                // 검색결과 화면으로 이동
                Intent intent = new Intent(ModifyActivity.this, ResultActivity.class);
                intent.putExtra("new_user_input", user_input);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });
    }
}