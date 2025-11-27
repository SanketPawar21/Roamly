package com.Sanket.roamly;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class U_HomeFragment extends Fragment {

    private RecyclerView recyclerTrips;
    private SearchView searchView;
    private TripAdapter adapter;
    private List<TripModel> tripList = new ArrayList<>();
    private DatabaseReference tripRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_u__home, container, false);

        recyclerTrips = v.findViewById(R.id.recyclerTrips);
        searchView = v.findViewById(R.id.searchView);

        recyclerTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripAdapter(getContext(), tripList);
        recyclerTrips.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        tripRef = FirebaseDatabase.getInstance().getReference("Trips");

        loadTrips();

        // 🔍 Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        return v;
    }

    private void loadTrips() {
        tripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tripList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    TripModel trip = ds.getValue(TripModel.class);
                    if (trip == null || trip.userId == null) continue;

                    // ❌ Hide trips where current user is host
                    if (currentUser != null && trip.userId.equals(currentUser.getUid())) continue;

                    // ✅ Show all other upcoming trips (you may be participant or not)
                    if (isDatePassed(trip.date)) continue;

                    tripList.add(trip);
                }

                adapter.updateList(tripList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 Safely convert string → int
    private int parseSeats(String seatsStr) {
        try {
            return Integer.parseInt(seatsStr);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isDatePassed(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date tripDate = sdf.parse(date);
            return tripDate != null && tripDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }
}
