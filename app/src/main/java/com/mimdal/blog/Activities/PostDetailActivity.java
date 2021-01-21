package com.mimdal.blog.Activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mimdal.blog.Adapters.CommentAdaptet;
import com.mimdal.blog.Models.Comment;
import com.mimdal.blog.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends BaseActivity {

    private ImageView post_detail_img, post_detail_writer_profile_img, post_detail_profile_img;
    private TextView post_detail_title, post_detail_date, post_detail_description;
    private EditText post_detail_edt_comment;
    private Button post_detail_add_comment;
    private RecyclerView RvComments;
    private CommentAdaptet commentAdaptet;
    private String postKey;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //set status bar transparent

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        init();

        //get intent

        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(post_detail_img);

        String postTitle = getIntent().getExtras().getString("title");
        post_detail_title.setText(postTitle);

        String postDescription = getIntent().getExtras().getString("description");
        post_detail_description.setText(postDescription);

        String postUserPhoto = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(postUserPhoto).into(post_detail_writer_profile_img);

        String date = timeStampToString(getIntent().getExtras().getLong("postDate"));
        post_detail_date.setText(date);

        Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).into(post_detail_profile_img);

        postKey = getIntent().getExtras().getString("postKey");

        post_detail_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                post_detail_edt_comment.setVisibility(View.INVISIBLE);
                DatabaseReference databaseReference = database.getReference("Comment").child(postKey).push();
                String commentContent = post_detail_edt_comment.getText().toString();
                String userId = mAuth.getCurrentUser().getUid();
                String userName = mAuth.getCurrentUser().getDisplayName();
                String userImg = mAuth.getCurrentUser().getPhotoUrl().toString();

                Comment comment = new Comment(commentContent, userId, userImg, userName);

                databaseReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("Comment added");
                        post_detail_edt_comment.setText("");
                        post_detail_edt_comment.setVisibility(View.VISIBLE);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        showMessage("failed to add comment. " + e.getMessage());

                    }
                });


            }
        });

        setUpCommentRV();

    }

    private void setUpCommentRV() {

        database.getReference("Comment").child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                RvComments.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));
                commentList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdaptet = new CommentAdaptet(PostDetailActivity.this, commentList);
                RvComments.setAdapter(commentAdaptet);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private String timeStampToString(long time) {

//        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
//        calendar.setTimeInMillis(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date(time));
    }

    private void init() {

        post_detail_img = findViewById(R.id.post_detail_img);
        post_detail_writer_profile_img = findViewById(R.id.post_detail_writer_profile_img);
        post_detail_profile_img = findViewById(R.id.post_detail_profile_img);
        post_detail_title = findViewById(R.id.post_detail_title);
        post_detail_date = findViewById(R.id.post_detail_date);
        post_detail_description = findViewById(R.id.post_detail_description);
        post_detail_edt_comment = findViewById(R.id.post_detail_edt_comment);
        post_detail_add_comment = findViewById(R.id.post_detail_add_comment);
        RvComments = findViewById(R.id.RvComments);
    }
}