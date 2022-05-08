package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DisplayActivity extends AppCompatActivity {
    String deviceID = "boath";
    String exist = "false";
    String yess = "false";
    String deviceexist = "false";
    String username;
    Button button;
    DatabaseReference reference;
    TextView textviewLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Intent intent = getIntent();
        deviceID = intent.getStringExtra("message_key");
        username = intent.getStringExtra("message_key2");
        textviewLocation = findViewById(R.id.location_display);
        button = findViewById(R.id.add_remove_favorites);
        textviewLocation.setText("Device ID : " + deviceID);
        readData(deviceID, username);
        do_needful(username, deviceID);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference("users");
                if(yess.equals("true")) {
                    reference.child(username).child("favorites").child(deviceID).setValue(0);
                }else {
                    reference.child(username).child("favorites").child(deviceID).setValue(1);
                }
                Toast.makeText(DisplayActivity.this, "Done", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(getApplicationContext(), DashboardActivity.class);
                intent2.putExtra("username", username);
                startActivity(intent2);
            }
        });
    }

    private void do_needful(String username, String deviceID) {
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(username).child("favorites").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String key = ds.getKey();
                        if(key.equals(deviceID)) {
                            exist = "true";
                            String val = String.valueOf(dataSnapshot.child(key).getValue());
                            if(val.equals("1")) {
                                yess="true";
                            }
                        }
                    }
                    if(exist.equals("false")) {
                        reference.child(username).child("favorites").child(deviceID).setValue(0);
                    }
                    if(yess.equals("true")) {
                        button.setText("remove from favorites");
                    }else {
                        button.setText("add to favorites");
                    }
                    deviceexist="true";
                } else {
                    Toast.makeText(DisplayActivity.this, "Weather Station Doesn't Exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorException = task.getException().toString();
                Toast.makeText(DisplayActivity.this, errorException, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readData(String username, String ss) {
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.liner_layout_for_data_in_display_activity);
        reference = FirebaseDatabase.getInstance().getReference("test");
        reference.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(DisplayActivity.this, "Successfully Read", Toast.LENGTH_SHORT).show();
                    DataSnapshot dataSnapshot = task.getResult();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String key = ds.getKey();
                        String val = String.valueOf(dataSnapshot.child(key).getValue());
                        View child1 = LayoutInflater.from(this).inflate(R.layout.item_parameter, null);
                        TextView textView = (TextView)child1.findViewById(R.id.parameter);
                        textView.setText(key);
                        TextView textView2 = (TextView)child1.findViewById(R.id.value);
                        textView2.setText(val);
                        inclusionViewGroup.addView(child1);
                    }
                } else {
                    Toast.makeText(DisplayActivity.this, "Weather Station Doesn't Exist", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent2.putExtra("username", ss);
                    startActivity(intent2);
                }
            } else {
                String errorException = task.getException().toString();
                Toast.makeText(DisplayActivity.this, errorException, Toast.LENGTH_SHORT).show();
            }
        });
    }
}