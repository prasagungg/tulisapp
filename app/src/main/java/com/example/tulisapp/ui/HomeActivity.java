package com.example.tulisapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tulisapp.R;

import com.example.tulisapp.adapter.PostAdapter;
import com.example.tulisapp.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    private static final String TAG = "HomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private ImageView ivUserImageHeader;
    private TextView tvUserNameHeader;

    private EditText etWhatsHappening;
    private Button btnPost;

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private ListenerRegistration postListenerRegistration;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ivUserImageHeader = findViewById(R.id.ivUserImageHeader);

        etWhatsHappening = findViewById(R.id.etWhatsHappening);
        btnPost = findViewById(R.id.btnPost);

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        recyclerViewPosts.setAdapter(postAdapter);

        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            android.net.Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .circleCrop()
                        .into(ivUserImageHeader);

            } else {
                ivUserImageHeader.setImageResource(R.drawable.baseline_account_circle_24);
            }

            ivUserImageHeader.setOnClickListener(v -> showProfilePopupMenu(v));

            btnPost.setOnClickListener(v -> {
                String postText = etWhatsHappening.getText().toString().trim();
                if (!postText.isEmpty()) {
                    addNewPost(postText, currentUser);
                    etWhatsHappening.setText("");
                }
            });

        } else {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPostListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (postListenerRegistration != null) {
            postListenerRegistration.remove();
            postListenerRegistration = null;
        }
    }

    private void startPostListener() {
        if (postListenerRegistration == null) {
            postListenerRegistration = db.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                Post post = dc.getDocument().toObject(Post.class);
                                post.setId(dc.getDocument().getId());

                                switch (dc.getType()) {
                                    case ADDED:
                                        postList.add(0, post);
                                        postAdapter.notifyItemInserted(0);
                                        recyclerViewPosts.scrollToPosition(0);
                                        break;
                                    case MODIFIED:
                                        postAdapter.updatePost(post);
                                        break;
                                    case REMOVED:
                                        for (int i = 0; i < postList.size(); i++) {
                                            if (postList.get(i).getId().equals(post.getId())) {
                                                postList.remove(i);
                                                postAdapter.notifyItemRemoved(i);
                                                break;
                                            }
                                        }
                                        break;
                                }
                            }
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    });
        }
    }

    private void addNewPost(String text, FirebaseUser currentUser) {
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous";
        String userProfileImageUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";

        Post newPost = new Post(
                null,
                userId,
                userName,
                userProfileImageUrl,
                text,
                com.google.firebase.Timestamp.now()
        );

        db.collection("posts").add(newPost)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(HomeActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onLikeClick(Post post) {
        if (currentUser == null) {
            Log.d(TAG, "User not logged in, cannot like post.");
            Toast.makeText(this, "Please log in to like posts.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        boolean hasLiked = post.getLikedBy().contains(currentUserId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            Post currentPost = transaction.get(db.collection("posts").document(post.getId()))
                    .toObject(Post.class);

            if (currentPost == null) {
                return null;
            }

            Map<String, Object> updates = new HashMap<>();
            if (hasLiked) {
                updates.put("likesCount", FieldValue.increment(-1));
                updates.put("likedBy", FieldValue.arrayRemove(currentUserId));
            } else {
                updates.put("likesCount", FieldValue.increment(1));
                updates.put("likedBy", FieldValue.arrayUnion(currentUserId));
            }

            transaction.update(db.collection("posts").document(post.getId()), updates);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Post like status updated successfully for post: " + post.getId());
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Error updating post like status: " + post.getId(), e);
            Toast.makeText(HomeActivity.this, "Failed to update like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void showProfilePopupMenu(android.view.View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }
}