package com.morgat.eleone.adapters;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.morgat.eleone.models.HomeModel;
import com.morgat.eleone.R;
import com.morgat.eleone.models.DiscoverModel;
import com.morgat.eleone.utils.Variables;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.CustomViewHolder > implements Filterable {
    public Context context;

    ArrayList<DiscoverModel> datalist;
    ArrayList<DiscoverModel> datalist_filter;

    public interface OnItemClickListener {
        void onItemClick(ArrayList<HomeModel> video_list, int postion);
    }

    public DiscoverAdapter.OnItemClickListener listener;

    public DiscoverAdapter(Context context, ArrayList<DiscoverModel> arrayList, DiscoverAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        datalist_filter=arrayList;
        this.listener = listener;
    }


    @Override
    public DiscoverAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_discover_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        DiscoverAdapter.CustomViewHolder viewHolder = new DiscoverAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist_filter.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        RecyclerView horizontal_reycerview;

        TextView title;

        public CustomViewHolder(View view) {
            super(view);

            horizontal_reycerview = view.findViewById(R.id.horizontal_recylerview);
            title = view.findViewById(R.id.title);
        }


    }


    @Override
    public void onBindViewHolder(final DiscoverAdapter.CustomViewHolder holder, final int i) {

        DiscoverModel item = datalist_filter.get(i);

        holder.title.setText(item.title);

        Horizontal_Adapter adapter = new Horizontal_Adapter(context, item.arrayList);
        holder.horizontal_reycerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.horizontal_reycerview.setAdapter(adapter);


    }


    // that function will filter the result
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    datalist_filter = datalist;
                } else {
                    ArrayList<DiscoverModel> filteredList = new ArrayList<>();
                    for (DiscoverModel row : datalist) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.title.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    datalist_filter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = datalist_filter;
                return filterResults;

            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                datalist_filter = (ArrayList<DiscoverModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class Horizontal_Adapter extends RecyclerView.Adapter<Horizontal_Adapter.CustomViewHolder> {
        public Context context;

        ArrayList<HomeModel> datalist;


        public Horizontal_Adapter(Context context, ArrayList<HomeModel> arrayList) {
            this.context = context;
            datalist = arrayList;
        }

        @Override
        public Horizontal_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_discover_horizontal_layout, viewGroup, false);
            view.setLayoutParams(new RecyclerView.LayoutParams((Variables.screen_width/3)-20, RecyclerView.LayoutParams.WRAP_CONTENT));
            Horizontal_Adapter.CustomViewHolder viewHolder = new Horizontal_Adapter.CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {

            ImageView video_thumbnail;


            public CustomViewHolder(View view) {
                super(view);
                video_thumbnail = view.findViewById(R.id.video_thumbnail);

            }

            public void bind(final int pos, final ArrayList<HomeModel> datalist) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(datalist,pos);
                    }
                });
            }


        }

        @Override
        public void onBindViewHolder(final Horizontal_Adapter.CustomViewHolder holder, final int i) {
            holder.setIsRecyclable(false);

            try {
                HomeModel item = datalist.get(i);
                holder.bind(i, datalist);



                try {
                Glide.with(context)
                        .asGif()
                        .load(item.gif)
                        .skipMemoryCache(true)
                        .thumbnail(new RequestBuilder[]{Glide
                        .with(context)
                        .load(item.thum)})
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder)).centerCrop())
                        .into(holder.video_thumbnail);

            }catch (Exception e){

            }


            }catch (Exception e){

            }
        }

    }


}