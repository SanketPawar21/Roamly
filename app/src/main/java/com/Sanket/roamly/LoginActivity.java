package com.Sanket.roamly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // use your xml filename

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        findViewById(R.id.loginBtn).setOnClickListener(v -> loginWithEmail());
        findViewById(R.id.registerText).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // ✅ Google Sign-In Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signInWithGoogle());
    }

    private void loginWithEmail() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save basic user info if not already in database
                            usersRef.child(user.getUid()).get().addOnCompleteListener(dataTask -> {
                                if (!dataTask.getResult().exists()) {
                                    UserProfile profile = new UserProfile(
                                            user.getDisplayName(),
                                            user.getEmail(),
                                            user.getPhoneNumber(),
                                            "User" // role
                                    );
                                    usersRef.child(user.getUid()).setValue(profile);
                                }
                                openUserDashboard();
                            });
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        usersRef.child(uid).child("role").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getValue(String.class);
                if ("Admin".equalsIgnoreCase(role)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                } else {
                    openUserDashboard();
                }
                finish();
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUserDashboard() {
        startActivity(new Intent(this, UserDashboardActivity.class));
        finish();
    }

    // Optional: Skip login if already signed in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser.getUid());
        }
    }

    // Helper class for saving user info (optional)
    public static class UserProfile {
        public String name, email, phone, role;

        public UserProfile() {} // empty constructor for Firebase

        public UserProfile(String name, String email, String phone, String role) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
        }
    }
}
