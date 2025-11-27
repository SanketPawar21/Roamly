package com.Sanket.roamly;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ViewProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private String userId;
    private DatabaseReference usersRef;

    private ShapeableImageView imageProfileView;
    private TextView tvProfileName, tvProfileEmail;
    private MaterialButton btnReportUser;

    public ViewProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarProfile);
        imageProfileView = view.findViewById(R.id.imageProfileView);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        btnReportUser = view.findViewById(R.id.btnReportUser);

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        loadUserProfile();
        setupReportButton();

        return view;
    }

    private void loadUserProfile() {
        if (userId == null) return;

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String imageBase64 = snapshot.child("profilePhotoUrl").getValue(String.class);

                if (name != null) tvProfileName.setText(name);
                if (email != null) tvProfileEmail.setText(email);

                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) {
                            imageProfileView.setImageBitmap(bitmap);
                            imageProfileView.setClipToOutline(true);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupReportButton() {
        btnReportUser.setOnClickListener(v -> showReportDialog());
    }

    private void showReportDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("Reason for reporting (required)");
        input.setMinLines(2);
        input.setMaxLines(4);

        new AlertDialog.Builder(requireContext())
                .setTitle("Report user")
                .setMessage("Please describe why you want to report this user.")
                .setView(input)
                .setPositiveButton("Report", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(requireContext(), "Please provide a reason", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reportUser(reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reportUser(String reason) {
        if (userId == null) return;

        String reporterId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (reporterId == null) {
            Toast.makeText(requireContext(), "You must be logged in to report", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reporterId.equals(userId)) {
            Toast.makeText(requireContext(), "You can't report yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = usersRef.child(userId);
        String reportId = userRef.child("reports").push().getKey();
        if (reportId == null) return;

        long now = System.currentTimeMillis();

        DatabaseReference reportRef = userRef.child("reports").child(reportId);
        reportRef.child("by").setValue(reporterId);
        reportRef.child("timestamp").setValue(now);
        reportRef.child("reason").setValue(reason)
                .addOnSuccessListener(unused -> adjustRatingAndBlock(userRef, now))
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to report: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void adjustRatingAndBlock(DatabaseReference userRef, long now) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double rating = snapshot.child("rating").getValue(Double.class);
                if (rating == null) rating = 5.0; // default rating

                // Decrease rating slightly per report
                rating -= 0.5;
                if (rating < 0) rating = 0.0;

                // Count reports in the last month
                long monthAgo = now - 30L * 24 * 60 * 60 * 1000;
                int monthlyReports = 0;
                DataSnapshot reportsSnap = snapshot.child("reports");
                for (DataSnapshot rep : reportsSnap.getChildren()) {
                    Long ts = rep.child("timestamp").getValue(Long.class);
                    if (ts != null && ts >= monthAgo) monthlyReports++;
                }

                boolean block = rating < 1.0 && monthlyReports >= 4;

                userRef.child("rating").setValue(rating);
                if (block) {
                    userRef.child("blocked").setValue(true);
                    Toast.makeText(requireContext(), "User has been blocked due to multiple reports", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "User reported", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
