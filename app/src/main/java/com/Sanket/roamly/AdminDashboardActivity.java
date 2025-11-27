package com.Sanket.roamly;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Default fragment for admin: show user oversight panel
        if (savedInstanceState == null) {
            loadFragment(new AdminUsersFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Admin home: manage users, see ratings / reports, block fake profiles
                selected = new AdminUsersFragment();
            } else if (id == R.id.nav_my_trips) {
                // Reuse MyTripsFragment so admin can still see own trips; can be replaced later with global trips view
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

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.Create_trips) {
                loadFragment(new CreateTripsFragment());
            } else if (id == R.id.nav_saved_places) {
                loadFragment(new SavedPlacesFragment());
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            } else {
                Toast.makeText(this, "Admin feature coming soon", Toast.LENGTH_SHORT).show();
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
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commitAllowingStateLoss();
        }
    }
}
