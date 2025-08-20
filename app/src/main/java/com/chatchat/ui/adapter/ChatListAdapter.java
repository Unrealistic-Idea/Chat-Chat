package com.chatchat.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.ui.ChatListFragment.ChatItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatItem> chatItems;
    private OnChatItemClickListener listener;

    public interface OnChatItemClickListener {
        void onChatItemClick(ChatItem chatItem);
    }

    public ChatListAdapter(List<ChatItem> chatItems) {
        this.chatItems = chatItems;
    }

    public void setOnChatItemClickListener(OnChatItemClickListener listener) {
        this.listener = listener;
    }

    public void updateChats(List<ChatItem> newChatItems) {
        this.chatItems = newChatItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chatItem = chatItems.get(position);
        holder.bind(chatItem);
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewAvatar;
        private TextView textViewName;
        private TextView textViewLastMessage;
        private TextView textViewTime;
        private TextView textViewUnreadCount;
        private View viewOnlineStatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);
            viewOnlineStatus = itemView.findViewById(R.id.viewOnlineStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onChatItemClick(chatItems.get(position));
                    }
                }
            });
        }

        public void bind(ChatItem chatItem) {
            textViewName.setText(chatItem.getName());
            textViewLastMessage.setText(chatItem.getLastMessage());
            
            // Format timestamp
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(timeFormat.format(new Date(chatItem.getTimestamp())));
            
            // Show unread count if > 0
            if (chatItem.getUnreadCount() > 0) {
                textViewUnreadCount.setVisibility(View.VISIBLE);
                textViewUnreadCount.setText(String.valueOf(chatItem.getUnreadCount()));
            } else {
                textViewUnreadCount.setVisibility(View.GONE);
            }
            
            // Show online status for non-AI chats
            if (!chatItem.isAiChat() && chatItem.isOnline()) {
                viewOnlineStatus.setVisibility(View.VISIBLE);
            } else {
                viewOnlineStatus.setVisibility(View.GONE);
            }
            
            // Set different icon for AI chat
            if (chatItem.isAiChat()) {
                // For now use default, could set special AI icon here
                imageViewAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        }
    }
}