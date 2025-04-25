package com.example.geoquiz.presentation.feature_role;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoquiz.R;
import com.example.geoquiz.presentation.feature_chat.MainFunctionActivity;
import com.example.geoquiz.presentation.feature_rider.RiderActivity;

/**
 * Role chooser screen allowing user to select Rider or User flow.
 */
public class RoleChooserActivity extends AppCompatActivity {

    private Button btnUser;
    private Button btnRider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_chooser);

        btnRider = findViewById(R.id.btnGoToRider);
        btnUser = findViewById(R.id.btnGoToUser);

        btnUser.setOnClickListener(v-> {
            RoleManager.setRole(getApplicationContext(), RoleManager.Role.USER);
            Intent intent = new Intent(RoleChooserActivity.this, MainFunctionActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        btnRider.setOnClickListener(v-> {

            RoleManager.setRole(getApplicationContext(), RoleManager.Role.RIDER);
            Intent intent = new Intent(RoleChooserActivity.this, RiderActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }
}
