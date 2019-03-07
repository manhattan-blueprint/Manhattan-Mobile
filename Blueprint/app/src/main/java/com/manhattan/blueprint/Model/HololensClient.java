package com.manhattan.blueprint.Model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.net.Socket;


public class HololensClient {

    private String serverAddress;
    private int    port;
    private ArrayList<String> buffer;

    public HololensClient() {
        buffer = new ArrayList<>();
    }

    public void setSocket(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port          = port;
    }

    public void addItem(String Item) {
        buffer.add(Item);
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

    public void run() {
        Log.d("hololog", "New instance of run");
        if (buffer.size() > 0) {
            sendBuffer();
        }
    }

    private void sendBuffer() {
        int idx = 0;
        do {
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
            } else if (response.equals(item)) {
                buffer.remove(item);
                buffer.trimToSize();
                idx = idx % buffer.size();
            } else if (response.equals("Not Complete")) {
                idx = (idx + 1) % buffer.size();
            }
        } while (!buffer.isEmpty());
    }
}