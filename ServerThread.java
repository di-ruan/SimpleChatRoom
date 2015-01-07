/*
 * Copyright Di Ruan from Columbia University. All Rights Reserved.
 *
 * Date: 10/02/2014
 *
 * This class is extended from Thread class and is in charge of the login. 3
 * The program will create a separate thread for each client connection to make
 * sure that the command from each connection can be processed in time.
 */

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {

    private String welcomeMsg = "Welcome to Di Ruan's chat server!\n";
    private String welcomeMsg_admin = "Hi, administrator, welcome back!\n";
    private String invalidCmdMsg = "invalid command, type 'help' for guide";

    private Server server;
    private Socket socket;
    private DataOutputStream dOut;
    private DataInputStream dIn;
    private String lastInputName;
    private String username;
    private boolean isLogin;
    private boolean isConnected;
    private int wrongTimes;
    private int lastCmdTime;
    private int id;

    /**
     * Constructor of ClientThread *
     */
    public ServerThread(Server server, Socket socket, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;
        wrongTimes = 0;
        lastCmdTime = server.getCurrentTime();
        isLogin = false;
        isConnected = true;
        username = "";
        lastInputName = "";

        try {
            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
        }
    }

    /**
     * Close the socket and I/O stream *
     */
    public void close() {
        try {

            if (dOut != null) {
                dOut.close();
            }

            if (dIn != null) {
                dIn.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Handle user login and different commands *
     */
    public void run() {
        try {

            while (isConnected) {
                if (!isLogin) {
                    login();
                } else {

                    String msg = dIn.readUTF();
                    lastCmdTime = server.getCurrentTime();

                    if (msg.equals("logout")) {
                        server.logout(id);
                        isLogin = false;
                        break;
                    } else if (msg.equals("whoelse")) {
                        server.whoelse(id);
                    } else if (msg.equals("wholasthr")) {
                        server.wholasthr(id);
                    } else if (msg.equals("help")) {
                        server.sendGuide(id);
                    } else if (msg.equals("template")) {
                        server.showTemplate(id);
                    } else if (msg.equals("expression")) {
                        server.showExpression(id);
                    } else if (msg.startsWith("broadcast")) {
                        try {
                            msg = msg.substring(10);
                            if (msg.equals("")) {
                                server.sendToOne(id, "message is empty");
                            } else {
                                msg = server.translate(msg);
                                msg = username + ": " + msg;
                                server.sendToAll(msg, id);
                            }
                        } catch (Exception e) {
                            server.sendToOne(id, invalidCmdMsg);
                        }
                    } else if (msg.startsWith("message")) {
                        try {
                            msg = msg.substring(8);
                            int index = msg.indexOf(' ');
                            String name = msg.substring(0, index);
                            String message = msg.substring(index + 1);

                            int send_id = server.getIDfromName(name);
                            if (send_id != -1) {
                                if (message.equals("")) {
                                    server.sendToOne(id, "message is empty");
                                } else {
                                    message = server.translate(message);
                                    message = username + ": " + message;
                                    server.sendToOne(send_id, message);
                                    server.sendToOne(id, "");
                                }
                            } else {
                                server.sendToOne(id, "no receiver");
                            }
                        } catch (Exception e) {
                            server.sendToOne(id, invalidCmdMsg);
                        }
                    } else if (msg.startsWith("kickout") && username.equals("admin")) {
                        msg = msg.substring(8);
                        server.sendToOne(id, "");
                        server.kickout(msg);
                    } else {
                        server.sendToOne(id, invalidCmdMsg);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            server.remove(id);
        }
    }

    /**
     * Handle login logic, check the username and password *
     */
    public void login() {
        try {
            String output = ">Username: ";
            writeMsg(output);
            String username = dIn.readUTF();

            if(username.equals("logout")) {
                server.logout(id);
                return;
            }

            int res = server.isUsnValid(username);
            if (res == 1) {
                output = ">Password: ";
                writeMsg(output);
                String password = dIn.readUTF();

                if(password.equals("logout")) {
                    server.logout(id);
                    return;
                }

                if (server.isPswValid(username, password)) {
                    wrongTimes = 0;
                    lastInputName = "";
                    this.username = username;
                    if (username.equals("admin")) {
                        writeMsg(">" + welcomeMsg_admin);
                    } else {
                        writeMsg(">" + welcomeMsg);
                    }
                    writeMsg("LOGIN_SUCCEED");
                    isLogin = true;
                    return;
                } else {
                    if (username.equals(lastInputName)) {
                        wrongTimes++;
                    } else {
                        wrongTimes = 1;
                        lastInputName = username;
                    }
                    writeMsg("Wrong password " + wrongTimes + " time");
                    if (wrongTimes == 3) {
                        writeMsg("Sorry, you will be blocked for 60 seconds");
                        server.blockUser(username);
                        server.logout(id);
                    }
                }
            } else if (res == -1) {
                writeMsg("user does not exist");
            } else if (res == -2) {
                writeMsg("Sorry, you are blocked");
            } else if (res == -3) {
                writeMsg("Sorry, you are already logged in");
            }
        } catch (Exception e) {
        }
        isLogin = false;
    }

    /**
     * Set inConnected to false when logout *
     */
    public void logout() {
        isConnected = false;
    }

    /**
     * Write the message to client *
     */
    public boolean writeMsg(String msg) {
        if (!socket.isConnected()) {
            close();
            return false;
        }
        try {
            dOut.writeUTF(msg);
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * Get username  *
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get thread ID *
     */
    public int getID() {
        return id;
    }

    /**
     * Get the last command time *
     */
    public int getLastCmdTime() {
        return lastCmdTime;
    }
}
