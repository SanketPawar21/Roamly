package com.Sanket.roamly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ImageView ivRoamly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ivRoamly = findViewById(R.id.ivroamly);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is logged in — check their role
                checkUserRole(currentUser.getUid());
            } else {
                // Not logged in → go to LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 3000); // 3 seconds delay
    }

    private void checkUserRole(String uid) {
        userRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    if ("Admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, UserDashboardActivity.class));
                    }
                } else {
                    // Default to user dashboard if role not found
                    startActivity(new Intent(SplashActivity.this, UserDashboardActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // On error, still proceed to user dashboard safely
                startActivity(new Intent(SplashActivity.this, UserDashboardActivity.class));
                finish();
            }
        });
    }
}
