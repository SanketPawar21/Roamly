package com.Sanket.roamly;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder> {

    private final Context context;
    private List<UserModel> participants;
    private List<String> participantIds;

    public ParticipantsAdapter(Context context, List<UserModel> participants, List<String> participantIds) {
        this.context = context;
        this.participants = participants;
        this.participantIds = participantIds;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        UserModel user = participants.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        String uid = (participantIds != null && position < participantIds.size())
                ? participantIds.get(position)
                : null;

        if (uid != null) {
            holder.itemView.setOnClickListener(v -> {
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userId", uid);
                fragment.setArguments(bundle);

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userFragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvParticipantName);
            email = itemView.findViewById(R.id.tvParticipantEmail);
        }
    }
}
