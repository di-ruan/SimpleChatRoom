/*
 * Copyright Di Ruan from Columbia University. All Rights Reserved.
 *
 * Date: 10/02/2014
 *
 * This class is responsible for creating the server socket, managing
 * the server threads, implementing the logic of the commands, providing
 * some helper functions and sending instructions to clients.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private String path = "user_pass.txt";
    private int TIME_OUT = 30 * 60;
    private int LAST_HOUR = 60 * 60;
    private int BLOCK_TIME = 60;

    private Map<String, String> userMap;
    private Map<String, Integer> historyMap;
    private Map<String, Integer> lastCmdMap;
    private Map<String, Integer> blockMap;
    private Map<Integer, ServerThread> threadMap;
    private ServerSocket serverSocket;
    private boolean isServerOn;
    private static int UID;
    private int port;

    private String[] expression = {":-)", ":-(", "O(^_^)O", "('_')", "^_^",
            "(>_<)", "(^_-)", "(T_T)", "(-_-)zzz"};

    private String[] sentences = {
            "Good morning, everyone!",
            "Nice to meet you!",
            "Sorry, I have to go. Bye!",
            "I will miss you!",
            "This chat room is so cool!"
    };

    /**
     * Constructor of Server *
     */
    public Server(int port) {
        userMap = new HashMap<String, String>();
        historyMap = new HashMap<String, Integer>();
        lastCmdMap = new HashMap<String, Integer>();
        blockMap = new HashMap<String, Integer>();
        threadMap = new HashMap<Integer, ServerThread>();
        this.port = port;
        isServerOn = false;
        UID = 1;
    }

    /**
     * Read current User Name and Password in the config file *
     */
    public void readUserInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();

            while (line != null) {
                int pos = line.indexOf(' ');
                String username = line.substring(0, pos);
                String password = line.substring(pos + 1);
                userMap.put(username, password);
                line = reader.readLine();
            }
        } catch (Exception e) {
            System.out.println("Cannot find user record");
            System.exit(0);
        }
    }

    /**
     * Read current User Name and Password in the config file
     * return -1 if user doesn't exist
     * return -2 if user is blocked
     * return -3 if user in currently online
     * return 1 if user is allowed to login *
     */
    public int isUsnValid(String username) {
        if (!userMap.containsKey(username)) {
            return -1;
        } else if (blockMap.containsKey(username)) {
            Integer time = getCurrentTime();
            if (time - blockMap.get(username) <= BLOCK_TIME) {
                return -2;
            } else {
                blockMap.remove(username);
            }
        } else {
            for (ServerThread th : threadMap.values()) {
                if (th.getUsername().equals(username)) {
                    return -3;
                }
            }
        }
        return 1;
    }

    /**
     * Put a user in a blockMap and record the time *
     */
    public void blockUser(String username) {
        Integer time = getCurrentTime();
        blockMap.put(username, time);
    }

    /**
     * Decide if the username and password are in the config file *
     */
    public boolean isPswValid(String username, String password) {
        if (!userMap.containsKey(username)) {
            return false;
        } else {
            return (userMap.get(username).equals(password));
        }
    }

    /**
     * Handle "whoelse" command, send the user the list of names online except himself *
     */
    public void whoelse(int id) {
        String msg = "";
        for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
            if (pair.getKey() != id) {
                ServerThread th = pair.getValue();
                msg += th.getUsername() + "\n";
            }
        }
        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * Handle "wholasthr" command, send the user the list of names
     * online during last hour except himself *
     */
    public void wholasthr(int id) {
        String msg = "";
        String username = "";
        for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
            if (pair.getKey() == id) {
                username = pair.getValue().getUsername();
            }
            else {
                ServerThread th = pair.getValue();
                msg += th.getUsername() + "\n";
            }
        }
        int currentTime = getCurrentTime();
        for (String name : historyMap.keySet()) {
            int time = historyMap.get(name);
            if (!name.equals(username) && currentTime - time <= LAST_HOUR) {
                msg += name + "\n";
            } else {
                historyMap.remove(name);
            }
        }

        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * Send the message to all the user except the sender himself *
     */
    public synchronized void sendToAll(String msg, int id) {
        String message = "";
        for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
            ServerThread th = pair.getValue();
            if (id == pair.getKey()) {
                message = "";
            } else {
                message = msg;
            }

            if (!th.writeMsg(message)) {
                remove(pair.getKey());
            }
        }
    }

    /**
     * Send the message to a specific user with id *
     */
    public void sendToOne(int id, String msg) {
        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * When user types "help", send them the list of commands *
     */
    public void sendGuide(int id) {
        String msg = "The chat room of Di Ruan, version 1.0.\n\n";
        msg += "Here is a list of commands that you can use.\n";
        msg += "1. whoelse                      --Display other connected users\n";
        msg += "2. wholasthr                    --Display users connected within last hour\n";
        msg += "3. broadcast <message>          --Send <message> to all other connected users\n";
        msg += "4. message <user> <message>     --Send private <message> to a <user>\n";
        msg += "5. logout                       --Log out current user\n";
        msg += "6. template                     --Show all template sentences\n";
        msg += "                                --Replace <message> by <T+number>\n";
        msg += "                                --For example, broadcast T1\n";
        msg += "7. expression                   --Show all expressions\n";
        msg += "                                --Replace <message> by <E+number>\n";
        msg += "                                --For example, broadcast E1\n";

        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * When user types "template", send them the list of commands *
     */
    public void showTemplate(int id) {
        String msg = "Here is a list of template sentences that you can use.\n\n";
        msg += "In order to use, you can replace <message> by <T+number>\n";
        msg += "For example, you can type \"broadcast T1\"\n\n";
        msg += "1. Good morning, everyone!\n";
        msg += "2. Nice to meet you!\n";
        msg += "3. Sorry, I have to go. Bye!\n";
        msg += "4. I will miss you!\n";
        msg += "5. This chat room is so cool!\n";
        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * When user types "expression", send them the list of commands *
     */
    public void showExpression(int id) {
        String msg = "Here is a list of template expressions that you can use.\n\n";
        msg += "In order to use, you can replace <message> by <E+number>\n";
        msg += "For example, you can type \"broadcast E1\"\n\n";
        msg += "1. :-)\n";
        msg += "2. :-(\n";
        msg += "3. O(^_^)O\n";
        msg += "4. ('_')\n";
        msg += "5. ^_^\n";
        msg += "6. (>_<)\n";
        msg += "7. (^_-)\n";
        msg += "8. (T_T)\n";
        msg += "9. (-_-)zzz\n";
        ServerThread th = threadMap.get(id);
        if (!th.writeMsg(msg)) {
            remove(id);
        }
    }

    /**
     * Only admin user can kick an user out by using this command *
     */
    public void kickout(String name) {
        int id = getIDfromName(name);
        if(id != -1) {
            logout(id);
        }
    }

    /**
     * Get the user thread id from the username *
     */
    public int getIDfromName(String name) {
        for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
            ServerThread th = pair.getValue();
            if (th.getUsername().equals(name)) {
                return pair.getKey();
            }
        }
        return -1;
    }

    /**
     * handle logout command, save logout time in historyMap *
     */
    public void logout(int id) {
        sendToOne(id, "DISCONNECTED");
        showDisconnection(id);
        ServerThread th = threadMap.get(id);
        if(th != null) {
            historyMap.put(threadMap.get(id).getUsername(), getCurrentTime());
            remove(id);
        }
    }

    /**
     * If user use expression or templates, then we need to translate
     * them into the corresponding sentences  *
     */
    public String translate(String msg) {
        if (msg.length() == 2) {
            if (msg.charAt(0) == 'E') {
                if (msg.charAt(1) >= '1' && msg.charAt(1) <= '9') {
                    int index = msg.charAt(1) - '1';
                    msg = expression[index];
                }
            } else if (msg.charAt(0) == 'T') {
                if (msg.charAt(1) >= '1' && msg.charAt(1) <= '5') {
                    int index = msg.charAt(1) - '1';
                    msg = sentences[index];
                }
            }
        }
        return msg;
    }

    /**
     * Display the message in a new line *
     */
    public void display(String msg) {
        System.out.println(msg);
    }

    /**
     * Show the disconnection message on Terminal *
     */
    public void showDisconnection(int id) {
        System.out.println("Connection " + id + " is disconnected");
    }

    /**
     * stop the server and close the sockets *
     */
    public void stopServer() {
        try {
            isServerOn = false;

            for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
                try {
                    ServerThread th = pair.getValue();
                    if(th != null) {
                        logout(pair.getKey());
                        th.logout();
                        th.close();
                    }
                } catch (Exception e) {
                }
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("All sockets are closed");
            }
        } catch (Exception e) {
        }
    }

    /**
     * Remove the thread of this id from the thread list *
     */
    public void remove(int id) {
        ServerThread th = threadMap.get(id);
        if (th != null) {
            threadMap.remove(id);
        }
    }

    /**
     * Return the current time in second *
     */
    public int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * Check if the last command from this happened within the timeout duration
     * return true if it is more than the timeout duration *
     */
    public void checkTimeOut() {
        for (Map.Entry<Integer, ServerThread> pair : threadMap.entrySet()) {
            ServerThread th = pair.getValue();
            if (th != null) {
                if (getCurrentTime() - th.getLastCmdTime() >= TIME_OUT) {
                    logout(pair.getKey());
                    th.logout();
                }
            }
        }
    }

    /**
     * Initialize the server, create the socket and read user information *
     */
    public void init() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("connect to port " + port + ", waiting for the clients...");
            isServerOn = true;
            readUserInfo();
        } catch (IOException e) {
            System.out.println("The connection cannot be established");
        }
    }

    /**
     * Start *
     */
    public void run() {
        Runnable checkRunnable = new Runnable() {
            public void run() {
                checkTimeOut();
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(checkRunnable, 0, 10, TimeUnit.SECONDS);

        while (isServerOn) {
            try {
                Socket newSocket = serverSocket.accept();

                if (!isServerOn) break;

                ServerThread th = new ServerThread(this, newSocket, UID++);
                threadMap.put(th.getID(), th);
                System.out.println("Connection " + th.getID() + " from " + newSocket);
                th.start();
            } catch (Exception e) {
            }
        }
        try {
            stopServer();
        } catch (Exception e) {
        }
    }

    /**
     * Entry *
     */
    public static void main(String[] args) throws Exception {
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Invalid port number");
            return;
        }

        final Server srv = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("\nClosing server, closing socket and notifying clients...");
                srv.stopServer();
                System.out.println("Exit successfully!");
            }
        });

        srv.init();
        srv.run();
    }
}
