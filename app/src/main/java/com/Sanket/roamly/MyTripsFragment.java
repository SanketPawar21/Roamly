package com.Sanket.roamly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyTripsFragment extends Fragment {

    private RecyclerView myTripsRecycler;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    private DatabaseReference tripRef;
    private List<MyTripModel> tripList = new ArrayList<>();
    private MyTripsAdapter adapter;
    private SharedPreferences feedbackPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        myTripsRecycler = view.findViewById(R.id.myTripsRecycler);
        searchView = view.findViewById(R.id.searchView);

        mAuth = FirebaseAuth.getInstance();
        tripRef = FirebaseDatabase.getInstance().getReference("Trips");
        feedbackPrefs = requireContext().getSharedPreferences("trip_feedback_prefs", Context.MODE_PRIVATE);

        myTripsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MyTripsAdapter(requireContext(), tripList);
        myTripsRecycler.setAdapter(adapter);

        loadTrips();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTrips(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTrips(newText);
                return true;
            }
        });

        return view;
    }

    private void loadTrips() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        tripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tripList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MyTripModel trip = snap.getValue(MyTripModel.class);
                    // Show trips where this user is host OR participant
                    if (trip != null && (trip.getUserId().equals(currentUserId)
                            || (trip.getParticipants() != null && trip.getParticipants().containsKey(currentUserId)))) {
                        tripList.add(trip);

                        // Show feedback prompt only if not yet submitted for this trip.
                        // We now mark it as shown only AFTER successful submission (in Activity).
                        if (isTripCompleted(trip) && !hasFeedbackBeenShown(trip.getTripId())) {
                            sendTripCompletedBroadcast(trip);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTrips(String query) {
        List<MyTripModel> filtered = new ArrayList<>();
        for (MyTripModel trip : tripList) {
            if (trip.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(trip);
            }
        }
        adapter.updateList(filtered);
    }

    private boolean isTripCompleted(MyTripModel trip) {
        String dateStr = trip.getDate();
        String periodStr = trip.getPeriod();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date startDate = sdf.parse(dateStr);
            if (startDate == null) return false;
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int days = 0;
            try {
                days = Integer.parseInt(periodStr);
            } catch (Exception ignored) {}
            cal.add(Calendar.DAY_OF_MONTH, days);
            Date endDate = cal.getTime();
            return endDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void sendTripCompletedBroadcast(MyTripModel trip) {
        Intent intent = new Intent("com.Sanket.roamly.TRIP_COMPLETED");
        intent.putExtra("tripId", trip.getTripId());
        intent.putExtra("tripLocation", trip.getLocation());
        intent.putExtra("hostId", trip.getUserId());
        requireContext().sendBroadcast(intent);
    }

    private boolean hasFeedbackBeenShown(String tripId) {
        return feedbackPrefs.getBoolean("feedback_shown_" + tripId, false);
    }

    private void markFeedbackShown(String tripId) {
        feedbackPrefs.edit().putBoolean("feedback_shown_" + tripId, true).apply();
    }
}
