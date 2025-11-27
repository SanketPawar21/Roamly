package com.Sanket.roamly;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.*;

public class TripDetailsFragment extends Fragment {

    private TextView tvLocation, tvDate, tvPeriod, tvPreference, tvPeopleNeeded, tvThings, tvStatus;
    private TextView tvHostName, tvHostEmail;
    private String hostId;
    private RecyclerView participantsRecycler;

    private DatabaseReference tripRef, usersRef;
    private String tripId;
    private ParticipantsAdapter adapter;
    private List<UserModel> participantsList = new ArrayList<>();

    public TripDetailsFragment() {}

    public TripDetailsFragment(String tripId) {
        this.tripId = tripId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        tvLocation = view.findViewById(R.id.tvLocation);
        tvDate = view.findViewById(R.id.tvDate);
        tvPeriod = view.findViewById(R.id.tvPeriod);
        tvPreference = view.findViewById(R.id.tvPreference);
        tvPeopleNeeded = view.findViewById(R.id.tvPeopleNeeded);
        tvThings = view.findViewById(R.id.tvThings);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvHostName = view.findViewById(R.id.tvHostName);
        tvHostEmail = view.findViewById(R.id.tvHostEmail);
        participantsRecycler = view.findViewById(R.id.participantsRecycler);

        participantsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ParticipantsAdapter(requireContext(), participantsList, participantIds);
        participantsRecycler.setAdapter(adapter);

        tripRef = FirebaseDatabase.getInstance().getReference("Trips");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        if (getArguments() != null)
            tripId = getArguments().getString("tripId");

        loadTripDetails();

        return view;
    }

    private void loadTripDetails() {
        tripRef.child(tripId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyTripModel trip = snapshot.getValue(MyTripModel.class);
                if (trip != null) {
                    tvLocation.setText("Location: " + trip.getLocation());
                    tvDate.setText("Date: " + trip.getDate());
                    tvPeriod.setText("Period: " + trip.getPeriod() + " Days");
                    tvPreference.setText("Preference: " + trip.getPreference());
                    tvPeopleNeeded.setText("People Needed: " + trip.getPeopleNeeded());
                    tvThings.setText("Things: " + trip.getThingsToCarry());
                    tvStatus.setText("Status: " + trip.getStatus());

                    loadHostDetails(trip.getUserId());
                    loadParticipants(trip.getParticipants());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadHostDetails(String hostId) {
        this.hostId = hostId;
        usersRef.child(hostId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel host = snapshot.getValue(UserModel.class);
                if (host != null) {
                    tvHostName.setText("Host Name: " + host.getName());
                    tvHostEmail.setText("Host Email: " + host.getEmail());

                    View.OnClickListener listener = v -> openProfile(hostId);
                    tvHostName.setOnClickListener(listener);
                    tvHostEmail.setOnClickListener(listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private List<String> participantIds = new ArrayList<>();

    private void loadParticipants(Map<String, Object> participantsMap) {
        if (participantsMap == null || participantsMap.isEmpty()) return;

        participantsList.clear();
        participantIds.clear();

        for (String uid : participantsMap.keySet()) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    if (user != null) {
                        participantsList.add(user);
                        participantIds.add(uid);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void openProfile(String userId) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.userFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
