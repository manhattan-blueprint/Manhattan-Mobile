package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ItemSchema {
    public class Item {
        @SerializedName("item_id")
        private int itemID;

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public int getItemID() {
            return itemID;
        }
    }

    @SerializedName("items")
    public ArrayList<Item> items;
}
