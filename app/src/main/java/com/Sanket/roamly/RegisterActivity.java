package com.Sanket.roamly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Sanket.roamly.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Dropdown for gender
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        binding.roleSpinner.setAdapter(adapter);

        binding.registerBtn.setOnClickListener(v -> registerUser());
        binding.backToLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = binding.firstNameEt.getText().toString().trim();
        String phone = binding.phoneEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String gender = binding.roleSpinner.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("phone", phone);
                        userMap.put("email", email);
                        userMap.put("gender", gender);
                        userMap.put("role", "User");

                        usersRef.child(uid).setValue(userMap)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Database Error: " + saveTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
