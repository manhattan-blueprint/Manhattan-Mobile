package com.manhattan.blueprint.View.ViewHolder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class RecipeItemViewHolder extends RecyclerView.ViewHolder {
    private TextView name;
    private TextView quantity;
    private ImageView image;

    public RecipeItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.name = itemView.findViewById(R.id.recipeItemTemplateName);
        this.quantity = itemView.findViewById(R.id.recipeItemTemplateQuantity);
        this.image = itemView.findViewById(R.id.recipeItemTemplateImage);
    }

    public void configure(String name, int quantity, Bitmap bitmap) {
        this.name.setText(name);
        this.quantity.setText(String.format("Quantity: %d", quantity));
        this.image.setImageBitmap(bitmap);
    }
}
