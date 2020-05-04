import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/*
 * @Author: zhouzilong 
 * @Date: 2020-05-03 17:24:09 
 * @Last Modified by: zhouzilong
 * @Last Modified time: 2020-05-04 00:10:02
 */

public class Client {

	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Socket socket;
	private String server;
	private String username;
	private String password;
	private int port;
	public final String flag = "[#]";

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Client(String server, int port, String username, String password) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	// to send a message to some place.
	public void sendMessage(Message message) {
		try {
			this.outputStream.writeObject(message);
		} catch (IOException e) {
			System.out.println("Exception writing to server: " + e);
		}
	}

	// to disconnect with the server.
	private void disconnect() {
		try {
			if (this.inputStream != null)
				this.inputStream.close();
		} catch (Exception e) {
		}
		try {
			if (this.outputStream != null)
				this.outputStream.close();
		} catch (Exception e) {
		}
		try {
			if (this.socket != null)
				this.socket.close();
		} catch (Exception e) {
		}
	}

	public boolean work() {
		// to connect to the server
		try {
			this.socket = new Socket(this.server, this.port);
		} catch (Exception e) {
			System.out.println("Error connectiong to server:" + e);
			return false;
		}
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);
		try {
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException eIO) {
			System.out.println("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		// create the Thread to listen from the server
		new ListenFromServer().start();
		try {
			this.outputStream.writeObject(this.username);
			this.outputStream.writeObject(this.password);
		} catch (IOException e) {
			System.out.println("Exception doing login : " + e);
			this.disconnect();
			return false;
		}
		// String loginOk = "";
		// try {
		// // System.out.println(this.inputStream.readObject());
		// loginOk = (String) this.inputStream.readObject();
		// Thread.sleep(1000);
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// }
		// try {
		// if (((String) this.inputStream.readObject()).equalsIgnoreCase("Wrong username
		// or password !")) {
		// this.disconnect();
		// return false;
		// }
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		return true;
	}

	class ListenFromServer extends Thread {
		public void run() {
			while (true) {
				try {
					String msg = (String) inputStream.readObject();
					System.out.println(msg);
					if (msg.equalsIgnoreCase("Wrong username or password !")) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						System.exit(0);
					}
					System.out.print("> ");
				} catch(IOException e) {
					System.out.println(flag + "Server has closed the connection: " + e);
					break;
				} catch(ClassNotFoundException e) {}
			}
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
		// default values if not entered
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Pioneer";
		String passWord = null;
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the username: ");
		userName = scan.nextLine();
		System.out.println("Enter the password: ");
		passWord = scan.nextLine();

        
		switch(args.length) {
			case 4:
				serverAddress = args[3];
			case 3:
				try {
					portNumber = Integer.parseInt(args[2]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [password] [portNumber] [serverAddress]");
                    // scan.close();
					return;
				}
			case 2: 
				userName = args[0];
				passWord = args[1];
			case 0:
				break;
			default:
                System.out.println("Usage is: > java Client [username] [password] [portNumber] [serverAddress]");
            // scan.close();
			return;
		}
		
		Client client = new Client(serverAddress, portNumber, userName, passWord);
		//to connect to the server
        if(!client.work()) {
            scan.close();
            return;
        }
		
		System.out.println("\n[#] Welcome to my chatroom --zzl.");
		System.out.println("[#] Here are the instructions:");
		System.out.println("[#] Simply type the message to send broadcast to all the users here.");
		System.out.println("[#] Type '@targetname + <space> + message' without quotes to send message to someone only.");
		System.out.println("[#] Type 'LIST' without quotes to see who are active.");
		System.out.println("[#] Type 'LOGOUT' without quotes to logout from the chatroom.");
		System.out.println("[#] Type 'SEARCH' without quotes to search the record for specific date.");

		while(true) {
			System.out.print("> ");
			String msg = scan.nextLine();
			
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new Message(Message.LOGOUT, ""));
				break;
            }
			else if(msg.equalsIgnoreCase("LIST")) {
				client.sendMessage(new Message(Message.LIST, ""));				
			}
			else if(msg.equalsIgnoreCase("SEARCH")) {
				System.out.println("Enter the date like : 'YYYY-MM-DD'");
				String str = null;
				str = scan.nextLine();
				client.sendMessage(new Message(Message.SEARCH, str));
			}
			else {
				client.sendMessage(new Message(Message.MESSAGE, msg));
			}
        }
        
		scan.close();
        client.disconnect();	
        
	}

}