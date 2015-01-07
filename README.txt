---------a.Introduction---------------------------------

The program is implemented in Java. It has 4 classes, which are Server, ServerThread, Client and ClientThread. 

The Server class is responsible for creating the server socket, managing the server threads, implementing the logic of the commands, providing some helper functions and sending instructions to clients. 

The ServerThread class is extended from Thread class and is in charge of the login. The program will create a separate thread for each client connection to make sure that the command from each connection can be processed in time.  
 
The Client class is responsible for creating the client socket, connecting to the server, sending commands to server, reading instructions and displaying instructions from the server as well. It also creates new thread for reading the user input.  

The ClientThread class is also extended from Thread class and handles user input from the terminal. 

The extra packages used are: 
java.io, java.net, java.util, java.util.concurrent, java.util, java.util.Scanner 

The main techniques used are:
1.Socket programming - create sockets for connections between server and clients
2.Multithread and synchronization - use multiple threads to handle user input and network I/O at the same time
3.ScheduledExecutorService - create timer so that the TIME_OUT mechanism can be checked every period of time
4.Runtime class - use AddShutdownHook in Runtime class to handle control + c gracefully 
 








--------b. Development environment------------------------

Operating System: Linux Ubuntu
JDK version: 1.6
IDE: IntelliJ
Testing: Linux terminal











-------c. Instruction on how to run code--------------------

First, we should run "make" to compile the .java files.
>make

Then the server program will be invoked as  
>Server <port_no> 
Example: java Server 4119

After that, we can invoke the client program by
>Client <server_IP_address> <server_port_no> 
Example: java Client 127.0.0.1 4119











------d. Sample commands and results--------------------------

Senario 1: Start the server program

Terminal 1 
>make
>java Server 4119
connect to port 4119, waiting for the clients...



Senario 2: Client connects to the server, inputs the username and password which is hidden because of privacy protection.

Terminal 2 
>java Client 127.0.0.1 4119
connected toSocket[addr=/127.0.0.1,port=4119,localport=61227]
>Username: facebook

//As part of extra feature, we mask the password
>Password:

//If user inputs wrong password, send him warning			
Wrong password 1 time	
		 
>Username: facebook
>Password:

//This time user inputs right password, show welcome message
>Welcome to Di Ruan's chat server!
	
//Wait for user to input commands
>Command: 				



Senario 3: Client inputs the wrong password for consecutive 3 times and he will be blocked by server for 60 seconds.

Terminal 3 
> java Client 127.0.0.1 4119
connected toSocket[addr=/127.0.0.1,port=4119,localport=61228]
>Username: google
>Password:
Wrong password 1 time
>Username: google
>Password:
Wrong password 2 time
>Username: google
>Password:
Wrong password 3 time

//If the user input wrong password for 3 times, he will be blocked and the connection will be dropped.
Wrong password 3 time				
Sorry, you will be blocked for 60 seconds	
You are logged out by the system. Goodbye!	
Notifying server and closing socket...
Exit successfully!

// After 60s, google can log in again
>Username: google				
>Password:
>Welcome to Di Ruan's chat server!
>Command:



Senario 4: Client uses a username who has already logged in. So he changes to another valid user name and run "whoelse", "wholasthr", "broadcast", "message", "logout".

Terminal 4 
If google has already logged in
> java Client 127.0.0.1 4119
connected toSocket[addr=/127.0.0.1,port=4119,localport=61228]
>Username: google		
//google has already logged in, so we will remind him if he uses the same username 		
Sorry, you are already logged in

//Then the same user login as wikipedia
>Username: wikipedia				
>Password:
>Welcome to Di Ruan's chat server!

//He runs whoelse and shows who currently is online except himself
>Command: whoelse				
facebook			
google

//He runs wholasthr and shows who currently is online during the last hour
>Command: wholasthr				
facebook				
google

//He sends "Hi" to all current online user except himself
>Command: broadcast Hi			
//Then this message shows on Terminal 2 and 3
Wikipedia: Hi

//He runs sends "Hi" to google
>Command: message google Hi			
Then this message shows on Terminal 3		
Wikipedia: Hi
>Command: logout		
//After he logouts, we close the socket and exit the program gracefully
You are logged out by the system. Goodbye! 

Notifying server and closing socket...
Exit successfully!







-------e. Additional functionalities-----------------------------

1. Mask the password

Description:
Like other terminal client program, we need to hide user password to ensure the privacy.

Test:
>Username: facebook
//the program make user input password invisible
>Password:				





2. Provide an extra command called "help"

Description:
For a new user, he may not know the commands that he can use or how to use them properly. Once he inputs an invalid command, we will notify him that he can type help for the list of commands available and the right format for each user. 

Test:
>Command: help
The chat room of Di Ruan, version 1.0.
Here is a list of commands that you can use.
1. whoelse                      	--Display other connected users
2. wholasthr                    	--Display users connected within last hour
3. broadcast <message>         	--Send <message> to all other connected users
4. message <user> <message>     	--Send private <message> to a <user>
5. logout                       	--Log out current user
6. template                     	--Show all template sentences
                                		--Replace <message> by <T+number>
                                		--For example, broadcast T1
7. expression                  	--Show all expressions
                                		--Replace <message> by <E+number>
                                		--For example, broadcast E1





3. Provide an extra command called "template"

Description:
There are some sentences those are used in the chat room very often, we can collect them and form a template. Then user doesn't need to repeat typing them every time. 

Test:
>Command: template			
//After you type "template", here is the instruction
Here is a list of template sentences that you can use.

In order to use, you can replace <message> by <T+number>
For example, you can type "broadcast T1"

1. Good morning, everyone!
2. Nice to meet you!
3. Sorry, I have to go. Bye!
4. I will miss you!
5. This chat room is so cool!

//And if you follow the instruction by replacing message by T1-T5, you will save a lot of time in tying. For example,
>Command: broadcast T2	
//It will show the second sentence on the template list above when other users see this message
facebook: Nice to meet you!	
	




4. Provide an extra command called "expression"

Description:
Some face expressions composed by symbols will make the chat room more vivid. Similarly to the template, we save the most often used expressions in our system and user can use them by inputting the shortcut.

Test:
>Command: expression
//After you type "expression", here is the instruction
Here is a list of template expressions that you can use.

In order to use, you can replace <message> by <E+number>
For example, you can type "broadcast E1"

1. :-)
2. :-(
3. O(^_^)O
4. ('_')
5. ^_^
6. (>_<)
7. (^_-)
8. (T_T)
9. (-_-)zzz

//And if you follow the instruction by replacing message by E1-E5, you will send lovely expressionfun to the other users, which makes the chat room more interesting. For example,
>Command: broadcast E3	
//It will show the third expression on the template list above when other users see this message.
facebook: O(^_^)O	









5. Create a role called Admin, who has the privilege to kick a user out

Description:
In most online chat room or BBS, we have a role called administrator. Therefore, we also create an admin, who is able to kick somebody out the chat room, if he found someone is cheating or spreading false information.

Test:
//Use username:admin and password:admin to login
>Username: admin
>Password:
>Hi, administrator, welcome back!
>Command: kickout wikipedia

//Then in the terminal of Wikipedia, it will show:
You are logged out by the system. Goodbye!
Notifying server and closing socket...
Exit successfully!







6. When server exits, we need to force all clients program to exit

Description:
When server exits, all the users should be automatically logged out. Likewise, when a user logged out, the client side should inform the server side and close corresponding connection. 

Test:
//Run server and close the server by typing Ctrl+C
//Here is the result in server terminal
Closing server, closing socket and notifying clients...
Connection 2 is disconnected
Connection 1 is disconnected
All sockets are closed
Exit successfully!

//Here is the result in client terminal
You are logged out by the system. Goodbye!
Notifying server and closing socket...
Exit successfully!






7. The connection status of all clients will show in server terminal

Description:
Whenever there's a new connection connected to the server or disconnected from the server, the information will show on the server terminal.

Test:
Server Terminal
> java Server 4119
connect to port 4119, waiting for the clients...
Connection 1 from Socket[addr=/127.0.0.1,port=61226,localport=4119]
Connection 2 from Socket[addr=/127.0.0.1,port=61227,localport=4119]
Connection 3 from Socket[addr=/127.0.0.1,port=61228,localport=4119]
Connection 1 is disconnected		//Exit Client 1 program by using ctrl+c
Connection 2 is disconnected		//Exit Client 2 program by using ctrl+c
Connection 3 is disconnected		//Exit Client 3 program by using ctrl+c	
