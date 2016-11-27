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
			out.write(payload);
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
		} else if (request.contains("login:")) {
			String nameAndPassword = snipOffRequestType(request);
			return checkUsernameAndPassword(nameAndPassword);
		}
		return null;
	}

	static String snipOffRequestType(String request) {

			int start = request.indexOf(":") + 1;
			int end = request.length();
			String requestInfo = request.substring(start, end);
			return requestInfo;
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

	static byte[] checkUsernameAndPassword(String usernameAndPassword) {
		String sendBack = "one if success, zero if fail";

		byte[] sendBackBytes = sendBack.getBytes();
		return sendBackBytes;
	}
}

