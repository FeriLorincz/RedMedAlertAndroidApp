package com.feri.redmedalertandroidapp;

import android.content.Intent;
import android.os.Bundle;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.feri.redmedalertandroidapp.notification.NotificationSettingsActivity;
import com.feri.redmedalertandroidapp.util.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Verifică și cere permisiunile necesare
        PermissionHelper.requestNotificationPermission(this);

        // Setup pentru butonul de setări notificări
        Button btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        btnNotificationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationSettingsActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Poți adăuga logică specifică aici pentru când utilizatorul răspunde la cererea de permisiuni
    }
}