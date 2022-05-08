package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicReference;

public class DashboardActivity extends AppCompatActivity {
    TextView textview1;
    String username;
    Button button1, button2;
    DatabaseReference reference, reference2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        textview1 = findViewById(R.id.userID);
        textview1.setText("user : " + username);
//        button1=findViewById(R.id.edit_favorites);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(DashboardActivity.this, EditFavoritesActivity.class));
//            }
//        });
        button2=findViewById(R.id.search_manually);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchManuallyActivity.class);
                intent.putExtra("message_key2", username);
                startActivity(intent);
            }
        });
        fill_weather_station_data(username);
    }
    private void fill_weather_station_data(String username) {
        AtomicReference<String> count = new AtomicReference<>("0");
        ViewGroup inclusionViewGroup = (ViewGroup)findViewById(R.id.liner_layout_for_data_in_dashboard_activity);
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference2 = FirebaseDatabase.getInstance().getReference("test");
        reference.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(DashboardActivity.this, "Successfully Read", Toast.LENGTH_SHORT).show();
                    DataSnapshot dataSnapshot = task.getResult();
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String key = ds.getKey();
                        if(key.equals("favorites")) {
                            for(DataSnapshot child : ds.getChildren()) {
                                String wsname = child.getKey();
                                String valueofparam = String.valueOf(ds.child(wsname).getValue());
                                if(valueofparam.equals("1")) {
                                    View child_layout = LayoutInflater.from(this).inflate(R.layout.item_data, null);
                                    LinearLayout insidelinearLayout = (LinearLayout)child_layout.findViewById(R.id.linear_layout_for_textview);
                                    TextView textView = (TextView) child_layout.findViewById(R.id.title);
                                    textView.setText(wsname);
                                    reference2.child(wsname).get().addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) {
                                            if(task1.getResult().exists()) {
                                                DataSnapshot wsss = task1.getResult();
                                                for (DataSnapshot wscss : wsss.getChildren())
                                                {
                                                    String param = wscss.getKey();
                                                    String paramval = String.valueOf(wsss.child(param).getValue());
                                                    TextView textView2 = new TextView(this);
                                                    textView2.setText(param+" : "+paramval);
                                                    insidelinearLayout.addView(textView2);
                                                }
                                            }else {
                                                String errorException = task.getException().toString();
                                                Toast.makeText(DashboardActivity.this, errorException, Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            String errorException = task.getException().toString();
                                            Toast.makeText(DashboardActivity.this, errorException, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    child_layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getApplicationContext(), DisplayActivity.class);
                                            intent.putExtra("message_key", wsname);
                                            intent.putExtra("message_key2", username);
                                            startActivity(intent);
                                        }
                                    });
                                    TextView tt = (TextView) findViewById(R.id.no_devices_text);
                                    tt.setText(" ");
                                    inclusionViewGroup.addView(child_layout);
                                }
                            }
                        }
                        else if(key.equals("flag")) {
                            String flagval = String.valueOf(ds.child("flag").getValue());
                            count.set(flagval);
                        }
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Favorites Doesn't Exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorException = task.getException().toString();
                Toast.makeText(DashboardActivity.this, errorException, Toast.LENGTH_SHORT).show();
            }
        });
    }
}