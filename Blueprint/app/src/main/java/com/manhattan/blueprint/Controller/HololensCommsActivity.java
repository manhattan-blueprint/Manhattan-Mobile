package com.manhattan.blueprint.Controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.HololensClient;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.manhattan.blueprint.R;


public class HololensCommsActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hololens);
        this.statusText = findViewById(R.id.hololens_status);
        updateStatus("Not Connected");
        BlueprintDAO.getInstance(this).getSession().ifPresent(session -> startClient(session.hololensIP));
    }

    private void startClient(String ipAddress) {
        HololensClient client = new HololensClient();

        client.setSocket(ipAddress, 9050);
        if (ipAddress.isEmpty()) {
            updateStatus("Please go to settings and set the IP address.");
            return;
        }
        updateStatus("IP address was set to " + ipAddress);

        int connectionRefreshDelay = 5;
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(() -> {
            try {
                client.addItem("I;000;-0002.00;-0001.99;Wood;004");
                client.addItem("I;001;-0002.10;-0002.03;Wood;002");
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
