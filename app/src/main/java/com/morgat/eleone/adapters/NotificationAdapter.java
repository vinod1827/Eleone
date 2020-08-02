package com.morgat.eleone.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.morgat.eleone.R;
import com.morgat.eleone.models.NotificationModel;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.CustomViewHolder > {
    public Context context;

    ArrayList<NotificationModel> datalist;
    public interface OnItemClickListener {
        void onItemClick(View view, int postion, NotificationModel item);
    }

    public NotificationAdapter.OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> arrayList, NotificationAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist= arrayList;
        this.listener=listener;
    }

    @Override
    public NotificationAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification,viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        NotificationAdapter.CustomViewHolder viewHolder = new NotificationAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done;

        public CustomViewHolder(View view) {
            super(view);
          //  image=view.findViewById(R.id.image);
            done=view.findViewById(R.id.done);

        }

        public void bind(final int pos , final NotificationModel item, final NotificationAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });


        }


    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        holder.bind(i,datalist.get(i),listener);

}

}