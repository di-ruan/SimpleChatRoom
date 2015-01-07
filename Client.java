/*
 * Copyright Di Ruan from Columbia University. All Rights Reserved.
 *
 * Date: 10/02/2014
 *
 * This class is responsible for creating the client socket, connecting to the server, sending commands
 * to server, reading instructions and displaying instructions from the server as well. It also creates
 * new thread for reading the user input.
 */

import java.io.*;
import java.net.*;

public class Client {

    private DataOutputStream dOut;
    private DataInputStream dIn;
    private Socket socket;
    private String IP;
    private int port;
    private boolean isConnected;
    private boolean isLogin;
    private boolean isInputtingPsw;

    /**
     * Constructor of Client *
     */
    public Client(String IP, int port) {
        this.IP = IP;
        this.port = port;
        isLogin = false;
        isConnected = false;
        isInputtingPsw = false;
    }

    /**
     * Create the socket and I/O stream *
     */
    public void init() {
        try {
            socket = new Socket(IP, port);
            display("connected to" + socket);

            isConnected = true;

            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());

            new ClientThread(this);
        } catch (Exception e) {
            display("Connection fails, please check the IP or port number");
        }
    }

    /**
     * Start reading message from server *
     */
    public void run() {
        try {
            while (isConnected) {
                String msg = dIn.readUTF();
                if (msg.equals("LOGIN_SUCCEED")) {
                    isLogin = true;
                } else if (msg.equals("DISCONNECTED")) {
                    isConnected = false;
                    display("You are logged out by the system. Goodbye!");
                    System.exit(0);
                } else if (!msg.isEmpty()) {
                    if (msg.startsWith(">")) {
                        displayInline(msg);
                    } else {
                        display(msg);
                    }
                }

                if (isLogin) {
                    System.out.print(">Command: ");
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Display the message in terminal *
     */
    public void display(String msg) {
        System.out.println("\n" + msg);
    }

    /**
     * For commands, we use print and leave space for user input *
     */
    public void displayInline(String msg) {
        if (msg.startsWith(">Password:")) {
            isInputtingPsw = true;
        } else {
            isInputtingPsw = false;
        }
        System.out.print(msg);
    }

    /**
     * Write message to Server *
     */
    public void sendMessage(String msg) {
        try {
            dOut.writeUTF(msg);
        } catch (Exception e) {
        }
    }

    /**
     * Constructor of ClientThread *
     */
    public void disconnect() {
        try {
            sendMessage("logout");
            isLogin = false;
            if (dIn != null) {
                dIn.close();
            }
            if (dOut != null) {
                dOut.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Get isLogin *
     */
    public boolean getIsLogin() {
        return isLogin;
    }

    /**
     * Set isLogin *
     */
    public void setIsLogin(boolean login) {
        isLogin = login;
    }

    /**
     * Set isConnected *
     */
    public void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Get isConnected *
     */
    public boolean getIsConnected() {
        return isConnected;
    }

    /**
     * Get isInputingPsw *
     */
    public boolean getIsInputingPsw() {
        return isInputtingPsw;
    }

    /**
     * Entry *
     */
    public static void main(String[] args) {

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("Invalid port number");
            return;
        }

        final Client client = new Client(args[0], port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("\nNotifying server and closing socket...");
                client.disconnect();
                System.out.println("Exit successfully!");
            }
        });

        client.init();
        client.run();
    }
}
