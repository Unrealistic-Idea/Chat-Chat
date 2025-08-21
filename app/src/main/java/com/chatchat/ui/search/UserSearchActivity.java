package com.chatchat.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.database.AppDatabase;
import com.chatchat.database.UserDao;
import com.chatchat.model.User;
import com.chatchat.ui.profile.UserProfileActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户搜索页面
 */
public class UserSearchActivity extends AppCompatActivity implements UserSearchAdapter.OnUserClickListener {
    
    private EditText editTextSearch;
    private RecyclerView recyclerViewUsers;
    private UserSearchAdapter userAdapter;
    private List<User> allUsers;
    private List<User> filteredUsers;
    
    private UserDao userDao;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchListener();
        loadUsers();
    }
    
    private void initViews() {
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("搜索用户");
        }
    }
    
    private void setupRecyclerView() {
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        userAdapter = new UserSearchAdapter(filteredUsers, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }
    
    private void setupSearchListener() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadUsers() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        
        executor.execute(() -> {
            if (userDao == null) {
                AppDatabase database = AppDatabase.getDatabase(this);
                userDao = database.userDao();
            }
            
            // Load some demo users if database is empty
            List<User> users = userDao.getAllUsers();
            if (users.isEmpty()) {
                createDemoUsers();
                users = userDao.getAllUsers();
            }
            
            runOnUiThread(() -> {
                allUsers.clear();
                allUsers.addAll(users);
                filteredUsers.clear();
                filteredUsers.addAll(users);
                userAdapter.notifyDataSetChanged();
            });
        });
    }
    
    private void createDemoUsers() {
        // Create some demo users for demonstration
        User[] demoUsers = {
            new User("user001", "张三", "zhang.san@example.com"),
            new User("user002", "李四", "li.si@example.com"),
            new User("user003", "王五", "wang.wu@example.com"),
            new User("user004", "赵六", "zhao.liu@example.com"),
            new User("user005", "陈七", "chen.qi@example.com"),
            new User("ai_assistant", "AI助手", "ai@chatchat.com")
        };
        
        for (User user : demoUsers) {
            userDao.insertUser(user);
        }
    }
    
    private void filterUsers(String query) {
        filteredUsers.clear();
        
        if (query.trim().isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerQuery) ||
                    user.getUserId().toLowerCase().contains(lowerQuery) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                    filteredUsers.add(user);
                }
            }
        }
        
        userAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onUserClick(User user) {
        // Open user profile
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("user_id", user.getUserId());
        intent.putExtra("username", user.getUsername());
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}