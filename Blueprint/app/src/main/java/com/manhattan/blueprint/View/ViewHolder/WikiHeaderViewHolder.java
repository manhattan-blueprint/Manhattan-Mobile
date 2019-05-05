package com.manhattan.blueprint.View.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class WikiHeaderViewHolder extends RecyclerView.ViewHolder {
    private TextView title;

    public WikiHeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.wikiHeaderTemplateTitle);
    }

    public void configure(String name) {
        this.title.setText(name);
    }
}
