package com.manhattan.blueprint.Model;

/*
Implements the the Client API, designed for custom low level communication as
outlined in Hololens "Communication V1.png".
*/

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.net.Socket;



public class HololensClient {

    enum State {
        IDLE,    // - Device socket not set yet
        GREET,   // - Send greet message to Hololens until response,
                 // send connected message to Hololens once received.
        IDLE_IP, // - If there is anything in the buffer it tries to send
                 // it repeatedly until it gets a mirrored response.
    }

    private String greetMessage;
    private String serverAddress;
    private State state;
    private int port;
    private long delayedTime;

    // First item in the buffer is treated as the head
    ArrayList<String> buffer;

    // Sets up the class, defining the strings used for communication.
    public HololensClient(String greetMessage) {
        this.greetMessage = greetMessage;
        this.state = State.IDLE;
        buffer = new ArrayList<String>();

        // Having this here makes timing logic more concise.
        this.delayedTime = System.currentTimeMillis();
    }

    public void setSocket(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        Log.d("HOLOLENS","Socket set to (" + serverAddress + ", " + port + ")");
        this.state = State.IDLE_IP;
    }

    public Boolean isRunning() {
        if (state != State.IDLE) { return true; }
        return false;
    }

    public void addItemToBuffer(String Item) {
        buffer.add(Item);
        Log.d("HOLOLENS", "Item " + Item + " added to buffer");
    }

    public String sendAndRecv(String message) throws Exception {
        Socket clientSocket = new Socket(serverAddress, 9050);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String sentence = "hello_blueprint";
        Log.d("HOLOLENS", "Sending: " + message);
        outToServer.writeUTF(message);
        outToServer.flush();
        message = new String(inFromServer.readLine());
        Log.d("HOLOLENS","Received: " + message);
        clientSocket.close();
        return message;
    }

    public void update() throws Exception {
        long currentTime = System.currentTimeMillis();

        if (currentTime >= delayedTime) {
            switch(state) {
                case IDLE:
                    idle();
                    break;

                case GREET:
                    greet();
                    break;

                case IDLE_IP:
                    idleIP();
                    break;
            }
            delayedTime = System.currentTimeMillis() + 1000;
        }
    }

    public void idle() {
        // Nothing to do :)
    }

    public void greet() throws Exception {
        String response = sendAndRecv(greetMessage);
        if (greetMessage.equals(response)) {
            System.out.println("Greeting established! Swapping to state 'IDLE_IP'.");
            state = State.IDLE_IP;
        }
    }

    public void idleIP() throws Exception {
        if (buffer.size() > 0) { // No need to check anything if the buffer is empty
            String object = buffer.get(0);
            String response = sendAndRecv(object);

            // byte[] objB = object.getBytes();
            // byte[] resB = response.getBytes();
            // System.out.println("Checking string <" + object + "> equals <" + response + ">");
            // System.out.println("Checking bytes <" + objB + "> equals <" + resB + ">");

            // if (object.equals(response)) {
            Log.d("HOLOLENS","Object display acknowledged for " + object + "!");
            buffer.remove(object);
            // }
        }
    }
}