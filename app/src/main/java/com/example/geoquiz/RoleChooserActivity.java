package com.example.geoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RoleChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_chooser);

        Button btnRider = findViewById(R.id.btnGoToRider);
        Button btnUser = findViewById(R.id.btnGoToUser);

        btnRider.setOnClickListener(v -> {
            Intent intent = new Intent(RoleChooserActivity.this, RiderActivity.class);
            startActivity(intent);
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(RoleChooserActivity.this, RequestRideActivity.class);
            startActivity(intent);
        });
    }

}
