package com.example.navermapex_2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class SearchActivity extends AppCompatActivity {

    private EditText search;
    private ImageButton search_btn, prev_btn;
    private String user_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_search);

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
                user_input = search.getText().toString(); //검색창에 입력한 내용
                if(!(user_input == null || user_input.isEmpty())) { //입력값이 공백일 때 화면전환x
                    // 검색결과 화면으로 이동
                    Intent intent = new Intent(SearchActivity.this, ResultActivity.class);
                    intent.putExtra("user_input", user_input);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    search.setText("");
                }
            }
        });
    }
}