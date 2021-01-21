package com.mimdal.blog.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mimdal.blog.Activities.PostDetailActivity;
import com.mimdal.blog.Models.Post;
import com.mimdal.blog.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private Context mcontext;
    private List<Post> mData;

    public PostAdapter(Context mcontext, List<Post> mData) {
        this.mcontext = mcontext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.post_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.item_title.setText(mData.get(position).getTitle());
        holder.item_userId.setText(mData.get(position).getUserName());
        Glide.with(mcontext).load(mData.get(position).getPicture()).into(holder.item_picture);
        Glide.with(mcontext).load(mData.get(position).getUserPhoto()).into(holder.item_userPhoto);


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView item_title;
        TextView item_userId;
        ImageView item_picture;
        ImageView item_userPhoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_userId = itemView.findViewById(R.id.item_userId);
            item_picture = itemView.findViewById(R.id.item_picture);
            item_userPhoto = itemView.findViewById(R.id.item_userPhoto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetail = new Intent(mcontext, PostDetailActivity.class);
                    int position = getAdapterPosition();
                    postDetail.putExtra("title", mData.get(position).getTitle());
                    postDetail.putExtra("description", mData.get(position).getDescription());
                    postDetail.putExtra("postImage", mData.get(position).getPicture());
                    postDetail.putExtra("postKey", mData.get(position).getPostKey());
                    postDetail.putExtra("userPhoto", mData.get(position).getUserPhoto());
                    postDetail.putExtra("postDate", (long) mData.get(position).getTimeStamp());
                    mcontext.startActivity(postDetail);
                }
            });

        }
    }
}
