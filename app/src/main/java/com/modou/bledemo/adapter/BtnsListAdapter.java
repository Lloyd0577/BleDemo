package com.modou.bledemo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.modou.bledemo.R;
import com.modou.bledemo.entity.CmdBtnEntity;
import com.modou.bledemo.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lloyd on 2017/11/14.
 */

public class BtnsListAdapter extends RecyclerView.Adapter {
    private LayoutInflater layoutInflater;
    private List<CmdBtnEntity> mData;
    private Context mContext;
    private OnItemClickListener listener;

    public BtnsListAdapter(Context context, List<CmdBtnEntity> data) {
        this.mContext = context;
        this.mData = data;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_btns_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        if (position == 3){
            viewHolder.btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_btns_bg_red));
        }else{
            viewHolder.btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_btns_bg));
        }
        if (position == 2){
            if (ConstantUtils.isReceiveMsg){
                viewHolder.btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_btns_bg_red));
                viewHolder.btn.setText("停止接受");
            }else{
                viewHolder.btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.shape_btns_bg_green));
                viewHolder.btn.setText("恢复接受");
            }
        }else{
            viewHolder.btn.setText(mData.get(position).getCmdName());
        }
        viewHolder.setPosition(position);
    }
    public List<CmdBtnEntity> getData(){
        if (mData == null){
            mData = new ArrayList<>();
        }
        return mData;
    }

    @Override
    public int getItemCount() {
        if (mData == null || mData.size() == 0){
            return 0;
        }
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        Button btn;
        protected int postion;
        public ViewHolder(View view){
            super(view);
            btn = (Button)view.findViewById(R.id.btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,postion);
                }
            });
        }
        public void setPosition(int position){
            this.postion = position;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.listener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int pos);
    }
}
