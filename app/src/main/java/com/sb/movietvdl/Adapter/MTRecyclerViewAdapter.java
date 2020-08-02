package com.sb.movietvdl.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.StaticResource;
import com.sb.movietvdl.source.MovieTV;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.net.ContentHandler;
import java.util.List;

public class MTRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Constants
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    //Elements
    private Context mContext;
    private List<MovieTV> mData;
    private  OnRVClickListener onRVClickListener;

    private static final String TAG = "MTRecyclerViewAdapter";

    public MTRecyclerViewAdapter(Context mContext, List<MovieTV> data, OnRVClickListener onRVClickListener){
        this.mContext = mContext;
        this.mData = data;
        this.onRVClickListener = onRVClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LogUtil.l(TAG+"_onCreateViewHolder","viewType: " + viewType,false);
        if(viewType==VIEW_TYPE_ITEM) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            view = mInflater.inflate(R.layout.cardlayout_items, parent, false);
            return new MyViewHolder(view, onRVClickListener);
        }else{
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            view = mInflater.inflate(R.layout.cardlayout_item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){
            ((MyViewHolder) holder).tv_title.setText(mData.get(position).getTitle());
            try{
                Picasso.with(mContext)
                        .load(mData.get(position).getImgicon())
                        .into(((MyViewHolder) holder).iv_img);
            }catch(Exception e){
                LogUtil.e(TAG,"Image loading fail: " + mData.get(position).getImgicon(),true);
            }
        }else{
            showLoadingView((LoadingViewHolder) holder, position);
        }

    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_title;
        ImageView iv_img;
        ImageView iv_titlebg;
        OnRVClickListener onRVClickListener;
        public MyViewHolder(View itemView, OnRVClickListener onRVClickListener){
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            iv_img = (ImageView) itemView.findViewById(R.id.iv_img);
            iv_titlebg =(ImageView) itemView.findViewById(R.id.iv_titlebg);
            this.onRVClickListener = onRVClickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            onRVClickListener.OnRVClick(getAdapterPosition());
        }
    }

    public interface OnRVClickListener{
        void OnRVClick(int position);
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        TextView tvNoMore;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pg_loading_rv);
            tvNoMore = itemView.findViewById(R.id.tv_no_more);
        }
    }

}
