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
import com.chatchat.model.Message;
import com.chatchat.ui.adapter.MessageAdapter;
import com.chatchat.utils.GpuOptimizationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    
    public static final String EXTRA_CHAT_NAME = "chat_name";
    public static final String EXTRA_CHAT_USER_ID = "chat_user_id";
    public static final String EXTRA_IS_AI_CHAT = "is_ai_chat";
    
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
    private ExecutorService executor;
    private SharedPreferences sharedPreferences;
    
    private String chatName;
    private String chatUserId;
    private boolean isAiChat;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 启用GPU硬件加速
        GpuOptimizationManager.enableHardwareAcceleration(this);
        
        setContentView(R.layout.activity_chat);

        getIntentData();
        initViews();
        initDatabase();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
    }

    private void getIntentData() {
        chatName = getIntent().getStringExtra(EXTRA_CHAT_NAME);
        chatUserId = getIntent().getStringExtra(EXTRA_CHAT_USER_ID);
        isAiChat = getIntent().getBooleanExtra(EXTRA_IS_AI_CHAT, false);
        
        if (chatName == null) chatName = "聊天";
        if (chatUserId == null) chatUserId = "ai_assistant";
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
        executor = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("ChatChatPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("current_user_id", "unknown");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        textViewChatName.setText(chatName);
        textViewChatStatus.setText(isAiChat ? "AI助手" : "在线");
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        messageAdapter.setOnMessageActionListener(this::recallMessage);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
        
        // 为消息RecyclerView启用GPU优化
        GpuOptimizationManager.optimizeRecyclerViewForGpu(recyclerViewMessages);
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
            List<Message> messages;
            if (isAiChat) {
                messages = messageDao.getAiMessages(currentUserId);
            } else {
                messages = messageDao.getDirectMessages(currentUserId, chatUserId);
            }

            runOnUiThread(() -> {
                messageAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            });
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        editTextMessage.setText("");
        
        // Create and save user message
        Message userMessage = new Message(
            UUID.randomUUID().toString(),
            currentUserId,
            messageText,
            Message.MessageType.TEXT
        );
        
        if (isAiChat) {
            userMessage.setReceiverId("ai_assistant");
        } else {
            userMessage.setReceiverId(chatUserId);
        }

        executor.execute(() -> {
            messageDao.insertMessage(userMessage);
            
            runOnUiThread(() -> {
                messageAdapter.addMessage(userMessage);
                recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            });

            // Handle AI response
            if (isAiChat) {
                handleAiResponse(messageText);
            }
        });
    }

    private void handleAiResponse(String userMessage) {
        // Simple AI response logic
        String aiResponse = generateAiResponse(userMessage);
        
        Message aiMessage = new Message(
            UUID.randomUUID().toString(),
            "ai_assistant",
            aiResponse,
            Message.MessageType.TEXT
        );
        aiMessage.setReceiverId(currentUserId);
        aiMessage.setAiMessage(true);

        // Simulate a small delay for AI response
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        messageDao.insertMessage(aiMessage);
        
        runOnUiThread(() -> {
            messageAdapter.addMessage(aiMessage);
            recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    private String generateAiResponse(String userMessage) {
        // Simple AI response generator
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("你好") || lowerMessage.contains("hello")) {
            return "你好！我是AI助手，很高兴为您服务！有什么可以帮助您的吗？";
        } else if (lowerMessage.contains("天气")) {
            return "抱歉，我目前无法获取实时天气信息。建议您查看天气应用获取准确信息。";
        } else if (lowerMessage.contains("时间")) {
            return "当前时间是 " + new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("thank")) {
            return "不客气！很高兴能帮助到您。还有其他问题吗？";
        } else if (lowerMessage.contains("再见") || lowerMessage.contains("bye")) {
            return "再见！期待下次与您的对话！";
        } else {
            return "我理解您说的是：" + userMessage + "\n\n这是一个演示AI回复。在完整版本中，这里会接入真正的AI API来提供更智能的对话。";
        }
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
        
        if (isAiChat) {
            emojiMessage.setReceiverId("ai_assistant");
        } else {
            emojiMessage.setReceiverId(chatUserId);
        }

        executor.execute(() -> {
            messageDao.insertMessage(emojiMessage);
            
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
        
        if (isAiChat) {
            imageMessage.setReceiverId("ai_assistant");
        } else {
            imageMessage.setReceiverId(chatUserId);
        }

        executor.execute(() -> {
            messageDao.insertMessage(imageMessage);
            
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
        
        if (isAiChat) {
            markdownMessage.setReceiverId("ai_assistant");
        } else {
            markdownMessage.setReceiverId(chatUserId);
        }

        executor.execute(() -> {
            messageDao.insertMessage(markdownMessage);
            
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