package com.manhattan.blueprint.Model.Managers;

import android.content.Context;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.Model.ItemSchema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemManager {
    private static ItemManager manager;
    private HashMap<Integer, ItemSchema.Item> itemMap;
    private BlueprintAPI api;

    public static ItemManager getInstance(Context context) {
        if (manager == null){
            manager = new ItemManager(context);
        }
        return manager;
    }

    private ItemManager(Context context) {
        this.itemMap = new HashMap<>();
        this.api = new BlueprintAPI(context);
    }

    public void fetchData(APICallback<Void> completion) {
       api.getSchema(new APICallback<ItemSchema>() {
           @Override
           public void success(ItemSchema response) {
               // Add to item map, removing electricity
               response.items.stream()
                       .filter(x -> x.getItemID() != 32)
                       .forEach(item -> itemMap.put(item.getItemID(), item));
               completion.success(null);
           }

           @Override
           public void failure(int code, String error) {
                completion.failure(code, error);
           }
       });
    }

    public Maybe<String> getName(int id) {
        return itemMap.containsKey(id) ? Maybe.of(itemMap.get(id).getName()) : Maybe.empty();
    }

    public Maybe<Integer> getId(String name) {
        for (Map.Entry<Integer, ItemSchema.Item> entry : itemMap.entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return Maybe.of(entry.getKey());
            }
        }
        return Maybe.empty();
    }

    public Collection<ItemSchema.Item> getItems() {
       return itemMap.values();
    }

    public Collection<String> getNames() {
        return itemMap.values().stream().map(ItemSchema.Item::getName).collect(Collectors.toList());
    }

}
