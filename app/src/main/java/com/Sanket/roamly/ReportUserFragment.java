package com.Sanket.roamly;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ReportUserFragment extends Fragment {

    private EditText etTargetEmail, etTargetName, etReason;
    private MaterialButton btnSubmitReport;

    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report_user, container, false);

        etTargetEmail = v.findViewById(R.id.etTargetEmail);
        etTargetName = v.findViewById(R.id.etTargetName);
        etReason = v.findViewById(R.id.etReason);
        btnSubmitReport = v.findViewById(R.id.btnSubmitReport);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        btnSubmitReport.setOnClickListener(view -> submitReport());

        return v;
    }

    private void submitReport() {
        String email = etTargetEmail.getText().toString().trim();
        String name = etTargetName.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(reason)) {
            Toast.makeText(requireContext(), "Enter email and reason", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.equalsIgnoreCase(currentUser.getEmail())) {
            Toast.makeText(requireContext(), "You can't report yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find user by email
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.hasChildren()) {
                            Toast.makeText(requireContext(), "No user found with this email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String targetUid = snap.getKey();
                            String dbName = snap.child("name").getValue(String.class);

                            // Optional name check
                            if (!TextUtils.isEmpty(name) && dbName != null && !name.equalsIgnoreCase(dbName)) {
                                Toast.makeText(requireContext(), "Name does not match this email (continuing)", Toast.LENGTH_SHORT).show();
                            }

                            writeReportAndAdjust(targetUid, reason, currentUser.getUid());
                            break; // only first match
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void writeReportAndAdjust(String targetUid, String reason, String reporterUid) {
        if (targetUid == null) return;

        DatabaseReference userRef = usersRef.child(targetUid);
        String reportId = userRef.child("reports").push().getKey();
        if (reportId == null) return;

        long now = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("by", reporterUid);
        data.put("timestamp", now);
        data.put("reason", reason);

        userRef.child("reports").child(reportId).setValue(data)
                .addOnSuccessListener(unused -> adjustRatingAndWarn(userRef))
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to report: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void adjustRatingAndWarn(DatabaseReference userRef) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double rating = snapshot.child("rating").getValue(Double.class);
                if (rating == null) rating = 5.0;

                // Decrease rating slightly per report
                rating -= 0.5;
                if (rating < 0) rating = 0.0;

                long reportsCount = snapshot.child("reports").getChildrenCount();

                userRef.child("rating").setValue(rating);

                if (reportsCount > 3) {
                    // Mark warning flag; UserDashboardActivity will show a visible warning using receiver
                    userRef.child("reportWarning").setValue(true);
                }

                Toast.makeText(requireContext(), "User reported", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
