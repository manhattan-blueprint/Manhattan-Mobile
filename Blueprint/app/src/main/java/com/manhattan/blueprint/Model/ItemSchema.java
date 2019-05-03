package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ItemSchema {
    public enum ItemType {
        PrimaryResource,
        BlueprintCraftedMachine,
        MachineCraftedComponent,
        BlueprintCraftedComponent,
        Intangible,
    }

    public class Item {
        @SerializedName("item_id")
        private int itemID;

        @SerializedName("name")
        private String name;

        @SerializedName("type")
        private int type;

        public String getName() {
            return name;
        }

        public int getItemID() {
            return itemID;
        }

        public ItemType getItemType() {
            switch(type) {
                case 1: return ItemType.PrimaryResource;
                case 2: return ItemType.BlueprintCraftedMachine;
                case 3: return ItemType.MachineCraftedComponent;
                case 4: return ItemType.BlueprintCraftedComponent;
                default: return ItemType.Intangible;
            }
        }
    }

    @SerializedName("items")
    public ArrayList<Item> items;
}
