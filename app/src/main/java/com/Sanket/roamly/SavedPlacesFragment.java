package com.Sanket.roamly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedPlacesFragment extends Fragment {

    private RecyclerView recyclerSavedTrips;
    private TripAdapter adapter;
    private List<TripModel> savedTrips = new ArrayList<>();

    private DatabaseReference savedRef;
    private DatabaseReference tripsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_places, container, false);

        recyclerSavedTrips = view.findViewById(R.id.recyclerSavedTrips);
        recyclerSavedTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Use TripAdapter in "saved list" mode so button text is "Remove" and removal is supported
        adapter = new TripAdapter(requireContext(), savedTrips, true);
        recyclerSavedTrips.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) {
            Toast.makeText(requireContext(), "Please log in to see saved trips", Toast.LENGTH_SHORT).show();
            return view;
        }

        savedRef = FirebaseDatabase.getInstance().getReference("SavedTrips").child(uid);
        tripsRef = FirebaseDatabase.getInstance().getReference("Trips");

        loadSavedTrips();

        return view;
    }

    private void loadSavedTrips() {
        savedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedTrips.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String tripId = snap.getKey();
                    if (tripId == null) continue;

                    tripsRef.child(tripId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot tripSnap) {
                            TripModel trip = tripSnap.getValue(TripModel.class);
                            if (trip != null) {
                                savedTrips.add(trip);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load saved trips", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
