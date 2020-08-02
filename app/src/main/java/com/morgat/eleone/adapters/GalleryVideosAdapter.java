package com.morgat.eleone.adapters;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.morgat.eleone.R;
import com.morgat.eleone.models.GalleryVideoModel;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class GalleryVideosAdapter extends RecyclerView.Adapter<GalleryVideosAdapter.CustomViewHolder > {

    public Context context;
    private GalleryVideosAdapter.OnItemClickListener listener;
    private ArrayList<GalleryVideoModel> dataList;


      public interface OnItemClickListener {
        void onItemClick(int postion, GalleryVideoModel item, View view);
    }

    public GalleryVideosAdapter(Context context, ArrayList<GalleryVideoModel> dataList, GalleryVideosAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public GalleryVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_galleryvideo_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        GalleryVideosAdapter.CustomViewHolder viewHolder = new GalleryVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return dataList.size();
    }



    class CustomViewHolder extends RecyclerView.ViewHolder {


        ImageView thumb_image;

        TextView view_txt;

        public CustomViewHolder(View view) {
            super(view);

            thumb_image=view.findViewById(R.id.thumb_image);
            view_txt=view.findViewById(R.id.view_txt);

        }

        public void bind(final int position, final GalleryVideoModel item, final GalleryVideosAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position,item,v);
                }
            });

        }

    }




    @Override
    public void onBindViewHolder(final GalleryVideosAdapter.CustomViewHolder holder, final int i) {
        final GalleryVideoModel item= dataList.get(i);

        holder.view_txt.setText(item.video_time);

        Glide.with( context )
                .load(Uri.fromFile(new File(item.video_path)) )
                .into(holder.thumb_image);

        holder.bind(i,item,listener);

   }

}