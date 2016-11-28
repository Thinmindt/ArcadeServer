import java.net.*;
import java.io.*;
import java.util.Date;
import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Iterator;

class Main {
	public static void main(String[] args) throws Exception {

		// Listen for a connection from a client
		ServerSocket serverSocket = new ServerSocket(2121);
			
		while (true) {
			Socket clientSocket = serverSocket.accept();
			System.out.println("Got a connection!");
			OutputStream out = clientSocket.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			// Receive the request from the client
			String inputLine;
			String request = "";
			
			do {
				inputLine = in.readLine();
			} while (inputLine == null);

			System.out.println("The client said: " + inputLine);
			request = inputLine;

			byte[] payload = generateSendBack(request);

			// Send the payload
			//out.write(payload.length);
			if (payload == null) {
				System.out.println("empty response");
			} else {
				out.write(payload);	
			}
			
			System.out.println("Done.");

			clientSocket.close();
		}
	}	

	static byte[] generateSendBack(String request) {
		if (request.contains("gamesList")) {
			return sendBackGamesList();
		} else if (request.contains("download:")) {
			String gameToDownload = snipOffRequestType(request);
			return sendBackAPK(gameToDownload);
		} else if (request.contains("password:")) {
			String nameAndPassword = snipOffRequestType(request);
			return checkUsernameAndPassword(nameAndPassword);
		} else if (request.contains("friendsList:")) {
			String user = snipOffRequestType(request);
			return getFriendsList(user);
		}
		return null;
	}

	static String snipOffRequestType(String request) {
		int start = request.indexOf(":") + 1;
		int end = request.length();
		String requestInfo = request.substring(start, end);
		return requestInfo;
	}

	static byte[] getFriendsList(String user) {
		String currentDir = System.getProperty("user.dir");
		File userFriendsFile = new File(currentDir + "/" + user + ".txt");
		byte[] userFriendsListBytes = null;
		String userFriendsList = null;
		System.out.println("trying to find " + userFriendsFile.getPath());

		try {
			userFriendsList = new Scanner(userFriendsFile).useDelimiter("\\Z").next();
			System.out.println(userFriendsList);
			userFriendsListBytes = userFriendsList.getBytes();
		} catch(FileNotFoundException e) {
			System.out.println("The file " + userFriendsFile.getPath() + " was not found.");
		}

		System.out.println("userFriendsSendBack:" + userFriendsList);
		return userFriendsListBytes;
	}

	static byte[] sendBackGamesList() {
		String currentDir = System.getProperty("user.dir");
		File gamesListFile = new File(currentDir + "/listOfGames.txt");
		byte[] gamesListBytes = null;

		try {
			String gamesList = new Scanner(gamesListFile).useDelimiter("\\Z").next();
			System.out.println(gamesList);
			gamesListBytes = gamesList.getBytes();
		} catch(FileNotFoundException e) {
			System.out.println("The file " + gamesListFile.getPath() + " was not found.");
		}

		return gamesListBytes;
	}

	static byte[] sendBackAPK(String gameToDownload) {
		String currentDir = System.getProperty("user.dir");
		File gameAPK = new File(currentDir + "/Tetris.apk");
		byte[] gameAPKBytes = new byte[(int) gameAPK.length()];
		FileInputStream fileInputStream = null;

		try {
			fileInputStream = new FileInputStream(gameAPK);
			fileInputStream.read(gameAPKBytes);
			fileInputStream.close();
		} catch(IOException e) {
			System.out.println("The file " + gameAPK.getPath() + " was not found.");
		}

		return gameAPKBytes;
	}

	static byte[] checkUsernameAndPassword(String clientUsernameAndPassword) {
		String sendBack = "invalid";

		String userToFind = getUserFromUsernameAndPassword(clientUsernameAndPassword);

		String currentDir = System.getProperty("user.dir");
		File userListFile = new File(currentDir + "/listOfUsers.txt");
		byte[] sendBackBytes = null;
		String userBlob = null;

		try {
			userBlob = new Scanner(userListFile).useDelimiter("\\Z").next();
			System.out.println("users: " + userBlob);
		} catch(FileNotFoundException e) {
			System.out.println("The file " + userListFile.getPath() + " was not found.");
		}

		String serverUsernameAndPassword;
		int i = 0;
		do {
			serverUsernameAndPassword = getUserAndPasswordFromUserBlob(userBlob, i);
			System.out.println(i + " pass over : " + serverUsernameAndPassword);
			if (getUserFromUsernameAndPassword(serverUsernameAndPassword).contains(getUserFromUsernameAndPassword(clientUsernameAndPassword))) {
				break;
			}
			i++;
		} while (i <= 3);
		if (!serverUsernameAndPassword.contains(",")) {
			sendBack = "invalid";
			System.out.println("failed to find user: " + getUserFromUsernameAndPassword(clientUsernameAndPassword));
			return sendBack.getBytes();
		} else {
			String clientPassword = getPasswordFromUsernameAndPassword(clientUsernameAndPassword);
			String serverPassword = getPasswordFromUsernameAndPassword(serverUsernameAndPassword);
			if (clientPassword.contains(serverPassword)) {
				sendBack = "Approved";
				System.out.println("password approved");
			} else {
				System.out.println("password doesn't match");
			}
		} 
		sendBackBytes = sendBack.getBytes();

		return sendBackBytes;
	}

	static String getUserFromUsernameAndPassword(String usernameAndPassword) {
		int start = 0;
		int end = usernameAndPassword.indexOf(",");
		System.out.println(usernameAndPassword.substring(start, end));
		return usernameAndPassword.substring(start, end);
	}

	static String getPasswordFromUsernameAndPassword(String usernameAndPassword) {
		int start = usernameAndPassword.indexOf(",") + 1;
		int end = usernameAndPassword.length();
		System.out.println(usernameAndPassword.substring(start, end));
		return usernameAndPassword.substring(start, end);
	}

	static String getUserAndPasswordFromUserBlob(String userBlob, int i) {
		String userCommaPassword = null;
		int j = 0;
		while (j <= i && userBlob != null) {
			if (userBlob.contains("}")) {
				int start = 0;
				int end = userBlob.indexOf("}");
				userCommaPassword = userBlob.substring(start + 1, end);

				if (end + 1 < userBlob.length())
					userBlob = userBlob.substring(end + 1, userBlob.length());
				else
					userBlob = null;
			}
			j++;
		}
		return userCommaPassword; 
	}
}

