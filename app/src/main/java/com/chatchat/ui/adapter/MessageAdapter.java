package com.chatchat.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.model.Message;
import com.chatchat.utils.GpuOptimizationManager;
import io.noties.markwon.Markwon;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;
    private OnMessageActionListener messageActionListener;

    public interface OnMessageActionListener {
        void onMessageRecall(Message message);
    }

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.messageActionListener = listener;
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
            // 为消息视图启用GPU加速
            GpuOptimizationManager.enableGpuLayerForView(view);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            // 为消息视图启用GPU加速
            GpuOptimizationManager.enableGpuLayerForView(view);
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

    private static void displayMessageContent(Message message, TextView textView) {
        switch (message.getType()) {
            case EMOJI:
                textView.setTextSize(24); // Larger text for emojis
                textView.setText(message.getContent());
                break;
            case IMAGE:
                textView.setTextSize(14); // Normal size
                textView.setText(message.getContent() + " 📷");
                break;
            case VOICE:
                textView.setTextSize(14);
                textView.setText(message.getContent() + " 🎵");
                break;
            case MARKDOWN:
                textView.setTextSize(14);
                Markwon markwon = Markwon.create(textView.getContext());
                markwon.setMarkdown(textView, message.getContent());
                break;
            case CHART:
                textView.setTextSize(14);
                textView.setText(message.getContent() + " 📊");
                break;
            default:
                textView.setTextSize(14);
                textView.setText(message.getContent());
                break;
        }
    }

    private void showMessageOptions(Message message, View view) {
        // Only allow recall for sent messages that are less than 2 minutes old
        long currentTime = System.currentTimeMillis();
        long messageTime = message.getTimestamp();
        boolean canRecall = message.getSenderId().equals(currentUserId) && 
                           (currentTime - messageTime) < 120000 && // 2 minutes
                           !message.isRecalled();
        
        if (canRecall) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("消息操作");
            builder.setItems(new String[]{"撤回消息"}, (dialog, which) -> {
                if (which == 0 && messageActionListener != null) {
                    messageActionListener.onMessageRecall(message);
                }
            });
            builder.show();
        }
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
                displayMessageContent(message, textViewMessage);
                textViewMessage.setTextColor(itemView.getContext().getColor(R.color.black));
                textViewMessage.setAlpha(1.0f);
            }
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(timeFormat.format(new Date(message.getTimestamp())));
            
            textViewStatus.setText(message.isRead() ? "已读" : "未读");
            
            // Add long press listener for message recall
            itemView.setOnLongClickListener(v -> {
                showMessageOptions(message, v);
                return true;
            });
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
                displayMessageContent(message, textViewMessage);
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