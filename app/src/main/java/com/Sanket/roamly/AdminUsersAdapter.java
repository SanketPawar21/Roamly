package com.Sanket.roamly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.AdminUserViewHolder> {

    private final Context context;
    private final List<AdminUser> users;
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

    public AdminUsersAdapter(Context context, List<AdminUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new AdminUserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder h, int position) {
        AdminUser u = users.get(position);

        h.tvName.setText(u.name != null ? u.name : "(no name)");
        h.tvEmail.setText(u.email != null ? u.email : "(no email)");
        h.tvRating.setText("Rating: " + String.format("%.1f", u.rating) + " (" + u.ratingCount + ")");
        h.tvReports.setText("Reports: " + u.reportsCount);
        h.tvStatus.setText(u.blocked ? "Blocked" : "Active");

        h.btnBlock.setText(u.blocked ? "Unblock" : "Block");
        h.btnBlock.setOnClickListener(v -> toggleBlock(u));
    }

    private void toggleBlock(AdminUser u) {
        boolean newState = !u.blocked;
        usersRef.child(u.uid).child("blocked").setValue(newState)
                .addOnSuccessListener(unused -> {
                    u.blocked = newState;
                    notifyDataSetChanged();
                    Toast.makeText(context, newState ? "User blocked" : "User unblocked", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRating, tvReports, tvStatus;
        Button btnBlock;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminUserName);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvRating = itemView.findViewById(R.id.tvAdminUserRating);
            tvReports = itemView.findViewById(R.id.tvAdminUserReports);
            tvStatus = itemView.findViewById(R.id.tvAdminUserStatus);
            btnBlock = itemView.findViewById(R.id.btnBlockUser);
        }
    }
}
