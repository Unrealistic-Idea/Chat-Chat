package com.chatchat.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.model.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? TYPE_MESSAGE_SENT : TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        private TextView textViewStatus;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }

        public void bind(Message message) {
            if (message.isRecalled()) {
                textViewMessage.setText("消息已撤回");
                textViewMessage.setTextColor(itemView.getContext().getColor(R.color.dark_gray));
                textViewMessage.setAlpha(0.6f);
            } else {
                textViewMessage.setText(message.getContent());
                textViewMessage.setTextColor(itemView.getContext().getColor(R.color.black));
                textViewMessage.setAlpha(1.0f);
            }
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            textViewStatus.setText(message.isRead() ? "已读" : "未读");
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        private TextView textViewSender;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSender = itemView.findViewById(R.id.textViewSender);
        }

        public void bind(Message message) {
            if (message.isRecalled()) {
                textViewMessage.setText("消息已撤回");
                textViewMessage.setTextColor(itemView.getContext().getColor(R.color.dark_gray));
                textViewMessage.setAlpha(0.6f);
            } else {
                textViewMessage.setText(message.getContent());
                textViewMessage.setTextColor(itemView.getContext().getColor(R.color.black));
                textViewMessage.setAlpha(1.0f);
            }
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            if (message.isAiMessage()) {
                textViewSender.setText("AI助手");
            } else {
                textViewSender.setText(message.getSenderId());
            }
        }
    }
}