package com.Sanket.roamly;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    private TextView userNameText, userPhoneText, userEmailText, userBioText;
    private ShapeableImageView userProfileImage;

    private RatingBar navUserRatingBar;
    private TextView tvReportWarning;

    private DatabaseReference dbRef;
    private ExecutorService executorService;

    private BroadcastReceiver tripCompletedReceiver;
    private BroadcastReceiver reportWarningReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdashboard_activity);

        // Background executor
        executorService = Executors.newSingleThreadExecutor();

        // Status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryVariant));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Navigation Drawer Header Setup (profile)
        View headerView = navigationView.getHeaderView(0);
        userProfileImage = headerView.findViewById(R.id.imageProfile);
        userNameText = headerView.findViewById(R.id.userName);
        userPhoneText = headerView.findViewById(R.id.userPhone);
        userEmailText = headerView.findViewById(R.id.userEmail);
        userBioText = headerView.findViewById(R.id.userBio);

        // Additional status header with rating bar + warning
        View statusView = getLayoutInflater().inflate(R.layout.nav_user_status, navigationView, false);
        navigationView.addHeaderView(statusView);
        navUserRatingBar = statusView.findViewById(R.id.navUserRating);
        tvReportWarning = statusView.findViewById(R.id.tvReportWarning);
        if (tvReportWarning != null) {
            tvReportWarning.setVisibility(View.GONE);
        }

        loadUserInfo();
        registerTripCompletedReceiver();
        registerReportWarningReceiver();

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new U_HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new U_HomeFragment();
            } else if (id == R.id.nav_my_trips) {
                selected = new MyTripsFragment();
            } else if (id == R.id.nav_chat) {
                selected = new ChatFragment();
            } else if (id == R.id.nav_profile) {
                selected = new U_ProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });

        // Navigation Drawer Items
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.Create_trips) {
                loadFragment(new CreateTripsFragment());
            } else if (id == R.id.nav_saved_places) {
                loadFragment(new SavedPlacesFragment());
            } else if (id == R.id.nav_safety_guidelines) {
                loadFragment(new SafetyGuidelinesFragment());
            } else if (id == R.id.nav_report_user) {
                loadFragment(new ReportUserFragment());
            } else if (id == R.id.nav_emergency) {
                loadFragment(new EmergencyFragment());
            } else if (id == R.id.nav_help_support) {
                loadFragment(new HelpSupportFragment());
            } else if (id == R.id.nav_about_roamly) {
                loadFragment(new AboutRoamlyFragment());
            } else if (id == R.id.nav_privacy_policy) {
                loadFragment(new PrivacyPolicyFragment());
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
                finish();
                return true;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null && (currentFragment == null || !currentFragment.getClass().equals(fragment.getClass()))) {
            currentFragment = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.userFragmentContainer, fragment)
                    .commitAllowingStateLoss();
        }
    }

    public void reloadUserInfo() {
        loadUserInfo();
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        dbRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String imageBase64 = snapshot.child("profilePhotoUrl").getValue(String.class);
                    Double rating = snapshot.child("rating").getValue(Double.class);

                    if (name != null) userNameText.setText(name);
                    if (phone != null) userPhoneText.setText(phone);
                    if (email != null) userEmailText.setText(email);
                    if (bio != null) userBioText.setText(bio);
                    if (rating != null && navUserRatingBar != null) navUserRatingBar.setRating(rating.floatValue());

                    long reportsCount = snapshot.child("reports").getChildrenCount();
                    if (tvReportWarning != null) {
                        if (reportsCount > 3) {
                            tvReportWarning.setText("Warning: Your account has been reported multiple times. Please follow community guidelines.");
                            tvReportWarning.setVisibility(View.VISIBLE);
                            // Fire a local broadcast so other components can react
                            Intent intent = new Intent("com.Sanket.roamly.REPORT_WARNING");
                            sendBroadcast(intent);
                        } else if (reportsCount > 0) {
                            tvReportWarning.setText("Some users have reported your account. Please behave responsibly.");
                            tvReportWarning.setVisibility(View.VISIBLE);
                        } else {
                            tvReportWarning.setVisibility(View.GONE);
                        }
                    }

                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        executorService.execute(() -> {
                            try {
                                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                runOnUiThread(() -> {
                                    if (bitmap != null) {
                                        userProfileImage.setImageBitmap(bitmap);
                                        userProfileImage.setClipToOutline(true);
                                    }
                                });
                            } catch (Exception e) {
                                runOnUiThread(() ->
                                        Toast.makeText(UserDashboardActivity.this, "Image load error", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDashboardActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerTripCompletedReceiver() {
        tripCompletedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String tripId = intent.getStringExtra("tripId");
                String tripLocation = intent.getStringExtra("tripLocation");
                String hostId = intent.getStringExtra("hostId");
                showFeedbackDialog(tripId, tripLocation, hostId);
            }
        };
        IntentFilter filter = new IntentFilter("com.Sanket.roamly.TRIP_COMPLETED");
        // For app-internal broadcast, use NOT_EXPORTED on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tripCompletedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(tripCompletedReceiver, filter);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerReportWarningReceiver() {
        reportWarningReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Show a visible warning when account has many reports
                new AlertDialog.Builder(UserDashboardActivity.this)
                        .setTitle("Account Warning")
                        .setMessage("Your account has been reported more than 3 times. If this continues, you may be blocked.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        };
        IntentFilter filter = new IntentFilter("com.Sanket.roamly.REPORT_WARNING");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(reportWarningReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(reportWarningReceiver, filter);
        }
    }

    private void showFeedbackDialog(String tripId, String tripLocation, String hostId) {
        if (tripId == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null);
        RatingBar rbTrip = dialogView.findViewById(R.id.rbTrip);
        RatingBar rbHost = dialogView.findViewById(R.id.rbHost);
        RatingBar rbFun = dialogView.findViewById(R.id.rbFun);
        RatingBar rbApp = dialogView.findViewById(R.id.rbApp);
        android.widget.EditText etSuggestion = dialogView.findViewById(R.id.etSuggestion);

        new AlertDialog.Builder(this)
                .setTitle(tripLocation != null ? "Feedback for " + tripLocation : "Trip Feedback")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float tripRating = rbTrip.getRating();
                    float hostRating = rbHost.getRating();
                    float funRating = rbFun.getRating();
                    float appRating = rbApp.getRating();
                    String suggestion = etSuggestion.getText().toString().trim();

                    String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                            : null;
                    if (uid == null) return;

                    DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback")
                            .child(tripId).child(uid);

                    feedbackRef.child("tripRating").setValue(tripRating);
                    feedbackRef.child("hostRating").setValue(hostRating);
                    feedbackRef.child("funRating").setValue(funRating);
                    feedbackRef.child("appRating").setValue(appRating);
                    if (!suggestion.isEmpty()) {
                        feedbackRef.child("suggestion").setValue(suggestion);
                    }
                    feedbackRef.child("timestamp").setValue(System.currentTimeMillis());

                    // mark feedback as given so dialog won't show again for this trip/user
                    markFeedbackShown(tripId);

                    // update host rating based on hostRating
                    if (hostId != null && hostRating > 0) {
                        updateHostRating(hostId, hostRating);
                    }
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void markFeedbackShown(String tripId) {
        SharedPreferences prefs = getSharedPreferences("trip_feedback_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("feedback_shown_" + tripId, true).apply();
    }

    private void updateHostRating(String hostId, float newRating) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(hostId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double currentRating = snapshot.child("rating").getValue(Double.class);
                Long ratingCount = snapshot.child("ratingCount").getValue(Long.class);

                if (currentRating == null) currentRating = 5.0; // default rating
                if (ratingCount == null) ratingCount = 0L;

                long newCount = ratingCount + 1;
                double updatedRating = (currentRating * ratingCount + newRating) / newCount;

                Map<String, Object> updates = new HashMap<>();
                updates.put("rating", updatedRating);
                updates.put("ratingCount", newCount);

                userRef.updateChildren(updates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (tripCompletedReceiver != null) {
            unregisterReceiver(tripCompletedReceiver);
        }
        if (reportWarningReceiver != null) {
            unregisterReceiver(reportWarningReceiver);
        }
    }
}
