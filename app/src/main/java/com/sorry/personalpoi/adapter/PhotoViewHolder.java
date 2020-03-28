package com.sorry.personalpoi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sorry.personalpoi.R;
import com.sorry.personalpoi.view.SquareImg;


/**
 * @anthor sorry
 * @time 2019/5/17
 * @class describe
 */
public class PhotoViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout card;
    public SquareImg img;
    public TextView tvTag;
    public CheckBox cbPhoto;
    public SquareImg ivGray;

    public PhotoViewHolder(View itemView) {
        super(itemView);
        card = (RelativeLayout) itemView.findViewById(R.id.rl_photo);
        img = (SquareImg) itemView.findViewById(R.id.iv_photo);
        tvTag = (TextView) itemView.findViewById(R.id.tv_imgtag);
        cbPhoto = (CheckBox) itemView.findViewById(R.id.cb_photo);
        ivGray = (SquareImg) itemView.findViewById(R.id.iv_gray);

    }
}
