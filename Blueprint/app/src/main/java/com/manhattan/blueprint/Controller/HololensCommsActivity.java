package com.manhattan.blueprint.Controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.Consumer;
import com.manhattan.blueprint.Model.HololensClient;
import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HololensCommsActivity extends AppCompatActivity {
    private final String TAG = "HOLOLENS";
    private final int port = 9050;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hololens);
        this.statusText = findViewById(R.id.hololens_status);

        BlueprintDAO.getInstance(this).getSession().ifPresent(session -> startClient(session.hololensIP));
        updateStatus("Not Connected");
    }

    private void startClient(String ipAddress) {
        HololensClient client = new HololensClient("Hello blueprint");
        try {
            updateStatus("Connecting to server");
            client.setSocket(ipAddress, port);
            client.addItemToBuffer("I;0.0;0.5;3.5;Wood");
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        if (!client.isRunning()) return;
        updateStatus("Connected");

        int connectionRefreshDelay = 1;
        Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(() -> {
            try {
                client.update();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }, 0, connectionRefreshDelay, TimeUnit.SECONDS);
    }

    private void updateStatus(String status){
        this.statusText.setText(String.format("Status: %s", status));
    }
}
