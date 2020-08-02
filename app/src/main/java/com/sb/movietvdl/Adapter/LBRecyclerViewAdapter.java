package com.sb.movietvdl.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.source.LinkButton;
import com.sb.movietvdl.source.MovieTV;
import com.sb.movietvdl.ui.Activities.DetailsActivity;

import java.util.List;

public class LBRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    //Elements
    private Context mContext;
    private List<LinkButton> mData;
    private OnLBClickListener onLBClickListener;
    //Constants
    private static final String TAG="LBRecyclerViewAdapter";

    public LBRecyclerViewAdapter(Context mContext, List<LinkButton> data, OnLBClickListener onLBClickListener){
        this.mContext = mContext;
        this.mData = data;
        this.onLBClickListener = onLBClickListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.linkbutton_item_layout, parent, false);
        return new MyViewHolder(view, onLBClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).tvEpLink.setText(mData.get(position).getEpname());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvEpLink;

        OnLBClickListener onLBClickListener;
        public MyViewHolder(View itemView, OnLBClickListener onLBClickListener){
            super(itemView);
            tvEpLink = (TextView) itemView.findViewById(R.id.tv_ep_link);

            this.onLBClickListener = onLBClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            LogUtil.l(TAG+"_click","Clicked Button",true);
            onLBClickListener.OnLBClick(getAdapterPosition());
        }
    }

    public interface OnLBClickListener{
        void OnLBClick(int position);
    }
}
