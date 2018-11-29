package com.manhattan.blueprint.Model;

import java.util.ArrayList;
import java.util.Arrays;

public class MockData {
    public static InventoryItem   itemA = new InventoryItem("Sand",  100);
    public static InventoryItem   itemB = new InventoryItem("Metal", 300);
    public static InventoryItem   itemC = new InventoryItem("String",1);
    public static InventoryItem[] items = new InventoryItem[]{itemA, itemB, itemC};

    private static Resource resourceA = new Resource("Sand",  new Location(51.456, -2.602));
    private static Resource resourceB = new Resource("Metal", new Location(51.457, -2.603));
    private static Resource resourceC = new Resource("Glass", new Location(51.458, -2.604));
    private static Resource resourceV = new Resource("Wood",  new Location(51.450, -2.599));
    private static Resource resourceR = new Resource("Coal",  new Location(51.450, -2.600));
    private static Resource resourceX = new Resource("String",  new Location(51.449, -2.601));
    private static Resource resourceY = new Resource("Kryptonite",  new Location(51.451, -2.600));
    private static Resource[] resources = new Resource[]{resourceA, resourceB, resourceC, resourceV, resourceR};

    public static TokenPair tokenPair = new TokenPair("refreshingToMeetYou", "helloWorld");
    public static Inventory inventory = new Inventory(new ArrayList<>(Arrays.asList(items)));
    public static ResourceSet resourceSet = new ResourceSet(new ArrayList<>(Arrays.asList(resources)));

}
