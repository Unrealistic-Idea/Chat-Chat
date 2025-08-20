package com.chatchat.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.chatchat.R;
import com.chatchat.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfileAvatar;
    private TextView textViewProfileName;
    private TextView textViewProfileTravelerId;
    private LinearLayout layoutChangeAvatar;
    private LinearLayout layoutAvatarAccessory;
    private LinearLayout layoutShareContact;
    private MaterialButton buttonLogout;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(root);
        initSharedPreferences();
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
    }

    private void initSharedPreferences() {
        sharedPreferences = requireActivity().getSharedPreferences("ChatChatPrefs", 0);
    }

    private void loadUserProfile() {
        String currentUserId = sharedPreferences.getString("current_user_id", "未知用户");
        textViewProfileName.setText(currentUserId);
        textViewProfileTravelerId.setText("旅者ID: " + currentUserId);
    }

    private void setupClickListeners() {
        layoutChangeAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "更换头像功能开发中...", Toast.LENGTH_SHORT).show();
        });

        layoutAvatarAccessory.setOnClickListener(v -> {
            Toast.makeText(getContext(), "头像挂件功能开发中...", Toast.LENGTH_SHORT).show();
        });

        layoutShareContact.setOnClickListener(v -> {
            shareContact();
        });

        buttonLogout.setOnClickListener(v -> {
            logout();
        });
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