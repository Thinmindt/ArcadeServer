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
		ServerSocket serverSocket = new ServerSocket(80);
		if(Desktop.isDesktopSupported())
			Desktop.getDesktop().browse(new URI("http://localhost:1234"));
		else
			System.out.println("Please direct your browser to http://localhost:1234.");
				
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Got a connection!");
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String dateString = (new Date()).toGMTString();
				
				
				// Receive the request from the client
				String inputLine;
				String url = "";
				
				while ((inputLine = in.readLine()) != null) {
					System.out.println("The client said: " + inputLine);
					if (inputLine.contains("GET ")) {
						int end = inputLine.indexOf(" HTTP");
						url = inputLine.substring(4, end);
					}
					if(inputLine.length() < 2)
						break;
				}
				
				String payload = generatePage(url);
				
				// Send HTTP headers
				System.out.println("Sending a response...");
				out.print("HTTP/1.1 200 OK\r\n");
				out.print("Content-Type: text/html\r\n");
				out.print("Content-Length: " + Integer.toString(payload.length()) + "\r\n");
				out.print("Date: " + dateString + "\r\n");
				out.print("Last-Modified: " + dateString + "\r\n");
				out.print("Connection: close\r\n");
				out.print("\r\n");

				// Send the payload
				out.println(payload);
				System.out.println("Done.");
		}
	}
	
	static String readFile(String filename) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filename)));
		} catch (IOException e) {
			System.out.println("The file was not found.");
		}
		return content;
	}
	
	static File[] getFiles(String fileName) {
		File folder = new File(fileName);
		File listOfFiles[] = folder.listFiles();
	
		return listOfFiles;
	}

	static LinkedList<File> getDocuments(File[] listOfFiles) {
		LinkedList<File> documents = new LinkedList<File>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				documents.add(listOfFiles[i]);
			}
		}
		return documents;
	}
	
	static LinkedList<File> getDirectories(File[] listOfFiles) {
		LinkedList<File> directories = new LinkedList<File>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				directories.add(listOfFiles[i]);
			}
		}
		return directories;
	}
	
	static String generatePage(String url) {
		StringBuilder payload = new StringBuilder();
		
		if (url.contains("favicon"))
			payload.append("");
		else if (!url.contains("openFile")) {
			if (url.equals("/")) 
				url = ".";
			else if (url.contains("index")) {
				url = url.substring(15, url.length());
				}
			
			File[] listOfFiles = getFiles(url);
			LinkedList<File> documents = getDocuments(listOfFiles);
			LinkedList<File> directories = getDirectories(listOfFiles);
			
			payload.append("<table>\n" +
			"<tr><td align=right>Current directory:</td><td>\n");
			payload.append(url);
			payload.append("</td></tr>\n" +
			"<tr><td>\n" +
			"<b>Folders:</b><br>\n" +
			"<select id=\"folderList\" size=\"15\" style=\"width: 280px\" onchange=\"javascript:location.href=this.value;\">\n" +
			"<option value=\"index.html?cd=..\">..</option> \n");
			
			Iterator<File> itDir = directories.iterator();
			while (itDir.hasNext()){
				File f = itDir.next();
				payload.append("<option value=\"index.html?cd=" + f + "\">" + f.getName() + "</option> \n");
			}
			
			payload.append("</select>\n" +
			"</td><td>\n" +
			"<b>Files:</b><br>\n" +
			"<select id=\"fileList\" size=\"15\" style=\"width: 280px\" onchange=\"javascript:location.href=this.value;\">\n");
			
			Iterator<File> itDoc = documents.iterator();
			while (itDoc.hasNext()){
				File f = itDoc.next();
				payload.append("<option value=\"openFile=" + url + "/" + f.getName() + "\">" + f.getName() + "</option> \n");
			}
			
			payload.append("</select> \n" +
			"</td></tr></table>\n");
			
		}
		else {
			url = url.substring(10, url.length());
			System.out.println("The file to find is named: " + url);
			
			try {
				String currentLine;
				BufferedReader br = new BufferedReader(new FileReader(url));
				while ((currentLine = br.readLine()) != null)
					payload.append(currentLine + "<br>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return payload.toString();
	}
}