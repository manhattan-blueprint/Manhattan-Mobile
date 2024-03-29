package com.manhattan.blueprint.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.Utils.ViewUtils;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BackpackView {

    public interface BackpackDelegate {
        void didTapBackpackItem(int itemID, int quantity);
    }

    private Context context;
    private float padding = 0.15f;
    private float cellHeight;
    private float cellWidth;
    private float centerX;
    private float centerY;

    private ArrayList<AnimatableLayout> layerOne;
    private ArrayList<AnimatableLayout> layerTwo;
    private ArrayList<View> popups;
    private BackpackDelegate delegate;

    public BackpackView(Context context, ViewGroup viewGroup, BackpackDelegate delegate){
        // 1.13 for the perfect hexagon ratio 👌
        this.cellWidth = ViewUtils.dpToPx(context, 68);
        this.cellHeight = 1.13f * cellWidth;
        this.centerX = viewGroup.getWidth() / 2 - cellWidth / 2;
        this.centerY = viewGroup.getHeight() / 2 - cellHeight / 2;
        this.context = context;
        this.popups = new ArrayList<>();
        this.delegate = delegate;

        // First layer
        AnimatableLayout layerOneLeft = createHex(viewGroup, centerX - (cellWidth * (1 + padding)), centerY);
        AnimatableLayout layerOneRight = createHex(viewGroup, centerX + (cellWidth * (1 + padding)), centerY);
        AnimatableLayout layerOneTopLeft = createHex(viewGroup, centerX - (cellWidth * (1 + padding) / 2), centerY - cellWidth);
        AnimatableLayout layerOneTopRight = createHex(viewGroup, centerX + (cellWidth * (1 + padding) / 2), centerY - cellWidth);
        AnimatableLayout layerOneBottomLeft = createHex(viewGroup, centerX - (cellWidth * (1 + padding) / 2), centerY + cellWidth);
        AnimatableLayout layerOneBottomRight = createHex(viewGroup, centerX + (cellWidth * (1 + padding) / 2), centerY + cellWidth);

        // Second Layer
        AnimatableLayout layerTwoTop = createHex(viewGroup, centerX, centerY - (2 * cellWidth));
        AnimatableLayout layerTwoBottom = createHex(viewGroup, centerX, centerY + (2 * cellWidth));
        AnimatableLayout layerTwoTopLeft = createHex(viewGroup, centerX - 1.5f * (cellWidth * (1 + padding)), centerY - cellWidth);
        AnimatableLayout layerTwoTopRight = createHex(viewGroup, centerX + 1.5f * (cellWidth * (1 + padding)), centerY - cellWidth);
        AnimatableLayout layerTwoBottomLeft = createHex(viewGroup, centerX - 1.5f * (cellWidth * (1 + padding)), centerY + cellWidth);
        AnimatableLayout layerTwoBottomRight = createHex(viewGroup, centerX + 1.5f * (cellWidth * (1 + padding)), centerY + cellWidth);

        // Layer in order we want to animate
        layerOne = new ArrayList<>(Arrays.asList(
                layerOneTopLeft, layerOneTopRight, layerOneRight,
                layerOneBottomRight, layerOneBottomLeft, layerOneLeft));

        layerTwo = new ArrayList<>(Arrays.asList(
                layerTwoTopLeft, layerTwoTop, layerTwoTopRight,
                layerTwoBottomRight, layerTwoBottom, layerTwoBottomLeft));

        layerOne.forEach(viewGroup::addView);
        layerTwo.forEach(viewGroup::addView);
        viewGroup.setClipChildren(false);
    }


    private AnimatableLayout createHex(ViewGroup viewGroup, float x, float y){
        LayoutInflater inflater = LayoutInflater.from(context);
        AnimatableLayout view = (AnimatableLayout) inflater.inflate(R.layout.inventory_item, viewGroup, false);
        view.setDestination(x, y);
        view.setLayoutParams(new LinearLayout.LayoutParams((int) cellWidth, (int) cellHeight));
        view.setX(centerX);
        view.setY(centerY);
        view.setAlpha(0);
        view.setClickable(true);
        view.requestLayout();
        return view;
    }

    public void animate(long delay){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                layerOne.forEach(v -> v.setAlpha(1));
                layerTwo.forEach(v -> v.setAlpha(1));
            }
        }, delay);

        long duration = 100;
        double overlap = 0.6;
        for (int i = 0; i < layerOne.size(); i++) {
            AnimatableLayout v = layerOne.get(i);
            v.animate()
                .x(v.getDestX())
                .y(v.getDestY())
                .setDuration(duration)
                .setInterpolator(new LinearInterpolator())
                .setStartDelay((long) (delay + i * (duration * overlap)));
        }

        for (int i = 0; i < layerTwo.size(); i++) {
            AnimatableLayout v = layerTwo.get(i);
            v.animate()
                .x(v.getDestX())
                .y(v.getDestY())
                .setDuration((long) (duration * 1.2))
                .setInterpolator(new LinearInterpolator())
                .setStartDelay((long) (delay + (i + layerOne.size()) * (duration * overlap)));
        }
    }

    public void update(Inventory inventory) {
        // Clear existing
        new Thread(() -> {
            layerOne.forEach(v -> {
                TextView quantity = v.findViewById(R.id.inventory_item_quantity);
                ImageView imageView = v.findViewById(R.id.inventory_item_image);
                quantity.post(() -> quantity.setText(""));
                imageView.post(() -> imageView.setImageBitmap(null));
            });
            layerTwo.forEach(v -> {
                TextView quantity = v.findViewById(R.id.inventory_item_quantity);
                ImageView imageView = v.findViewById(R.id.inventory_item_image);
                quantity.post(() -> quantity.setText(""));
                imageView.post(() -> imageView.setImageBitmap(null));
            });

            ArrayList<InventoryItem> items = inventory.getItems();
            for(int i = 0; i < items.size(); i++){
                InventoryItem item = items.get(i);
                AnimatableLayout layout;

                if (i < layerOne.size()) {
                    // Put in layer 1
                    layout = layerOne.get(i);
                } else if (i >= layerOne.size() && i < layerOne.size() + layerTwo.size()) {
                    // Put in layer 2
                    layout = layerTwo.get(i - layerOne.size());
                } else {
                    // Something has gone very wrong!
                    return;
                }

                TextView quantityText = layout.findViewById(R.id.inventory_item_quantity);
                ImageView imageView = layout.findViewById(R.id.inventory_item_image);
                quantityText.post(() -> quantityText.setText(String.valueOf(item.getQuantity())));
                imageView.post(() -> {
                    imageView.setImageBitmap(SpriteManager.getInstance(context).fetch(item.getId()));
                    imageView.setOnClickListener(v -> delegate.didTapBackpackItem(item.getId(), item.getQuantity()));
                });
            }

        }).start();
    }

    public void jumpToEndPosition() {
        layerOne.forEach(b -> b.animate().x(b.getDestX()).y(b.getDestY()).setStartDelay(0).setDuration(0));
        layerTwo.forEach(b -> b.animate().x(b.getDestX()).y(b.getDestY()).setStartDelay(0).setDuration(0));
    }

    public void hide(long duration) {
        popups.forEach(p -> ((ViewGroup)p.getParent()).removeView(p));
        popups.clear();

        layerOne.forEach( v -> {
            v.animate()
                    .x(centerX)
                    .y(centerY)
                    .setDuration(duration)
                    .setStartDelay(0)
                    .setInterpolator(new AnticipateInterpolator());
        });
        layerTwo.forEach( v -> {
            v.animate()
                    .x(centerX)
                    .y(centerY)
                    .setDuration(duration)
                    .setStartDelay(0)
                    .setInterpolator(new AnticipateInterpolator());
        });
    }

    public void remove(){
        layerOne.forEach(v -> v.setAlpha(0));
        layerTwo.forEach(v -> v.setAlpha(0));
    }
}
