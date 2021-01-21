package com.mimdal.blog.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mimdal.blog.Models.Comment;
import com.mimdal.blog.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdaptet extends RecyclerView.Adapter<CommentAdaptet.CommentViewHolder> {

    private static final String TAG = "CommentAdaptet";

    private Context mcontex;
    private List<Comment> commentList;

    public CommentAdaptet(Context mcontex, List<Comment> commentList) {
        this.mcontex = mcontex;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontex).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        try{Glide.with(mcontex).load(commentList.get(position).getUserImg()).into(holder.comment_img);}
        catch (Exception e){
            Log.d(TAG,"Error with glide");

        }finally {

        holder.comment_user_name.setText(commentList.get(position).getUserName());
            holder.comment_content.setText(commentList.get(position).getContent());
            holder.comment_date.setText(timeStampToString((long) commentList.get(position).getTimeStamp()));
        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView comment_img;
        TextView comment_user_name;
        TextView comment_content;
        TextView comment_date;


        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            comment_img = itemView.findViewById(R.id.comment_img);
            comment_user_name = itemView.findViewById(R.id.comment_user_name);
            comment_content = itemView.findViewById(R.id.comment_content);
            comment_date = itemView.findViewById(R.id.comment_date);
        }
    }

    private String timeStampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm", calendar).toString();
        return date;
    }
}
