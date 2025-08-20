package com.chatchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.MessageDao;
import com.chatchat.database.UserDao;
import com.chatchat.database.ChatGroupDao;
import com.chatchat.model.Message;
import com.chatchat.model.User;
import com.chatchat.model.ChatGroup;
import com.chatchat.ui.adapter.ChatListAdapter;
import com.chatchat.ui.chat.GroupChatActivity;
import com.chatchat.utils.GpuOptimizationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerViewChats;
    private TextView textViewEmptyState;
    private ChatListAdapter chatListAdapter;
    private AppDatabase database;
    private MessageDao messageDao;
    private UserDao userDao;
    private ChatGroupDao chatGroupDao;
    private ExecutorService executor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat_list, container, false);

        initViews(root);
        initDatabase();
        setupRecyclerView();
        loadChats();

        return root;
    }

    private void initViews(View root) {
        recyclerViewChats = root.findViewById(R.id.recyclerViewChats);
        textViewEmptyState = root.findViewById(R.id.textViewEmptyState);
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(requireContext());
        messageDao = database.messageDao();
        userDao = database.userDao();
        chatGroupDao = database.chatGroupDao();
        executor = Executors.newSingleThreadExecutor();
    }

    private void setupRecyclerView() {
        chatListAdapter = new ChatListAdapter(new ArrayList<>());
        chatListAdapter.setOnChatItemClickListener(this::openChat);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(chatListAdapter);
        
        // 为RecyclerView启用GPU优化
        GpuOptimizationManager.optimizeRecyclerViewForGpu(recyclerViewChats);
    }

    private void openChat(ChatItem chatItem) {
        Intent intent;
        if (chatItem.isGroupChat()) {
            intent = new Intent(getActivity(), GroupChatActivity.class);
            intent.putExtra(GroupChatActivity.EXTRA_GROUP_ID, chatItem.getGroupId());
            intent.putExtra(GroupChatActivity.EXTRA_GROUP_NAME, chatItem.getName());
        } else {
            intent = new Intent(getActivity(), com.chatchat.ui.chat.ChatActivity.class);
            intent.putExtra(com.chatchat.ui.chat.ChatActivity.EXTRA_CHAT_NAME, chatItem.getName());
            intent.putExtra(com.chatchat.ui.chat.ChatActivity.EXTRA_CHAT_USER_ID, chatItem.getUserId());
            intent.putExtra(com.chatchat.ui.chat.ChatActivity.EXTRA_IS_AI_CHAT, chatItem.isAiChat());
        }
        startActivity(intent);
    }

    private void loadChats() {
        executor.execute(() -> {
            // For now, load some demo data including AI chat
            List<ChatItem> chatItems = new ArrayList<>();
            
            // Add AI Assistant as the first chat
            ChatItem aiChat = new ChatItem();
            aiChat.setName("AI助手");
            aiChat.setLastMessage("我是您的智能助手，有什么可以帮助您的吗？");
            aiChat.setTimestamp(System.currentTimeMillis());
            aiChat.setUnreadCount(0);
            aiChat.setIsAiChat(true);
            chatItems.add(aiChat);

            // Load group chats
            List<ChatGroup> groups = chatGroupDao.getAllChatGroups();
            for (ChatGroup group : groups) {
                ChatItem chatItem = new ChatItem();
                chatItem.setName(group.getGroupName());
                chatItem.setGroupId(group.getGroupId());
                chatItem.setLastMessage("群聊消息");
                chatItem.setTimestamp(group.getLastMessageTime());
                chatItem.setUnreadCount(group.getUnreadCount());
                chatItem.setIsGroupChat(true);
                chatItems.add(chatItem);
            }

            // Load actual user chats would go here
            List<User> users = userDao.getAllUsers();
            for (User user : users) {
                // Skip current user
                String currentUserId = requireActivity().getSharedPreferences("ChatChatPrefs", 0)
                    .getString("current_user_id", "");
                if (!user.getTravelerId().equals(currentUserId)) {
                    ChatItem chatItem = new ChatItem();
                    chatItem.setName(user.getUsername());
                    chatItem.setUserId(user.getTravelerId());
                    chatItem.setLastMessage("点击开始对话");
                    chatItem.setTimestamp(user.getLastSeen());
                    chatItem.setUnreadCount(0);
                    chatItem.setIsOnline(user.isOnline());
                    chatItems.add(chatItem);
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (chatItems.isEmpty()) {
                    textViewEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewChats.setVisibility(View.GONE);
                } else {
                    textViewEmptyState.setVisibility(View.GONE);
                    recyclerViewChats.setVisibility(View.VISIBLE);
                    chatListAdapter.updateChats(chatItems);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Helper class for chat list items
    public static class ChatItem {
        private String name;
        private String userId;
        private String groupId;
        private String lastMessage;
        private long timestamp;
        private int unreadCount;
        private boolean isOnline;
        private boolean isAiChat;
        private boolean isGroupChat;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }

        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

        public boolean isOnline() { return isOnline; }
        public void setIsOnline(boolean online) { isOnline = online; }

        public boolean isAiChat() { return isAiChat; }
        public void setIsAiChat(boolean aiChat) { isAiChat = aiChat; }

        public boolean isGroupChat() { return isGroupChat; }
        public void setIsGroupChat(boolean groupChat) { isGroupChat = groupChat; }
    }
}