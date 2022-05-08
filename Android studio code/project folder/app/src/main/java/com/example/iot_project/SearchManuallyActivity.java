package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class SearchManuallyActivity extends AppCompatActivity {
    Button button_enter_device_id;
    EditText send_text;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_manually);
        Intent intent = getIntent();
        username = intent.getStringExtra("message_key2");
        button_enter_device_id = findViewById(R.id.button_enter_device_id);
        send_text = findViewById(R.id.device_id_input);
        button_enter_device_id.setOnClickListener(view -> {
            String str = send_text.getText().toString();
            Intent intent2 = new Intent(getApplicationContext(), DisplayActivity.class);
            intent2.putExtra("message_key", str);
            intent2.putExtra("message_key2", username);
            startActivity(intent2);
        });
    }
}