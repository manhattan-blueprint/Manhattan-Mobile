package com.manhattan.blueprint.View.ViewHolder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class WikiItemViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public ImageView image;

    public WikiItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.blueprintTemplateTitle);
        this.image = itemView.findViewById(R.id.blueprintTemplateImage);
    }

    public void configure(String name, Bitmap bitmap, View.OnClickListener onClickListener) {
        this.title.setText(name);
        this.image.setImageBitmap(bitmap);
        this.itemView.setOnClickListener(onClickListener);
    }
}
