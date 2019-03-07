package com.manhattan.blueprint.Controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.HololensClient;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.R;


public class HololensActivity extends AppCompatActivity {
    private TextView statusText;
    private int resourceId;
    private int resourceQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hololens);
        this.statusText = findViewById(R.id.hololens_status);
        updateStatus("Not Connected");
        BlueprintDAO.getInstance(this).getSession().ifPresent(session -> startClient(session.hololensIP));

        String jsonResource = (String) getIntent().getExtras().get("resource");
        Gson gson = new GsonBuilder().create();
        Resource resourceToCollect = gson.fromJson(jsonResource, Resource.class);
        resourceId = resourceToCollect.getId();
        resourceQuantity = resourceToCollect.getQuantity();
    }

    private void startClient(String ipAddress) {
        HololensClient client = new HololensClient();

        client.setSocket(ipAddress, 9050);
        if (ipAddress.isEmpty()) {
            updateStatus("Please go to settings and set the IP address.");
            return;
        }
        updateStatus("IP address was set to " + ipAddress);

        StringBuilder message = new StringBuilder();
        message.append("I;");
        message.append("000;");
        message.append("0000.00;0000.00;");
        message.append(resourceId);
        message.append(";");
        message.append(resourceQuantity);
        message.append(";");

        int connectionRefreshDelay = 5;
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(() -> {
            try {
                client.addItem(message.toString());
                Log.d("hololog", "Message created ->  " + message.toString());
                client.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, connectionRefreshDelay, TimeUnit.SECONDS);
    }

    private void updateStatus(String status) {
        statusText.setText("Status:" + status);
    }
}