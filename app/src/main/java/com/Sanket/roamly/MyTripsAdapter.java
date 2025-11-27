package com.Sanket.roamly;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;

public class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.TripViewHolder> {

    private Context context;
    private List<MyTripModel> tripList;
    // Colors to cycle for "My trips" cards
    private final int[] cardColors = new int[] {
            R.color.trip_card_1,
            R.color.trip_card_2,
            R.color.trip_card_3,
            R.color.trip_card_4,
            R.color.trip_card_5
    };
    private String currentUserId;
    private DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("Trips");
    private DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("SavedTrips");

    public MyTripsAdapter(Context context, List<MyTripModel> tripList) {
        this.context = context;
        this.tripList = tripList;
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        MyTripModel trip = tripList.get(position);

        // Set different background color per card based on position
        int colorResId = cardColors[position % cardColors.length];
        int colorInt = ContextCompat.getColor(context, colorResId);
        holder.cardView.setCardBackgroundColor(colorInt);

        holder.location.setText(trip.getLocation());
        holder.date.setText("Date: " + trip.getDate());
        holder.people.setText("People Needed: " + trip.getPeopleNeeded());
        holder.status.setText(trip.getStatus());
        holder.role.setText(trip.getUserId().equals(currentUserId) ? "Host" : "Participant");

        holder.viewDetailsBtn.setOnClickListener(v -> {
            TripDetailsFragment fragment = new TripDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("tripId", trip.getTripId());
            fragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.userFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        if (trip.getUserId().equals(currentUserId)) {
            // Host: can delete the trip
            holder.leaveTripBtn.setVisibility(View.VISIBLE);
            holder.leaveTripBtn.setText("Delete Trip");
            holder.leaveTripBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Trip")
                        .setMessage("Do you want to delete this trip? All participants will be removed and the trip will no longer be visible.")
                        .setPositiveButton("Yes", (dialog, which) -> deleteTrip(trip, holder.getAdapterPosition()))
                        .setNegativeButton("No", null)
                        .show();
            });
        } else {
            // Participant: can leave the trip
            holder.leaveTripBtn.setVisibility(View.VISIBLE);
            holder.leaveTripBtn.setText("Leave Trip");
            holder.leaveTripBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Leave Trip")
                        .setMessage("Do you want to leave this trip? You will be removed from the chat group as well.")
                        .setPositiveButton("Yes", (dialog, which) -> leaveTrip(trip, holder.getAdapterPosition()))
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        holder.saveTripBtn.setOnClickListener(v -> saveTrip(trip));

    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public void updateList(List<MyTripModel> filtered) {
        this.tripList = filtered;
        notifyDataSetChanged();
    }

    private void saveTrip(MyTripModel trip) {
        String uid = currentUserId;
        savedRef.child(uid).child(trip.getTripId()).setValue(true)
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, "Trip saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void leaveTrip(MyTripModel trip, int position) {
        String uid = currentUserId;
        DatabaseReference tripNode = tripRef.child(trip.getTripId());

        int seats = 0;
        try {
            seats = Integer.parseInt(trip.getPeopleNeeded());
        } catch (Exception e) {
            seats = 0;
        }
        int updatedSeats = seats + 1;

        tripNode.child("peopleNeeded").setValue(String.valueOf(updatedSeats));
        tripNode.child("participants").child(uid).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Left trip successfully", Toast.LENGTH_SHORT).show();
                    if (position != RecyclerView.NO_POSITION && position < tripList.size()) {
                        tripList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteTrip(MyTripModel trip, int position) {
        DatabaseReference tripNode = tripRef.child(trip.getTripId());
        tripNode.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Trip deleted successfully", Toast.LENGTH_SHORT).show();
                    if (position != RecyclerView.NO_POSITION && position < tripList.size()) {
                        tripList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView location, date, people, status, role;
        Button viewDetailsBtn, leaveTripBtn, saveTripBtn;
        CardView cardView;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRootMyTrip);
            location = itemView.findViewById(R.id.tvLocation);
            date = itemView.findViewById(R.id.tvDate);
            people = itemView.findViewById(R.id.tvPeople);
            status = itemView.findViewById(R.id.tvStatus);
            role = itemView.findViewById(R.id.tvRole);
            viewDetailsBtn = itemView.findViewById(R.id.viewDetailsBtn);
            saveTripBtn = itemView.findViewById(R.id.btnSaveTripMy);
            leaveTripBtn = itemView.findViewById(R.id.btnLeaveTrip);
        }
    }
}
