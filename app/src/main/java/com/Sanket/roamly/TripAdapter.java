package com.Sanket.roamly;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.*;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private Context context;
    private List<TripModel> tripList, fullList;
    // Colors to cycle through for each card
    private final int[] cardColors = new int[] {
            R.color.trip_card_1,
            R.color.trip_card_2,
            R.color.trip_card_3,
            R.color.trip_card_4,
            R.color.trip_card_5
    };
    private DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("Trips");
    private DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("SavedTrips");
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
    private boolean isSavedList;

    // NEW: store current user gender
    private String currentUserGender;
    private boolean isGenderLoaded = false;

    public TripAdapter(Context context, List<TripModel> tripList) {
        this(context, tripList, false);
    }

    public TripAdapter(Context context, List<TripModel> tripList, boolean isSavedList) {
        this.context = context;
        this.tripList = tripList;
        this.fullList = new ArrayList<>(tripList);
        this.isSavedList = isSavedList;

        // Load current user gender once
        loadCurrentUserGender();
    }

    private void loadCurrentUserGender() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserGender = snapshot.child("gender").getValue(String.class);
                isGenderLoaded = true;
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isGenderLoaded = true; // avoid endless "Checking..."
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_trip, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        TripModel t = tripList.get(pos);

        // Set different background color per card based on position
        int colorResId = cardColors[pos % cardColors.length];
        int colorInt = ContextCompat.getColor(context, colorResId);
        h.cardView.setCardBackgroundColor(colorInt);

        // Convert peopleNeeded string -> int
        int seats = 0;
        try {
            seats = Integer.parseInt(t.peopleNeeded);
        } catch (Exception e) {
            seats = 0;
        }

        // Host name: use cached hostName if available, otherwise fallback to Users/{userId}
        if (t.hostName != null && !t.hostName.isEmpty()) {
            h.tvHostName.setText("Host: " + t.hostName);
        } else if (t.userId != null) {
            h.tvHostName.setText("Host: loading...");
            usersRef.child(t.userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String finalName = name != null && !name.isEmpty()
                            ? name
                            : (email != null ? email : "Unknown");
                    t.hostName = finalName;
                    h.tvHostName.setText("Host: " + finalName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        } else {
            h.tvHostName.setText("Host: Unknown");
        }

        h.tvLocation.setText("Location: " + t.location);
        h.tvDate.setText("Date: " + t.date);
        h.tvPeriod.setText("Period: " + t.period + " days");
        h.tvPreference.setText("Preference: " + t.preference);
        h.tvSeatLeft.setText("Seats Left: " + seats);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- FEMALE-ONLY LOGIC START ---
        // Change "Female only" to match your actual female-only value in TripModel.preference
        boolean isTripFemaleOnly = t.preference != null
                && t.preference.equalsIgnoreCase("Female only");

        boolean isUserFemale = currentUserGender != null
                && currentUserGender.equalsIgnoreCase("female");

        // Clear old click listener to avoid recycling issues
        h.btnJoinTrip.setOnClickListener(null);

        if (!isGenderLoaded) {
            // Gender not loaded yet - temporarily disable
            h.btnJoinTrip.setEnabled(false);
            h.btnJoinTrip.setText("Checking...");
        } else if (isTripFemaleOnly && !isUserFemale) {
            // Trip is only for females and user is NOT female
            h.btnJoinTrip.setEnabled(false);
            h.btnJoinTrip.setText("Female only");
        } else {
            // Gender allowed -> normal seat/joined logic
            if (seats <= 0) {
                h.btnJoinTrip.setEnabled(false);
                h.btnJoinTrip.setText("Full");
            } else if (t.participants != null && t.participants.containsKey(uid)) {
                h.btnJoinTrip.setEnabled(false);
                h.btnJoinTrip.setText("Joined");
            } else {
                h.btnJoinTrip.setEnabled(true);
                h.btnJoinTrip.setText("Join");
                int finalSeats = seats;
                h.btnJoinTrip.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Join Trip")
                            .setMessage("Do you want to join this trip?")
                            .setPositiveButton("Yes", (dialog, which) -> joinTrip(t, uid, h, finalSeats))
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        }
        // --- FEMALE-ONLY LOGIC END ---

        // Save / Remove behavior depending on whether this adapter is used for Saved Places
        if (isSavedList) {
            h.btnSaveTrip.setText("clear");
            h.btnSaveTrip.setOnClickListener(v -> removeSavedTrip(t, uid, h.getAdapterPosition()));
        } else {
            h.btnSaveTrip.setText("Save");
            h.btnSaveTrip.setOnClickListener(v -> saveTrip(t, uid));
        }

        h.btnViewDetails.setOnClickListener(v -> {
            TripDetailsFragment fragment = new TripDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("tripId", t.getTripId());
            fragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.userFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void saveTrip(TripModel t, String uid) {
        if (uid == null) return;
        savedRef.child(uid).child(t.getTripId()).setValue(true)
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, "Trip saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeSavedTrip(TripModel t, String uid, int position) {
        if (uid == null) return;

        savedRef.child(uid).child(t.getTripId()).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                    if (position != RecyclerView.NO_POSITION && position < tripList.size()) {
                        tripList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void joinTrip(TripModel t, String uid, ViewHolder h, int seats) {

        if (seats <= 0) {
            Toast.makeText(context, "Trip is full!", Toast.LENGTH_SHORT).show();
            h.btnJoinTrip.setEnabled(false);
            h.btnJoinTrip.setText("Full");
            return;
        }

        int updatedSeats = seats - 1;
        DatabaseReference tripNode = tripRef.child(t.tripId);

        // Save as String (Firebase stores as String)
        tripNode.child("peopleNeeded").setValue(String.valueOf(updatedSeats));

        // Add participant
        Map<String, Object> participant = new HashMap<>();
        participant.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        participant.put("joinedAt", String.valueOf(System.currentTimeMillis())); // FIX: String

        tripNode.child("participants").child(uid).setValue(participant)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show();
                    h.btnJoinTrip.setEnabled(false);
                    h.btnJoinTrip.setText("Joined");
                    h.tvSeatLeft.setText("Seats Left: " + updatedSeats);

                    // Update local model
                    t.peopleNeeded = String.valueOf(updatedSeats);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public void updateList(List<TripModel> newList) {
        this.tripList = newList;
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        text = text.toLowerCase(Locale.getDefault());
        List<TripModel> filtered = new ArrayList<>();
        for (TripModel t : fullList) {
            if (t.location.toLowerCase().contains(text) ||
                    (t.hostName != null && t.hostName.toLowerCase().contains(text))) {
                filtered.add(t);
            }
        }
        tripList = filtered;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHostName, tvLocation, tvDate, tvPeriod, tvPreference, tvSeatLeft;
        Button btnViewDetails, btnJoinTrip, btnSaveTrip;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            cardView = v.findViewById(R.id.cardRoot);
            tvHostName = v.findViewById(R.id.tvHostName);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvDate = v.findViewById(R.id.tvDate);
            tvPeriod = v.findViewById(R.id.tvPeriod);
            tvPreference = v.findViewById(R.id.tvPreference);
            tvSeatLeft = v.findViewById(R.id.tvSeatLeft);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
            btnSaveTrip = v.findViewById(R.id.btnSaveTrip);
            btnJoinTrip = v.findViewById(R.id.btnJoinTrip);
        }
    }
}