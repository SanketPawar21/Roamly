package com.Sanket.roamly;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class U_ProfileFragment extends Fragment {

    private ShapeableImageView imgProfile;
    private RatingBar userRatingBar;
    private EditText etUserName, etUserPhone, etUserBio, etCurrentPassword, etNewPassword;
    private TextView tvUserEmail;
    private MaterialButton btnChangePhoto, btnUpdateProfile, btnChangePassword;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_u__profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return view;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        imgProfile = view.findViewById(R.id.imgProfile);
        userRatingBar = view.findViewById(R.id.userRatingBar);
        etUserName = view.findViewById(R.id.etUserName);
        etUserPhone = view.findViewById(R.id.etUserPhone);
        etUserBio = view.findViewById(R.id.etUserBio);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        setupImagePicker();
        loadUserProfile();

        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());

        return view;
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                            imgProfile.setImageBitmap(bitmap);
                            saveProfileImage(bitmap);
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Image error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveProfileImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        String base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);
        userRef.child("profilePhotoUrl").setValue(base64)
                .addOnSuccessListener(unused -> {
                    if (getActivity() instanceof UserDashboardActivity) {
                        ((UserDashboardActivity) getActivity()).reloadUserInfo();
                    }
                });
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String bio = snapshot.child("bio").getValue(String.class);
                String imageBase64 = snapshot.child("profilePhotoUrl").getValue(String.class);
                Double rating = snapshot.child("rating").getValue(Double.class);

                if (name != null) etUserName.setText(name);
                if (phone != null) etUserPhone.setText(phone);
                if (email != null) tvUserEmail.setText(email);
                if (bio != null) etUserBio.setText(bio);
                if (rating != null) userRatingBar.setRating(rating.floatValue());

                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] decoded = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bitmap != null) {
                            imgProfile.setImageBitmap(bitmap);
                            imgProfile.setClipToOutline(true);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateProfile() {
        String name = etUserName.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String bio = etUserBio.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("bio", bio);

        userRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    // refresh nav drawer header
                    if (getActivity() instanceof UserDashboardActivity) {
                        ((UserDashboardActivity) getActivity()).reloadUserInfo();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(requireContext(), "Enter current and new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            Toast.makeText(requireContext(), "No email associated with account", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(unused -> firebaseUser.updatePassword(newPassword)
                        .addOnSuccessListener(u ->
                                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Current password incorrect", Toast.LENGTH_SHORT).show());
    }
}
