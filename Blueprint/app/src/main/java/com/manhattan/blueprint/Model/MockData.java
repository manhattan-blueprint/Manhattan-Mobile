package com.manhattan.blueprint.Model;

import java.util.ArrayList;
import java.util.Arrays;

public class MockData {
    private static InventoryItem itemA = new InventoryItem("Sand", 100);
    private static InventoryItem itemB = new InventoryItem("Metal", 300);
    private static InventoryItem itemC = new InventoryItem("String", 1);
    private static InventoryItem[] items = new InventoryItem[]{itemA, itemB, itemC};

    private static Resource resourceA = new Resource("Foo", new Location(51.456, -2.602));
    private static Resource resourceB = new Resource("Bar", new Location(51.457, -2.603));
    private static Resource resourceC = new Resource("Baz", new Location(51.458, -2.604));
    private static Resource[] resources = new Resource[]{resourceA, resourceB, resourceC};

    public static TokenPair tokenPair = new TokenPair("refreshingToMeetYou", "helloWorld");
    public static Inventory inventory = new Inventory(new ArrayList<>(Arrays.asList(items)));
    public static ResourceSet resourceSet = new ResourceSet(new ArrayList<>(Arrays.asList(resources)));

}
