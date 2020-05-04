import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * @Author: zhouzilong 
 * @Date: 2020-05-03 17:23:41 
 * @Last Modified by: zhouzilong
 * @Last Modified time: 2020-05-04 00:42:00
 */

public class Server {
    // The UID for the clients.
    private static int ID;
    // The set to keep all the users in the room.
    private ArrayList<ClientThread> userList;
    // To get time.
    private SimpleDateFormat sdf;
    // Port number.
    private int port;
    // To check if the server is still working.
    private boolean working;

    private final String flag = "[#] ";

    private FileWriter fileWriter;

    public Server(int port) throws IOException {
        this.port = port;
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.userList = new ArrayList<ClientThread>();
        this.fileWriter = null;
    }

    public void serve() {
        this.working = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            // waiting for connection.
            while (working) {
                show("Server waiting for Clients on port " + port + ".");
                // if requested from client.
                Socket socket = serverSocket.accept();
                // if stopped.
                if (!working) {
                    break;
                }
                // if a client was connected, create a thread for it.
                ClientThread clientThread = null;
                try {
                    clientThread = new ClientThread(socket);
                } catch (RuntimeException e) {
                    continue;
                }
                // add this client to the user list.
                userList.add(clientThread);
                clientThread.start();
            }
            // trying to close the server if not working.
            try {
                serverSocket.close();
                for (int i = 0; i < userList.size(); ++i) {
                    ClientThread curClientThread = userList.get(i);
                    try {
                        curClientThread.inputStream.close();
                        curClientThread.outputStream.close();
                        curClientThread.socket.close();
                    } catch (IOException ioE) {
                    }
                }
            } catch (Exception e) {
                show("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception creating new ServerSocket: " + e + "\n";
            show(msg);
        }

    }

    // to stop a server.
    protected void stop() {
        this.working = false;
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
        }
    }

    // to show some messages to the console.
    public void show(String msg) {
        String str = sdf.format(new Date()) + " " + msg;
        System.out.println(str);
    }

    // To kick it out if a client sent the LOGOUT message.
    synchronized void remove(int id) {
        String disconnectedClient = "";
        for (int i = 0; i < userList.size(); ++i) {
            ClientThread curClientThread = userList.get(i);
            // if found
            if (curClientThread.UID == id) {
                disconnectedClient = curClientThread.getUsername();
                userList.remove(i);
                break;
            }
        }
        this.broadcast(this.flag + disconnectedClient + " has left the room.");
    }

    private synchronized boolean broadcast(String message) {
        String time = sdf.format(new Date());
        String[] w = message.split(" ", 3);
        boolean isPrivate = false;
        if (w[1].charAt(0) == '@')
            isPrivate = true;
        // if private message, send message to the target.
        if (isPrivate == true) {
            String tocheck = w[1].substring(1, w[1].length());
            message = w[0] + w[2];
            String messageLf = time + " " + message + "\n";
            boolean found = false;
            for (int y = userList.size(); --y >= 0;) {
                ClientThread ct1 = userList.get(y);
                String check = ct1.getUsername();
                if (check.equals(tocheck)) {
                    if (!ct1.writeMsg(messageLf)) {
                        userList.remove(y);
                        show("Disconnected Client " + ct1.username + " removed from list.");
                    }
                    found = true;
                    break;
                }
            }
            // mentioned user not found, return false
            if (found != true) {
                return false;
            }
        }
        // if message is a broadcast message
        else {
            String messageLf = time + " " + message + "\n";
            if (messageLf.charAt(20) != '[') {
                try {
                    this.fileWriter = new FileWriter("./record.txt", true);
                    this.fileWriter.write(messageLf);
                    this.fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.print(messageLf);
            for (int i = userList.size(); --i >= 0;) {
                ClientThread ct = userList.get(i);
                if (!ct.writeMsg(messageLf)) {
                    userList.remove(i);
                    show("Disconnected Client " + ct.username + " removed from list.");
                }
            }
        }
        return true;
    }

    class ClientThread extends Thread {
        // Socket for exchanging message.
        Socket socket;
        // I/O stream
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        // UID for this client.
        int UID;
        // Username
        String username;
        // Password
        String password;
        // Message
        Message message;
        // time
        String time;

        public ClientThread(Socket socket) {
            this.UID = ++ID;
            this.socket = socket;
            // Set I/O steam for it.
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
                this.inputStream = new ObjectInputStream(this.socket.getInputStream());
                this.username = (String) this.inputStream.readObject();
                this.password = (String) this.inputStream.readObject();
                int position = 0;
                String[] bufstring = new String[1024];
                BufferedReader br = new BufferedReader(new FileReader("users.txt"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    bufstring[position] = line;
                    position++;
                }
                br.close();
                String model = username + " " + password;
                for (int i = 0; i < position; i++) {
                    if (!model.equals(bufstring[i])) {
                        if (i == position - 1) {
                            writeMsg("Wrong username or password !");
                            writeMsg("Wrong username or password !");
                            throw new RuntimeException("Wrong username or password !");
                        }
                        continue;
                    } else {
                        break;
                        // writeMsg("Wrong username or password !");
                        // throw new RuntimeException("Wrong username or password !");
                        // writeMsg("Wrong username or password !");
                    }
                }
                broadcast(flag + this.username + " has entered the room");
            } catch (IOException e) {
                show("Exception creating the I/O Stream" + e);
                return;
            } catch (ClassNotFoundException e) {
            }
            this.time = new Date().toString();
        }

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        // Unless receive LOGOUT, keep running.
        public void run() {
            boolean running = true;
            while (running) {
                try {
                    this.message = (Message) inputStream.readObject();
                } catch (IOException e) {
                    show(username + " has exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // get the message.
                String messageStr = message.getMessage();
                switch (message.getType()) {

                    case Message.MESSAGE:
                        boolean confirmation = broadcast(username + ": " + messageStr);
                        if (confirmation == false) {
                            String msg = flag + "No such user exists.";
                            writeMsg(msg);
                        }
                        break;

                    case Message.LOGOUT:
                        show(username + " disconnected with a LOGOUT message.");
                        running = false;
                        break;

                    case Message.SEARCH:
                        // writeMsg("Enter the date like : 'YYYY-MM-DD'");
                        int position = 0;
                        String[] bufstring = new String[1024];
                        BufferedReader br = null;
                        try {
                            br = new BufferedReader(new FileReader("record.txt"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String line=null;
                        try {
                            while ((line = br.readLine()) != null) {
                                bufstring[position] = line;
                                position++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // System.out.println(message.getMessage());
                        
                        for (int i = 0; i < position; i++) {
                            // System.out.println(bufstring[i].substring(0, 9));
                            if (bufstring[i].substring(0, 10).equalsIgnoreCase(message.getMessage())) {
                                writeMsg(bufstring[i]);
                            }
                        }
                        break;
                    
                    case Message.LIST:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        for(int i = 0; i < userList.size(); ++i) {
                            ClientThread curClientThread = userList.get(i);
                            writeMsg((i+1) + ") " + curClientThread.username + " since " + curClientThread.time + "\n");
                        }
                        break;
				}
            }
            //if out of loop, kick it out.
			remove(this.UID);
			close();
        }

        private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				this.outputStream.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				show(flag + "Error sending message to " + username);
				show(e.toString());
			}
			return true;
        }
        
        private void close() {
			try {
                if(this.outputStream != null) 
                this.outputStream.close();
			}
			catch(Exception e) {}
			try {
                if(this.inputStream != null) 
                this.inputStream.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
        }
    }

    public static void main(String[] args) {
        System.out.println("         _      _           _   ");
		System.out.println("        | |    | |         | |  ");
		System.out.println(" _______| | ___| |__   __ _| |_ ");
		System.out.println("|_  /_  / |/ __| '_ \\ / _` | __|");
		System.out.println(" / / / /| | (__| | | | (_| | |_ ");
		System.out.println("/___/___|_|\\___|_| |_|\\__,_|\\__|");
		System.out.println("                                ");
        // start server on port 5051 unless a PortNumber is specified 
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;
                
        }
        Server server = null;
        try {
            server = new Server(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.serve();
    }
}



