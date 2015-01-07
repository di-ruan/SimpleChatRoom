/*
 * Copyright Di Ruan from Columbia University. All Rights Reserved.
 *
 * Date: 10/02/2014
 *
 * This class is extended from Thread class and handles user input from the terminal.
 */

import java.util.Scanner;

public class ClientThread extends Thread {

    private Client client;

    /**
     * Constructor of ClientThread *
     */
    public ClientThread(Client client) {
        this.client = client;
        start();
    }

    /**
     * Start reading input from the user *
     */
    public void run() {
        try {
            getUserInput();
        } catch (Exception e) {
        }
    }

    /**
     * Read keyboard input and send the command *
     */
    public void getUserInput() {
        Scanner scan = new Scanner(System.in);
        try {
            while (client.getIsConnected()) {
                if (!client.getIsLogin()) {
                    String msg = "";
                    if (client.getIsInputingPsw()) {
                        char[] password = System.console().readPassword();
                        msg = new String(password);
                    } else {
                        msg = scan.nextLine();
                    }
                    client.sendMessage(msg);
                } else {
                    String msg = scan.nextLine();
                    if (msg.equals("logout")) {
                        client.sendMessage(msg);
                        client.setIsLogin(false);
                        client.setIsConnected(false);
                        break;
                    } else {
                        client.sendMessage(msg);
                    }
                }
                sleep(100);
            }
        } catch (Exception e) {
        }
    }
}
