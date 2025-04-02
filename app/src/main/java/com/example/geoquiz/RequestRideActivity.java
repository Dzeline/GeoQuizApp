package com.example.geoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RequestRideActivity extends AppCompatActivity {

    private LinearLayout riderSection;
    private MaterialButton btnShareLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.riderequest); // Links to riderequest.xml

        // View bindings
        riderSection = findViewById(R.id.riderSection);



        findViewById(R.id.btnBacktoChat).setOnClickListener(v -> {
            Intent intent = new Intent(RequestRideActivity.this, Main_function.class);
            startActivity(intent);
        });

        btnShareLocation = findViewById(R.id.btnShareLocation);

        // Default: Requester mode
        riderSection.setVisibility(View.GONE);
        btnShareLocation.setVisibility(View.GONE);

        // back to chat roles




    }

}
