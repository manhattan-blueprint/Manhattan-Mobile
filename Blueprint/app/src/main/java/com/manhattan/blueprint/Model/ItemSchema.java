package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ItemSchema {
    public enum ItemType {
        PrimaryResource,
        BlueprintCraftedMachine,
        MachineCraftedComponent,
        BlueprintCraftedComponent,
        Intangible,
    }

    public class RecipeItem {
        @SerializedName("item_id")
        private int itemID;

        @SerializedName("quantity")
        private int quantity;

        public int getItemID() {
            return itemID;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public class Item {
        @SerializedName("item_id")
        private int itemID;

        @SerializedName("name")
        private String name;

        @SerializedName("type")
        private int type;

        @SerializedName("machine_id")
        private Integer machineID;

        @SerializedName("recipe")
        private List<RecipeItem> recipe;

        @SerializedName("blueprint")
        private List<RecipeItem> blueprint;

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

        public List<RecipeItem> getBlueprint() {
            return blueprint;
        }

        public List<RecipeItem> getRecipe() {
            return recipe;
        }

        public Integer getMachineID() {
            return machineID;
        }
    }

    @SerializedName("items")
    public ArrayList<Item> items;
}
