package com.mimdal.blog.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mimdal.blog.Adapters.PostAdapter;
import com.mimdal.blog.Models.Post;
import com.mimdal.blog.R;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    RecyclerView profileRecyclerView;
    PostAdapter postAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<Post> profilePostList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        profileRecyclerView = view.findViewById(R.id.postRecyclerView);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("posts");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        //get post list from database

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                profilePostList = new ArrayList<>();

                for (DataSnapshot postSnap : snapshot.getChildren()) {

                    Post post = postSnap.getValue(Post.class);

                    if (post.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        profilePostList.add(post);
                    }
                }

                postAdapter = new PostAdapter(getActivity(), profilePostList);
                profileRecyclerView.setAdapter(postAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public String toString() {
        return "Profile";
    }
}
