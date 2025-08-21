package com.chatchat.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chatchat.R;
import com.chatchat.model.User;
import java.util.List;

/**
 * 用户搜索适配器
 */
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    
    private List<User> users;
    private OnUserClickListener listener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
    
    public UserSearchAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUsername;
        private TextView textViewUserId;
        private TextView textViewEmail;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewUserId = itemView.findViewById(R.id.textViewUserId);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUserClick(users.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(User user) {
            textViewUsername.setText(user.getUsername());
            textViewUserId.setText("ID: " + user.getUserId());
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                textViewEmail.setText(user.getEmail());
                textViewEmail.setVisibility(View.VISIBLE);
            } else {
                textViewEmail.setVisibility(View.GONE);
            }
        }
    }
}