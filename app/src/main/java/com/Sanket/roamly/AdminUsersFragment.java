package com.Sanket.roamly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView recyclerAdminUsers;
    private AdminUsersAdapter adapter;
    private List<AdminUser> users = new ArrayList<>();
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_users, container, false);

        recyclerAdminUsers = v.findViewById(R.id.recyclerAdminUsers);
        recyclerAdminUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUsersAdapter(requireContext(), users);
        recyclerAdminUsers.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadUsers();

        return v;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String uid = snap.getKey();
                    String name = snap.child("name").getValue(String.class);
                    String email = snap.child("email").getValue(String.class);
                    Double rating = snap.child("rating").getValue(Double.class);
                    Long ratingCount = snap.child("ratingCount").getValue(Long.class);
                    Boolean blocked = snap.child("blocked").getValue(Boolean.class);
                    long reportsCount = snap.child("reports").getChildrenCount();

                    AdminUser u = new AdminUser(
                            uid,
                            name,
                            email,
                            rating != null ? rating : 5.0,
                            ratingCount != null ? ratingCount : 0L,
                            reportsCount,
                            blocked != null && blocked
                    );
                    users.add(u);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
