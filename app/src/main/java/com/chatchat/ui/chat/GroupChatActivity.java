package com.chatchat.ui.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.chatchat.R;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.MessageDao;
import com.chatchat.database.ChatGroupDao;
import com.chatchat.model.Message;
import com.chatchat.model.ChatGroup;
import com.chatchat.ui.adapter.MessageAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupChatActivity extends AppCompatActivity {
    
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_GROUP_NAME = "group_name";
    
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    
    private Toolbar toolbar;
    private ImageView imageViewChatAvatar;
    private TextView textViewChatName;
    private TextView textViewChatStatus;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonEmoji;
    private ImageButton buttonImage;
    private ImageButton buttonVoice;
    
    private MessageAdapter messageAdapter;
    private AppDatabase database;
    private MessageDao messageDao;
    private ChatGroupDao chatGroupDao;
    private ExecutorService executor;
    private SharedPreferences sharedPreferences;
    
    private String groupId;
    private String groupName;
    private String currentUserId;
    private ChatGroup currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        initDatabase();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        loadGroupInfo();
    }

    private void getIntentData() {
        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        
        if (groupId == null) {
            finish();
            return;
        }
        if (groupName == null) groupName = "群聊";
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewChatAvatar = findViewById(R.id.imageViewChatAvatar);
        textViewChatName = findViewById(R.id.textViewChatName);
        textViewChatStatus = findViewById(R.id.textViewChatStatus);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonEmoji = findViewById(R.id.buttonEmoji);
        buttonImage = findViewById(R.id.buttonImage);
        buttonVoice = findViewById(R.id.buttonVoice);
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        messageDao = database.messageDao();
        chatGroupDao = database.chatGroupDao();
        executor = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("current_user_id", "");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        textViewChatName.setText(groupName);
        textViewChatStatus.setText("群聊");
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        messageAdapter.setOnMessageActionListener(this::recallMessage);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonSend.setOnLongClickListener(v -> {
            sendMarkdownMessage();
            return true;
        });
        
        buttonEmoji.setOnClickListener(v -> showEmojiPicker());
        
        buttonImage.setOnClickListener(v -> selectImage());
        
        buttonVoice.setOnClickListener(v -> {
            Toast.makeText(this, "语音功能开发中...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMessages() {
        executor.execute(() -> {
            List<Message> messages = messageDao.getGroupMessages(groupId);

            runOnUiThread(() -> {
                messageAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            });
        });
    }

    private void loadGroupInfo() {
        executor.execute(() -> {
            currentGroup = chatGroupDao.getChatGroupById(groupId);
            if (currentGroup != null) {
                runOnUiThread(() -> {
                    textViewChatName.setText(currentGroup.getGroupName());
                    // Update member count status
                    if (currentGroup.getMemberIds() != null) {
                        int memberCount = currentGroup.getMemberIds().size();
                        textViewChatStatus.setText(memberCount + "人");
                    }
                });
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        editTextMessage.setText("");
        
        // Create and save group message
        Message groupMessage = new Message(
            UUID.randomUUID().toString(),
            currentUserId,
            messageText,
            Message.MessageType.TEXT
        );
        
        groupMessage.setGroupId(groupId);

        executor.execute(() -> {
            messageDao.insertMessage(groupMessage);
            
            // Update group's last message
            chatGroupDao.updateLastMessage(groupId, groupMessage.getMessageId(), 
                    groupMessage.getTimestamp());
            
            runOnUiThread(() -> {
                messageAdapter.addMessage(groupMessage);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            });
        });
    }

    private void showEmojiPicker() {
        String[] emojis = {"😀", "😂", "😍", "🤔", "👍", "👎", "❤️", "😢", "😡", "🎉", "🔥", "💯"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择表情");
        builder.setItems(emojis, (dialog, which) -> {
            sendEmojiMessage(emojis[which]);
        });
        builder.show();
    }

    private void sendEmojiMessage(String emoji) {
        Message emojiMessage = new Message(
            UUID.randomUUID().toString(),
            currentUserId,
            emoji,
            Message.MessageType.EMOJI
        );
        
        emojiMessage.setGroupId(groupId);

        executor.execute(() -> {
            messageDao.insertMessage(emojiMessage);
            chatGroupDao.updateLastMessage(groupId, emojiMessage.getMessageId(), 
                    emojiMessage.getTimestamp());
            
            runOnUiThread(() -> {
                messageAdapter.addMessage(emojiMessage);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            });
        });
    }

    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                REQUEST_CAMERA_PERMISSION);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            String imageUri = data.getData().toString();
            sendImageMessage(imageUri);
        }
    }

    private void sendImageMessage(String imageUri) {
        Message imageMessage = new Message(
            UUID.randomUUID().toString(),
            currentUserId,
            "[图片]",
            Message.MessageType.IMAGE
        );
        
        imageMessage.setMediaUrl(imageUri);
        imageMessage.setGroupId(groupId);

        executor.execute(() -> {
            messageDao.insertMessage(imageMessage);
            chatGroupDao.updateLastMessage(groupId, imageMessage.getMessageId(), 
                    imageMessage.getTimestamp());
            
            runOnUiThread(() -> {
                messageAdapter.addMessage(imageMessage);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            });
        });
    }

    private void sendMarkdownMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        editTextMessage.setText("");
        
        // Create and save markdown message
        Message markdownMessage = new Message(
            UUID.randomUUID().toString(),
            currentUserId,
            messageText,
            Message.MessageType.MARKDOWN
        );
        
        markdownMessage.setGroupId(groupId);

        executor.execute(() -> {
            messageDao.insertMessage(markdownMessage);
            chatGroupDao.updateLastMessage(groupId, markdownMessage.getMessageId(), 
                    markdownMessage.getTimestamp());
            
            runOnUiThread(() -> {
                messageAdapter.addMessage(markdownMessage);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                Toast.makeText(this, "Markdown消息已发送", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void recallMessage(Message message) {
        executor.execute(() -> {
            messageDao.recallMessage(message.getMessageId());
            
            runOnUiThread(() -> {
                // Update the message in the adapter
                message.setRecalled(true);
                messageAdapter.notifyDataSetChanged();
                Toast.makeText(this, "消息已撤回", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}