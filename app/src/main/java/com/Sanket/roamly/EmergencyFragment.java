package com.Sanket.roamly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyFragment extends Fragment {

    private static final int REQ_PERMISSIONS_SOS = 2001;

    private MaterialButton btnSos, btnAddContact, btnWomenHelpline, btnPoliceHelpline;
    private RecyclerView recyclerEmergencyContacts;
    private EmergencyContactsAdapter contactsAdapter;
    private List<EmergencyContact> contactList = new ArrayList<>();

    private DatabaseReference contactsRef;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        btnSos = view.findViewById(R.id.btnSos);
        btnAddContact = view.findViewById(R.id.btnAddContact);
        btnWomenHelpline = view.findViewById(R.id.btnWomenHelpline);
        btnPoliceHelpline = view.findViewById(R.id.btnPoliceHelpline);
        recyclerEmergencyContacts = view.findViewById(R.id.recyclerEmergencyContacts);

        recyclerEmergencyContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        contactsAdapter = new EmergencyContactsAdapter(contactList, this::removeContact);
        recyclerEmergencyContacts.setAdapter(contactsAdapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid != null) {
            contactsRef = FirebaseDatabase.getInstance().getReference("EmergencyContacts").child(uid);
            loadContactsFromFirebase();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        btnAddContact.setOnClickListener(v -> showAddContactDialog());
        btnWomenHelpline.setOnClickListener(v -> dialNumber("1091"));
        btnPoliceHelpline.setOnClickListener(v -> dialNumber("100"));
        btnSos.setOnClickListener(v -> sendSosAlert());

        return view;
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_emergency_contact, null);
        EditText etName = dialogView.findViewById(R.id.etContactName);
        EditText etPhone = dialogView.findViewById(R.id.etContactPhone);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(phone)) {
                        Toast.makeText(requireContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(name)) {
                        name = "Contact";
                    }

                    EmergencyContact c = new EmergencyContact(name, phone);
                    contactList.add(c);
                    contactsAdapter.notifyItemInserted(contactList.size() - 1);

                    if (contactsRef != null) {
                        String key = contactsRef.push().getKey();
                        if (key != null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("phone", phone);
                            contactsRef.child(key).setValue(map);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadContactsFromFirebase() {
        if (contactsRef == null) return;

        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    String phone = child.child("phone").getValue(String.class);
                    if (!TextUtils.isEmpty(phone)) {
                        contactList.add(new EmergencyContact(name, phone));
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void removeContact(int position) {
        if (position < 0 || position >= contactList.size()) return;
        contactList.remove(position);
        contactsAdapter.notifyItemRemoved(position);
        // NOTE: for full sync with Firebase, also remove there when you store IDs.
    }

    private void dialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void sendSosAlert() {
        if (contactList.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one emergency contact", Toast.LENGTH_SHORT).show();
            return;
        }

        // Request permissions if missing
        boolean locFine = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean locCoarse = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean smsOk = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        if (!locFine && !locCoarse || !smsOk) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.SEND_SMS
            }, REQ_PERMISSIONS_SOS);
            Toast.makeText(requireContext(), "Please grant permissions and tap SOS again", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    String msg = buildSosMessage(location);
                    sendSmsToAll(msg);
                })
                .addOnFailureListener(e -> {
                    String msg = buildSosMessage(null);
                    sendSmsToAll(msg);
                });
    }

    private String buildSosMessage(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("EMERGENCY! I need help.\n");
        if (location != null) {
            String mapsUrl = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
            sb.append("My current location: ")
              .append(mapsUrl)
              .append("\n");
        } else {
            sb.append("Location not available. Please try calling me.");
        }
        return sb.toString();
    }

    private void sendSmsToAll(String message) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "SMS permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        for (EmergencyContact c : contactList) {
            android.telephony.SmsManager sms = android.telephony.SmsManager.getDefault();
            sms.sendTextMessage(c.getPhone(), null, message, null, null);
        }

        Toast.makeText(requireContext(), "SOS sent to " + contactList.size() + " contacts", Toast.LENGTH_SHORT).show();
    }
}
