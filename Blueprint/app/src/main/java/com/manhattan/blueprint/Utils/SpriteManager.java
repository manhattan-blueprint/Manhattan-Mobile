package com.manhattan.blueprint.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.R;

import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static int MAX_ID = 32;
    private static SpriteManager instance;
    private Bitmap defaultSprite;
    private Bitmap defaultScaledSprite;
    private int scaledSpriteSize = 150;
    private HashMap<Integer, Bitmap> spriteMap;
    private HashMap<Integer, Bitmap> mapSpriteMap;

    private SpriteManager(Context context) {
        spriteMap = new HashMap<>();
        mapSpriteMap = new HashMap<>();
        defaultSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.sprite_default);
        defaultScaledSprite = Bitmap.createScaledBitmap(defaultSprite, scaledSpriteSize, scaledSpriteSize, false);

        // Scale to 1/4 of the size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        for (int itemID = 0; itemID < MAX_ID; itemID++){
            Maybe<Integer> resourceID = getResourceID(context, "sprite_" + itemID);
            if (resourceID.isPresent()) {
                spriteMap.put(itemID, BitmapFactory.decodeResource(context.getResources(), resourceID.get(), options));
            }

            Maybe<Integer> mapResourceID = getResourceID(context, "map_sprite_" + itemID);
            if (mapResourceID.isPresent()) {
                mapSpriteMap.put(itemID, BitmapFactory.decodeResource(context.getResources(), mapResourceID.get(), options));
            }
        }
    }

    public static SpriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new SpriteManager(context);
        }
        return instance;
    }

    public Bitmap fetch(int itemID) {
        if (spriteMap.containsKey(itemID)) {
            return spriteMap.get(itemID);
        }
        return defaultSprite;
    }

    // Map Sprites could be different, and need to be scaled
    public Bitmap fetchMapSprite(int itemID) {
        if (mapSpriteMap.containsKey(itemID)) {
            return Bitmap.createScaledBitmap(mapSpriteMap.get(itemID), scaledSpriteSize, scaledSpriteSize, false);
        } else if (spriteMap.containsKey(itemID)) {
            return Bitmap.createScaledBitmap(spriteMap.get(itemID), scaledSpriteSize, scaledSpriteSize, false);
        }
        return defaultScaledSprite;
    }

    private static Maybe<Integer> getResourceID(Context context, String resName) {
        try {
            int identifier = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
            return identifier == 0 ? Maybe.empty() : Maybe.of(identifier);
        } catch (Exception e) {
            return Maybe.empty();
        }
    }

    public static HashMap<Integer, String> addModels() {
        HashMap<Integer, String> modelsMap = new HashMap<>();
        modelsMap.put(1,  "model_1.sfb");
        modelsMap.put(2,  "model_2.sfb");
        modelsMap.put(3,  "model_3.sfb");
        modelsMap.put(4,  "model_4.sfb");
        modelsMap.put(5,  "model_5.sfb");
        modelsMap.put(6,  "model_6.sfb");
        modelsMap.put(7,  "model_7.sfb");
        modelsMap.put(8,  "model_8.sfb");
        modelsMap.put(9,  "model_9.sfb");
        modelsMap.put(10, "model_10.sfb");
        return modelsMap;
    }
}
