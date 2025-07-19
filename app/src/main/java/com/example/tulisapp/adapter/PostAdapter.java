package com.example.tulisapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tulisapp.R;
import com.example.tulisapp.models.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeClick(Post post);
    }

    public PostAdapter(List<Post> postList, OnPostInteractionListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostUserName.setText(post.getUserName());
        holder.tvPostText.setText(post.getText());

        if (post.getTimestamp() != null) {
            Date date = post.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            holder.tvPostTimestamp.setText(sdf.format(date));
        }

        if (post.getUserProfileImageUrl() != null && !post.getUserProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getUserProfileImageUrl())
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(holder.ivPostUserProfileImage);
        } else {
            holder.ivPostUserProfileImage.setImageResource(R.drawable.baseline_account_circle_24);
        }

        holder.tvLikesCount.setText(String.valueOf(post.getLikesCount()));

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId != null && post.getLikedBy().contains(currentUserId)) {
            holder.ivLikeButton.setImageResource(R.drawable.baseline_favorite_24);
            holder.ivLikeButton.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.ivLikeButton.setImageResource(R.drawable.baseline_favorite_border_24);
            holder.ivLikeButton.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }

        holder.ivLikeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updatePost(Post updatedPost) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getId().equals(updatedPost.getId())) {
                postList.set(i, updatedPost);
                notifyItemChanged(i);
                return;
            }
        }

    }

    public void setPosts(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }


    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPostUserProfileImage;
        TextView tvPostUserName;
        TextView tvPostTimestamp;
        TextView tvPostText;
        ImageView ivLikeButton;
        TextView tvLikesCount;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostUserProfileImage = itemView.findViewById(R.id.ivPostUserProfileImage);
            tvPostUserName = itemView.findViewById(R.id.tvPostUserName);
            tvPostTimestamp = itemView.findViewById(R.id.tvPostTimestamp);
            tvPostText = itemView.findViewById(R.id.tvPostText);
            ivLikeButton = itemView.findViewById(R.id.ivLikeButton);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
        }
    }
}