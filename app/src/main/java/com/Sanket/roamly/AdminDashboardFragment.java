package com.Sanket.roamly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvActiveTrips, tvPendingReports, tvTripsThisWeek;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnTabUsers, btnTabTrips, btnTabReports;
    private FrameLayout listContainer;

    private DatabaseReference usersRef, tripsRef, reportsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveTrips = view.findViewById(R.id.tvActiveTrips);
        tvPendingReports = view.findViewById(R.id.tvPendingReports);
        tvTripsThisWeek = view.findViewById(R.id.tvTripsThisWeek);
        toggleGroup = view.findViewById(R.id.toggleGroup);
        btnTabUsers = view.findViewById(R.id.btnTabUsers);
        btnTabTrips = view.findViewById(R.id.btnTabTrips);
        btnTabReports = view.findViewById(R.id.btnTabReports);
        listContainer = view.findViewById(R.id.adminListContainer);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        tripsRef = FirebaseDatabase.getInstance().getReference("Trips");
        reportsRef = FirebaseDatabase.getInstance().getReference("Reports");

        loadSummaryCounts();

        // Default tab: Users (reuse AdminUsersFragment)
        toggleGroup.check(btnTabUsers.getId());
        showUsersFragment();

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == btnTabUsers.getId()) {
                showUsersFragment();
            } else if (checkedId == btnTabTrips.getId()) {
                showTripsFragment();
            } else if (checkedId == btnTabReports.getId()) {
                showReportsPlaceholder();
            }
        });

        return view;
    }

    private void loadSummaryCounts() {
        // Total users
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvTotalUsers.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Trips summary
        tripsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long active = 0;
                long thisWeek = 0;
                long now = System.currentTimeMillis();
                long weekMillis = 7L * 24 * 60 * 60 * 1000;

                for (DataSnapshot child : snapshot.getChildren()) {
                    TripModel trip = child.getValue(TripModel.class);
                    if (trip == null) continue;

                    // count active (status not completed/cancelled)
                    if (trip.status == null || (!"completed".equalsIgnoreCase(trip.status) && !"cancelled".equalsIgnoreCase(trip.status))) {
                        active++;
                    }

                    if (trip.timestamp > now - weekMillis) {
                        thisWeek++;
                    }
                }

                tvActiveTrips.setText(String.valueOf(active));
                tvTripsThisWeek.setText(String.valueOf(thisWeek));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Pending reports (Reports where resolved=false)
        reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long pending = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean resolved = child.child("resolved").getValue(Boolean.class);
                    if (resolved == null || !resolved) {
                        pending++;
                    }
                }
                tvPendingReports.setText(String.valueOf(pending));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showUsersFragment() {
        // Reuse existing AdminUsersFragment inside the dashboard list container
        replaceInnerFragment(new AdminUsersFragment());
    }

    private void showTripsFragment() {
        // For now reuse MyTripsFragment as a placeholder; later you can create AdminTripsFragment
        replaceInnerFragment(new MyTripsFragment());
    }

    private void showReportsPlaceholder() {
        // Placeholder: you can later implement AdminReportsFragment
        replaceInnerFragment(new ReportUserFragment());
    }

    private void replaceInnerFragment(Fragment fragment) {
        if (getChildFragmentManager() == null) return;
        FragmentTransaction tx = getChildFragmentManager().beginTransaction();
        tx.replace(R.id.adminListContainer, fragment);
        tx.commitAllowingStateLoss();
    }
}
