package com.manhattan.blueprint.Model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.ViewUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HololensClient {

    final private Context ctx;
    private String serverAddress;
    private int    port;
    private ArrayList<String> buffer;

    public HololensClient(Context ctx) {
        this.ctx = ctx;
        this.buffer = new ArrayList<>();
    }

    public void run(String ipAddress) {

        this.setSocket(ipAddress, 9050);
        if (ipAddress == null) {
            return;
        }

        int connectionRefreshDelay = 5;
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(() -> {
            try {
                this.sendBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, connectionRefreshDelay, TimeUnit.SECONDS);
    }

    public void addItem(int resourceId, int resourceQuantity, int counter) {
        String msg = this.buildMessage(resourceId, resourceQuantity, counter);
        this.buffer.add(msg);
    }

    private void setSocket(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port          = port;
    }

    private String sendAndRecv(String message) throws Exception {
        Socket clientSocket = new Socket(serverAddress, port);
        clientSocket.setSoTimeout(10000);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader  inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Log.d("hololog", "Sending... " + message);
        outToServer.writeBytes(message);
        outToServer.flush();
        message = inFromServer.readLine();
        Log.d("hololog","Received... " + message);
        clientSocket.close();
        return message;
    }

    private String buildMessage(int resId, int resQty, int ctr) {
        String ctrId  = "000" + ctr;
        String strId  = "000" + resId;
        String strQty = "00"  + resQty;

        return "I;" +
                (ctrId).substring(ctrId.length() - 3) +
                ";" +
                "00000.00;00004.00;" +
                (strId).substring(strId.length() - 2) +
                ";" +
                (strQty).substring(strQty.length() - 3);
    }

    private void sendBuffer() {
        Log.d("hololog", "New call of run");
        Log.d("hololog", "Buffer: " + this.buffer);

        int idx = 0;
        while (!buffer.isEmpty()) {
            Log.d("hololog", "\n");
            Log.d("hololog","Buffer size: " + buffer.size());
            Log.d("hololog","Sending item at index " + idx);
            String item = buffer.get(idx);
            String response = null;
            try {
                response = sendAndRecv(item);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response == null) {
                Log.d("hololog","No response!");
                // try again
            } else if (response.substring(0,3).equals(item.substring(0,3))) {
                int resourceId  = Integer.parseInt(response.substring(24,26));
                int resourceQty = Integer.parseInt(response.substring(27,30));

                BlueprintAPI api = new BlueprintAPI(ctx);
                InventoryItem itemCollected = new InventoryItem(resourceId, resourceQty);
                Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));
                api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
                    @Override
                    public void success(Void response) {
                        String itemName = ItemManager.getInstance(ctx).getName(resourceId).getWithDefault("items");
                        String successMsg = String.format("You collected %d %s. Well done!", resourceQty, itemName);
                        Toast.makeText(ctx, successMsg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(int code, String error) {
                        ViewUtils.createDialog(ctx, "Item collection failed", error,
                                (dialog, which) -> dialog.dismiss());
                    }
                });

                buffer.remove(item);
                buffer.trimToSize();
                if (buffer.size() > 0) {
                    idx = idx % buffer.size();
                }
            } else if (response.equals("Not Complete")) {
                idx = (idx + 1) % buffer.size();
            }
        }
    }
}