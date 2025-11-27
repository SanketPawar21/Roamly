package com.Sanket.roamly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<EmergencyContact> contacts;
    private final OnRemoveClickListener removeClickListener;

    public EmergencyContactsAdapter(List<EmergencyContact> contacts, OnRemoveClickListener listener) {
        this.contacts = contacts;
        this.removeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact c = contacts.get(position);
        holder.tvName.setText(c.getName() != null ? c.getName() : "Unknown");
        holder.tvPhone.setText(c.getPhone());

        holder.btnRemove.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemove(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            btnRemove = itemView.findViewById(R.id.btnRemoveContact);
        }
    }
}
