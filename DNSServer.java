/* Spring 2020 CSci4211: Introduction to Computer Networks
** This program serves as the server of DNS query.
** Written in Java. 
** Author: Jeevan Prakash
** Date: 10/20/21
*/

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

class DNSServer {
	public static void main(String[] args) throws Exception {
		int port = 8990;
		ServerSocket sSock = null;

		try {
			sSock = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		System.out.println("Server is listening...");
		new monitorQuit().start(); // Start a new thread to monitor exit signal.

		while (true) {
			new dnsQuery(sSock.accept()).start();
		}
	}
}

class dnsQuery extends Thread {
	Socket sSock = null;
    	dnsQuery(Socket sSock) {
    	this.sSock = sSock;

    }
    public String IPselection(String[] ipList){
		if(ipList.length==0) return ""; // if ipList is empty, return an empty string
		return ipList[0]; // return the first ip in the list of IP addresses
    }
	@Override public void run(){
		BufferedReader inputStream;
        PrintWriter outStream;
        try {
			//Open an input stream and an output stream for the socket
			inputStream = new BufferedReader(new InputStreamReader(sSock.getInputStream()));
			outStream = new PrintWriter(sSock.getOutputStream(), true);

			//Read requested query from socket input stream
			String query = inputStream.readLine().trim();

			//Parse input from the input stream
			//Check the requested query

			Scanner cacheReader; // reader for the cache file
			PrintWriter cacheWriter; // writer for the cache file
			PrintWriter logWriter; // writer for the log file

			String response = ""; // String to store the response back to client
			String cacheInput = ""; // String to store what goes into the cache
			String log = ""; // String to store the log message

            boolean hostFound = false; // flag for whether host has been found
            try {
				File cacheFile = new File("DNS_mapping.txt"); //set local file cache to predetermined file.
				if(!cacheFile.exists()) {
					cacheFile.createNewFile(); //create file if it doesn't exist
				}
				File logFile = new File("dns-server-log.csv"); //set local file cache to predetermined file.
				if(!logFile.exists()) {
					logFile.createNewFile(); //create file if it doesn't exist
				}

				String cacheLine; // temporary variable for storing each line of the cache

				cacheReader = new Scanner(cacheFile);
				while(cacheReader.hasNextLine()) { //check the DNS_mapping.txt to see if the host name exists
					//if it does exist, read the file line by line to look for a
					//match with the query sent from the client
					cacheLine = cacheReader.nextLine();
					String[] temp = cacheLine.split(",");
					if(temp[0].equals(query)) { //If match, use the entry in cache.
						//However, we may get multiple IP addresses in cache, so call IPselection to select one. 
						
						//Putting all IP Addresses received into String array
						String[] ipAddresses = new String[temp.length-1];
						for(int i=0; i<ipAddresses.length; i++) {
							ipAddresses[i] = temp[i+1];
						}

						response = query + ":" + IPselection(ipAddresses) + ":" + "CACHE"; // formatting response
						log = query + "," + IPselection(ipAddresses) + ",CACHE"; // formatting cache message

						hostFound = true; // host has been found
						break;
					}
				}
				if(!hostFound) {
					//If no lines match, query the local machine DNS lookup to get the IP resolution
					String apiResponse;

					try {
						InetAddress[] addresses = InetAddress.getAllByName(query); // get all the IP addresses under the queried hostname

						//Putting all IP Addresses received into String array
						String[] ipAddresses = new String[addresses.length];
						cacheInput = query;
						for(int i=0; i<addresses.length; i++) {
							cacheInput = cacheInput + "," + addresses[i].getHostAddress();
							ipAddresses[i] = addresses[i].getHostAddress();
						}

						apiResponse = IPselection(ipAddresses); // selecting IP address
						response = query + ":" + apiResponse + ":API"; // formatting response
						log = query + "," + apiResponse + ",API"; // formatting log message
						cacheInput = query + "," + apiResponse; // formatting cache message

						hostFound = true; // host has been found
					} catch(UnknownHostException e) { // If host was not found even through the API
						response = query + ":server can't find " + query + ":API"; // formatting response
						log = query + ",server can't find " + query + ",API"; // formatting log message
						cacheInput = query + ",server can't find " + query; // formatting cache message
						hostFound = true;
					}
				}

				// instantiating cache writer and log writer
				cacheWriter = new PrintWriter(new FileOutputStream(cacheFile, true));
				logWriter = new PrintWriter(new FileOutputStream(logFile, true));
				if(hostFound) {
					//send the response back to the client
					outStream.println(response);

					//write the cache message to cache if there is anything to write
					if(!cacheInput.isEmpty()) cacheWriter.println(cacheInput);

					//write the response in dns-server-log.csv
					logWriter.println(log);

					//print response to the terminal
					System.out.println(response);
				}
				//Close the server socket.
				sSock.close();

				//Close the input and output streams.
				cacheWriter.close();
				logWriter.close();
				inputStream.close();
				outStream.close();
				cacheReader.close();
            
            } catch (Exception e) {
                System.out.println("exception: " + e);
			}

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Host not found.\n" + e);
        }
	}
}

class monitorQuit extends Thread {
	@Override
	public void run() {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(System.in)); // Get input from user.
		String st = null;
		while(true){
			try{
				st = inFromClient.readLine();
			} catch (IOException e) {
			}
            if(st.equalsIgnoreCase("exit")){
                System.exit(0);
            }
        }
	}
}
