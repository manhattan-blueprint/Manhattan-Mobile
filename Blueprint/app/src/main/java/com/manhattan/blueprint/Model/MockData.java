package com.manhattan.blueprint.Model;

import java.util.ArrayList;
import java.util.Arrays;

public class MockData {
    public static InventoryItem itemA = new InventoryItem(0, 100);
    public static InventoryItem itemB = new InventoryItem(1, 300);
    public static InventoryItem itemC = new InventoryItem(2, 1);
    public static InventoryItem[] items = new InventoryItem[]{itemA, itemB, itemC};

    private static Resource resourceA = new Resource(8, new Location(51.456, -2.602));
    private static Resource resourceB = new Resource(1, new Location(51.457, -2.603));
    private static Resource resourceC = new Resource(2, new Location(51.458, -2.604));
    private static Resource resourceV = new Resource(3, new Location(51.450, -2.599));
    private static Resource resourceR = new Resource(4, new Location(51.450, -2.600));
    private static Resource resourceX = new Resource(5, new Location(51.449, -2.601));
    private static Resource resourceY = new Resource(6, new Location(51.451, -2.600));
    private static Resource resourceZ = new Resource(7, new Location(51.479, -2.628));
    private static Resource[] resources = new Resource[]{resourceA, resourceB, resourceC, resourceV, resourceR, resourceZ};

    public static TokenPair tokenPair = new TokenPair("refreshingToMeetYou", "helloWorld");
    public static TokenPair refreshTokenPair = new TokenPair("reallyRefreshingToMeetYou", "helloWorldAgain");
    public static Inventory inventory = new Inventory(new ArrayList<>(Arrays.asList(items)));
    public static ResourceSet resourceSet = new ResourceSet(new ArrayList<>(Arrays.asList(resources)));

}
