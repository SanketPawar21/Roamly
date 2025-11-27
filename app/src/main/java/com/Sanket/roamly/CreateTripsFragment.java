package com.Sanket.roamly;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateTripsFragment extends Fragment {

    private EditText locationEt, dateEt, periodEt, peopleEt, thingsEt;
    private Spinner preferenceSpinner;
    private MaterialButton createTripBtn;
    private ViewPager2 bannerViewPager;
    private LinearLayout dotsLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference tripRef;
    private int[] bannerImages = {
            R.drawable.t2,
            R.drawable.t3,
            R.drawable.timage,
            R.drawable.tt,
            R.drawable.ttt,
            R.drawable.banner
    };

    private int currentPage = 0;
    private final Handler sliderHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_trips, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        tripRef = FirebaseDatabase.getInstance().getReference("Trips");

        // Initialize views
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        dotsLayout = view.findViewById(R.id.dotsLayout);
        locationEt = view.findViewById(R.id.locationEt);
        dateEt = view.findViewById(R.id.dateEt);
        periodEt = view.findViewById(R.id.periodEt);
        peopleEt = view.findViewById(R.id.peopleEt);
        preferenceSpinner = view.findViewById(R.id.preferenceSpinner);
        thingsEt = view.findViewById(R.id.thingsEt);
        createTripBtn = view.findViewById(R.id.createTripBtn);

        setupBannerSlider();
        setupPreferenceSpinner();

        dateEt.setOnClickListener(v -> showDatePicker());

        createTripBtn.setOnClickListener(v -> saveTripToFirebase());

        return view;
    }

    // 🔹 Banner Slider setup
    private void setupBannerSlider() {
        BannerAdapter adapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(adapter);
        setupDots();

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateDots(position);
            }
        });

        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerViewPager.getAdapter() != null) {
                currentPage = (currentPage + 1) % bannerImages.length;
                bannerViewPager.setCurrentItem(currentPage, true);
                sliderHandler.postDelayed(this, 3000);
            }
        }
    };

    private void setupDots() {
        ImageView[] dots = new ImageView[bannerImages.length];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(requireContext());
            dots[i].setImageResource(i == 0 ? R.drawable.active_dot : R.drawable.inactive_dot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            params.setMargins(6, 0, 6, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsLayout.getChildAt(i);
            dot.setImageResource(i == position ? R.drawable.active_dot : R.drawable.inactive_dot);
        }
    }

    // 🔹 Spinner setup
    private void setupPreferenceSpinner() {
        List<String> preferences = Arrays.asList("Male Only", "Female Only", "Anyone Can Join");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, preferences);
        preferenceSpinner.setAdapter(adapter);
    }

    // 🔹 Date picker
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateEt.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // 🔹 Save Trip to Firebase
    private void saveTripToFirebase() {
        String location = locationEt.getText().toString().trim();
        String date = dateEt.getText().toString().trim();
        String period = periodEt.getText().toString().trim();
        String people = peopleEt.getText().toString().trim();
        String preference = preferenceSpinner.getSelectedItem().toString();
        String things = thingsEt.getText().toString().trim();

        if (TextUtils.isEmpty(location) || TextUtils.isEmpty(date) || TextUtils.isEmpty(period) || TextUtils.isEmpty(people)) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String tripId = tripRef.push().getKey();

        // Host name for display in U_HomeFragment (fallback to email if no profile name)
        String hostName = null;
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().getDisplayName() != null && !mAuth.getCurrentUser().getDisplayName().isEmpty()) {
                hostName = mAuth.getCurrentUser().getDisplayName();
            } else {
                hostName = mAuth.getCurrentUser().getEmail();
            }
        }

        Map<String, Object> tripData = new HashMap<>();
        tripData.put("tripId", tripId);
        tripData.put("userId", userId);
        tripData.put("hostName", hostName);
        tripData.put("location", location);
        tripData.put("date", date);
        tripData.put("period", period);
        tripData.put("peopleNeeded", people);
        tripData.put("preference", preference);
        tripData.put("thingsToCarry", things);
        tripData.put("status", "Open");
        tripData.put("timestamp", System.currentTimeMillis());

        if (tripId != null) {
            tripRef.child(tripId).setValue(tripData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "Trip created successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // 🔹 Clear fields after saving
    private void clearFields() {
        locationEt.setText("");
        dateEt.setText("");
        periodEt.setText("");
        peopleEt.setText("");
        thingsEt.setText("");
        preferenceSpinner.setSelection(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
