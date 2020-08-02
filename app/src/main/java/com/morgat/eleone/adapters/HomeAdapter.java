package com.morgat.eleone.adapters;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.morgat.eleone.R;
import com.morgat.eleone.models.HomeModel;
import com.morgat.eleone.utils.Variables;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.CustomViewHolder> {

    public Context context;
    private HomeAdapter.OnItemClickListener listener;
    private ArrayList<HomeModel> dataList;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, HomeModel item, View view);
    }


    public HomeAdapter(Context context, ArrayList<HomeModel> dataList, HomeAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public HomeAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_home_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        HomeAdapter.CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final HomeAdapter.CustomViewHolder holder, final int i) {
        final HomeModel item = dataList.get(i);
        if (Variables.IS_DEBUG)
        System.out.println("## "+item.toString());
        holder.setIsRecyclable(false);

        try {

            holder.bind(i, item, listener);

            holder.username.setText(item.first_name + " " + item.last_name);


            if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                holder.sound_name.setText("original sound - " + item.first_name + " " + item.last_name);
            } else {
                holder.sound_name.setText(item.sound_name);
            }
            holder.sound_name.setSelected(true);


            holder.desc_txt.setText(item.video_description);

            Picasso.get().
                    load(item.profile_pic)
                    .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                    .resize(100, 100).into(holder.user_pic);


            if ((item.sound_name == null || item.sound_name.equals(""))
                    || item.sound_name.equals("null")) {

                item.sound_pic = item.profile_pic;

            } else if (item.sound_pic.equals(""))
                item.sound_pic = "Null";


            Picasso.get().
                    load(item.sound_pic)
                    .placeholder(context.getResources().getDrawable(R.drawable.ic_round_music))
                    .resize(100, 100).into(holder.sound_image);


            Drawable likeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_like);
            if (item.liked.equals("1")) {
                likeDrawable.setTint(ContextCompat.getColor(context, R.color.redcolor));
                holder.like_image.setImageDrawable(likeDrawable);
            } else {
                likeDrawable.setTint(ContextCompat.getColor(context, R.color.white));
                holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like));
            }


            holder.like_txt.setText(item.like_count);
            holder.comment_txt.setText(item.video_comment_count);


        } catch (Exception e) {

        }
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {

        //  PlayerView playerview;
        TextView username, desc_txt, sound_name;
        ImageView user_pic, sound_image;

        LinearLayout like_layout, comment_layout, shared_layout, sound_image_layout;
        ImageView like_image, comment_image;
        TextView like_txt, comment_txt;


        public CustomViewHolder(View view) {
            super(view);

            // playerview=view.findViewById(R.id.playerview);

            username = view.findViewById(R.id.userNameTextView);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.sound_name);
            sound_image = view.findViewById(R.id.sound_image);

            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.like_image);
            like_txt = view.findViewById(R.id.like_txt);


            desc_txt = view.findViewById(R.id.desc_txt);

            comment_layout = view.findViewById(R.id.comment_layout);
            comment_image = view.findViewById(R.id.comment_image);
            comment_txt = view.findViewById(R.id.comment_txt);


            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            shared_layout = view.findViewById(R.id.shared_layout);
        }

        public void bind(final int postion, final HomeModel item, final HomeAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


            user_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });


            like_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });


            comment_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            shared_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            sound_image_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


        }


    }


}