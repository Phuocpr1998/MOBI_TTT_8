package com.hcmus.dreamers.foodmap.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.R;
import com.hcmus.dreamers.foodmap.common.TimeFormatter;
import com.hcmus.dreamers.foodmap.event.ClickListener;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolderComment>  {

    Context context;
    int resource;
    List<Comment> commentList;

    private static ClickListener onClickListener;

    public CommentListAdapter(@NonNull Context context, int resource, List<Comment> commentList) {
        this.commentList = commentList;
        this.resource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderComment onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewHolderComment viewHolderComment = new ViewHolderComment(LayoutInflater.from(context).inflate(resource, viewGroup, false));
        return  viewHolderComment;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderComment viewHolderComment, int i) {
        String email = commentList.get(i).getEmailGuest();
        int imageRes = R.drawable.ic_avatar_guest_comment;
        String date = TimeFormatter.format(commentList.get(i).getDateTime());

        if (email.equals("") || email.equals("null")){
            email = commentList.get(i).getEmailOwner();
            imageRes = R.drawable.ic_avatar_owner_comment;
        }

        viewHolderComment.igvAvatar.setImageResource(imageRes);
        viewHolderComment.txtEmail.setText(email);
        viewHolderComment.txtComment.setText(commentList.get(i).getComment());
        viewHolderComment.txtDate.setText(date);
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void setOnClickListener(ClickListener onClickListener){
        this.onClickListener = onClickListener;
    }


    class ViewHolderComment extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView igvAvatar;
        public TextView txtEmail;
        public TextView txtComment;
        public TextView txtDate;

        public ViewHolderComment(@NonNull View view) {
            super(view);

            igvAvatar = (ImageView) view.findViewById(R.id.igv_avatar);
            txtEmail = (TextView)view.findViewById(R.id.txtEmail);
            txtComment = (TextView)view.findViewById(R.id.txtComment);
            txtDate = view.findViewById(R.id.txtDate);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null)
                onClickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            if (onClickListener != null)
                onClickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }
}
