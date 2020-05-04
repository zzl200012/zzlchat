### ZZLCHAT

**Background**

This is a homework for the OOP course in Java, which just used a sort of OOP and socket features or APIs in Java cause I'm not very familiar with Java programming.

**Install**

For installation, simply start a server by typing `java Server`to start a service on the default port (1500), and then start a client by typing `java Client` to start a service on 1500. Now you can send the message.

**Usage**

###### server

`java Server`: Start a server service on 1500.

`java Server [portNumber]`: Start a server service on the port you set.

###### client

`java Client `: Start a client service on 1500.

`java Client [userName] [passWord] [portNumber]`: Start a client service on the port you set.

`java Client [userName] [passWord] [portNumber] [serverAddress]`: Start a client service on your demand. 

###### instruction

1.Simply type the message to send broadcast to all the users active.

2.Type '@targetname + <space> + message' without quotes to send message to someone only.

3.Type 'LIST' without quotes to see who are active.

4.Type 'LOGOUT' without quotes to logout from the chatroom.

5.Type 'SEARCH' without quotes to search the record for specific date.