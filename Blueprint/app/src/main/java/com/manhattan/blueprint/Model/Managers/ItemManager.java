package com.manhattan.blueprint.Model.Managers;

import android.content.Context;

import com.manhattan.blueprint.Controller.MapViewActivity;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.Model.ItemSchema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemManager {
    private static ItemManager manager;
    private HashMap<Integer, String> itemMap;
    private HashMap<String, Integer> reverseItemMap;
    private BlueprintAPI api;

    public static ItemManager getInstance(Context context) {
        if (manager == null){
            manager = new ItemManager(context);
        }
        return manager;
    }

    private ItemManager(Context context) {
        this.itemMap = new HashMap<>();
        this.reverseItemMap = new HashMap<>();
        this.api = new BlueprintAPI(context);
    }

    public void fetchData(APICallback<Void> completion) {
       api.getSchema(new APICallback<ItemSchema>() {
           @Override
           public void success(ItemSchema response) {
               // Capitalize the first letter of each name
               response.items.forEach(item -> {
                  String firstCapitalized = item.getName().substring(0, 1).toUpperCase() + item.getName().substring(1);
                  itemMap.put(item.getItemID(), firstCapitalized);
                  reverseItemMap.put(firstCapitalized, item.getItemID());
               });
               completion.success(null);
           }

           @Override
           public void failure(int code, String error) {
                completion.failure(code, error);
           }
       });
    }

    public Maybe<String> getName(int id) {
        return itemMap.containsKey(id) ? Maybe.of(itemMap.get(id)) : Maybe.empty();
    }

    public Maybe<Integer> getId(String name) {
        return reverseItemMap.containsKey(name) ? Maybe.of(reverseItemMap.get(name)) : Maybe.empty();
    }

    public Collection<String> getNames() {
        return itemMap.values();
    }

}
