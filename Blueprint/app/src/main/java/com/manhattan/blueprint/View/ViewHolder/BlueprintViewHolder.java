package com.manhattan.blueprint.View.ViewHolder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class BlueprintViewHolder extends RecyclerView.ViewHolder {
    private TextView title;
    private ImageView image;

    public BlueprintViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.blueprintTemplateTitle);
        this.image = itemView.findViewById(R.id.blueprintTemplateImage);
    }

    public void configure(String name, Bitmap bitmap) {
        this.title.setText(name);
        this.image.setImageBitmap(bitmap);
    }
}
