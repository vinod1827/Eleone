package com.morgat.eleone.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.morgat.eleone.R;
import com.morgat.eleone.models.CommentModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CustomViewHolder > {

    public Context context;
    private CommentsAdapter.OnItemClickListener listener;
    private ArrayList<CommentModel> dataList;



    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, CommentModel item, View view);
    }

    public CommentsAdapter(Context context, ArrayList<CommentModel> dataList, CommentsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public CommentsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CommentsAdapter.CustomViewHolder viewHolder = new CommentsAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return dataList.size();
    }


    @Override
    public void onBindViewHolder(final CommentsAdapter.CustomViewHolder holder, final int i) {
        final CommentModel item= dataList.get(i);


        holder.username.setText(item.first_name+" "+item.last_name);

        try{
        Picasso.get().
                load(item.profile_pic)
                .resize(50,50)
                .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                .into(holder.user_pic);

       }catch (Exception e){

       }

        holder.message.setText(item.comments);


        holder.bind(i,item,listener);

   }



    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username,message;
        ImageView user_pic;


        public CustomViewHolder(View view) {
            super(view);

            username=view.findViewById(R.id.userNameTextView);
            user_pic=view.findViewById(R.id.user_pic);
            message=view.findViewById(R.id.message);

        }

        public void bind(final int postion, final CommentModel item, final CommentsAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion,item,v);
                }
            });

        }


    }





}