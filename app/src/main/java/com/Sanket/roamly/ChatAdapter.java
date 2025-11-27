package com.Sanket.roamly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvSender.setText(message.getSenderName() != null ? message.getSenderName() : "");
        holder.tvMessage.setText(message.getMessage());

        // Date header: show when first message of this day
        String currentDate = dateFormat.format(new Date(message.getTimestamp()));
        if (position == 0) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDate);
        } else {
            ChatMessage prev = messages.get(position - 1);
            String prevDate = dateFormat.format(new Date(prev.getTimestamp()));
            if (!currentDate.equals(prevDate)) {
                holder.tvDateHeader.setVisibility(View.VISIBLE);
                holder.tvDateHeader.setText(currentDate);
            } else {
                holder.tvDateHeader.setVisibility(View.GONE);
            }
        }

        // Time (HH:mm)
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        if (message.getSenderId() != null && message.getSenderId().equals(currentUid)) {
            // current user: align right, gradient bubble
            holder.rootMessage.setGravity(Gravity.END);
            holder.tvMessage.setBackgroundResource(R.drawable.btn_gradient);
            holder.tvMessage.setTextColor(holder.itemView.getResources().getColor(android.R.color.white));
        } else {
            // others: align left, card background
            holder.rootMessage.setGravity(Gravity.START);
            holder.tvMessage.setBackgroundResource(R.drawable.bg_card);
            holder.tvMessage.setTextColor(holder.itemView.getResources().getColor(R.color.textPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootMessage;
        TextView tvSender, tvMessage, tvTime, tvDateHeader;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            rootMessage = itemView.findViewById(R.id.rootMessage);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
