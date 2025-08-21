package com.chatchat.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.chatchat.R;
import com.chatchat.ui.auth.LoginActivity;
import com.chatchat.utils.AvatarManager;
import com.google.android.material.button.MaterialButton;
import java.io.File;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfileAvatar;
    private TextView textViewProfileName;
    private TextView textViewProfileTravelerId;
    private LinearLayout layoutChangeAvatar;
    private LinearLayout layoutAvatarAccessory;
    private LinearLayout layoutShareContact;
    private MaterialButton buttonLogout;
    private MaterialButton buttonViewProfile;
    private MaterialButton buttonEditProfile;
    private SharedPreferences sharedPreferences;
    private AvatarManager avatarManager;
    
    private static final int REQUEST_AVATAR_PICK = 2001;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(root);
        initSharedPreferences();
        avatarManager = new AvatarManager(requireContext());
        loadUserProfile();
        setupClickListeners();

        return root;
    }

    private void initViews(View root) {
        imageViewProfileAvatar = root.findViewById(R.id.imageViewProfileAvatar);
        textViewProfileName = root.findViewById(R.id.textViewProfileName);
        textViewProfileTravelerId = root.findViewById(R.id.textViewProfileTravelerId);
        layoutChangeAvatar = root.findViewById(R.id.layoutChangeAvatar);
        layoutAvatarAccessory = root.findViewById(R.id.layoutAvatarAccessory);
        layoutShareContact = root.findViewById(R.id.layoutShareContact);
        buttonLogout = root.findViewById(R.id.buttonLogout);
        buttonViewProfile = root.findViewById(R.id.buttonViewProfile);
        buttonEditProfile = root.findViewById(R.id.buttonEditProfile);
    }

    private void initSharedPreferences() {
        sharedPreferences = requireActivity().getSharedPreferences("ChatChatPrefs", 0);
    }

    private void loadUserProfile() {
        String currentUserId = sharedPreferences.getString("current_user_id", "未知用户");
        textViewProfileName.setText(currentUserId);
        textViewProfileTravelerId.setText("旅者ID: " + currentUserId);
        
        // Load avatar
        String avatarPath = avatarManager.getAvatarPath();
        if (avatarPath != null && new File(avatarPath).exists()) {
            Glide.with(this)
                    .load(avatarPath)
                    .circleCrop()
                    .into(imageViewProfileAvatar);
        }
    }

    private void setupClickListeners() {
        layoutChangeAvatar.setOnClickListener(v -> changeAvatar());

        layoutAvatarAccessory.setOnClickListener(v -> showAccessoryPicker());

        layoutShareContact.setOnClickListener(v -> {
            shareContact();
        });

        buttonLogout.setOnClickListener(v -> {
            logout();
        });
        
        buttonViewProfile.setOnClickListener(v -> {
            viewProfile();
        });
        
        buttonEditProfile.setOnClickListener(v -> {
            editProfile();
        });
    }

    private void changeAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_AVATAR_PICK);
    }

    private void showAccessoryPicker() {
        String[] accessories = avatarManager.getAvailableAccessories();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择头像挂件");
        builder.setItems(accessories, (dialog, which) -> {
            avatarManager.setAvatarAccessory(accessories[which]);
            Toast.makeText(getContext(), "挂件已设置: " + accessories[which], Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("移除挂件", (dialog, which) -> {
            avatarManager.clearAccessory();
            Toast.makeText(getContext(), "挂件已移除", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_AVATAR_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri avatarUri = data.getData();
            if (avatarUri != null) {
                boolean saved = avatarManager.saveAvatar(avatarUri);
                if (saved) {
                    loadUserProfile(); // Refresh the avatar display
                    Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "头像保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void viewProfile() {
        String currentUserId = sharedPreferences.getString("current_user_id", "未知用户");
        String username = sharedPreferences.getString("username", currentUserId);
        String email = sharedPreferences.getString("user_email", "");
        
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra("user_id", currentUserId);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    
    private void editProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void shareContact() {
        String currentUserId = sharedPreferences.getString("current_user_id", "未知用户");
        String contactCard = "我的Chat-Chat名片\n" +
                "用户名: " + currentUserId + "\n" +
                "旅者ID: " + currentUserId + "\n" +
                "欢迎添加我为好友！";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, contactCard);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chat-Chat 名片分享");

        try {
            startActivity(Intent.createChooser(shareIntent, "分享名片"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}