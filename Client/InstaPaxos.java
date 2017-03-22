import java.util.*;
import java.io.*;
import java.net.*;

public class InstaPaxos {
	public static void main(String[] args) {
		File file = new File("configuration.txt");
		ArrayList<SiteInfo> siteInfo = new ArrayList<SiteInfo>();
		String hostname = "52.26.114.170";

		// Set leader ID to 0
		int leaderID = 0;
		int newLeaderID;
		Random rand = new Random();

		// Initialize scanner for configuration file 
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch(FileNotFoundException e) {
			System.out.println("Configuration file does not exist.");
			e.printStackTrace();
		}

		// Get client IP address


		int myPort = 0;
		int portNum;
		String ipAddress;
		String input;
		String delims = " ";
		StringTokenizer st;
		int siteNumber = 0;
		int lineNumber = 1;

		// Load configuration information into siteInfo
		while(sc.hasNextLine()) {
			input = sc.nextLine();
			st = new StringTokenizer(input,delims);

			if(lineNumber == 1) {
				myPort = Integer.parseInt(st.nextElement().toString());
			} else {
				ipAddress = st.nextElement().toString();
				portNum = Integer.parseInt(st.nextElement().toString());
				siteInfo.add(new SiteInfo(siteNumber,ipAddress,portNum));
				siteNumber++;
			}

			lineNumber++;
		}

		sc.close();



		input = "";
		String msg = "";
		sc = new Scanner(System.in);
		String[] stringSplit;
		Message request = null;
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(myPort);
		} catch(IOException e) {
			e.printStackTrace();
		}

		// Handle Timeouts 
		//timeout currently set to 15 seconds
		try {
			serverSocket.setSoTimeout(15000);
		} catch(SocketException e) {
			//e.printStackTrace();
		}

		while(true) {
			msg = "";
			Socket incoming;
			Socket outgoing;
			System.out.print("> ");
			input = sc.nextLine();
			stringSplit = input.split(" ");

			if(stringSplit[0].equals("Read")) {

				// Read='R'
				request = new Message("Read","",hostname,myPort);
			}

			if(stringSplit[0].equals("Post")) {
				for(int i = 1; i < stringSplit.length; i++) {
					msg += stringSplit[i] + " ";
				}

				// Post='P'
				request = new Message("Post",msg,hostname,myPort);
			}

			if(stringSplit[0].equals("Quit")) {
				return;
			}

			if(stringSplit[0].equals("Post") || stringSplit[0].equals("Read")) {
				try {
					outgoing = new Socket(siteInfo.get(leaderID).getIPAddress(),9999);
			
					OutputStream os = outgoing.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(request);
					oos.close();
					os.close();
					outgoing.close();
				} catch(Exception e) {
					e.printStackTrace();
				}

				Message response = null;
				try {
					incoming = serverSocket.accept();
					InputStream is = incoming.getInputStream();
					ObjectInputStream ois = new ObjectInputStream(is);
					response = (Message) ois.readObject();
					ois.close();
					is.close();
					incoming.close();
				} catch(SocketTimeoutException e) {

					// THIS HANDLES TIMEOUTS 
					System.out.println("Request failed. Please try again.");
					newLeaderID = rand.nextInt(5);
					while(newLeaderID == leaderID) {
						newLeaderID = rand.nextInt(5);
					}
					leaderID = newLeaderID;
					System.out.println("Try server " +leaderID);
				} catch(Exception e) {
					e.printStackTrace();
				}

				// update accordingly
				if(response != null) {
					if(response.getType().equals("Post")) {
						System.out.println("-----------------------------------------");
						System.out.println("Post appended to log. \nMsg id: " + response.getNode().getMsgID());
						System.out.println("-----------------------------------------");
					}
					if(response.getType().equals("Read")) {
						System.out.println("-----------------------------------------");
						for(int i = 0; i < response.getLog().size(); i++) {
							System.out.println("Msg: " + response.getLog().get(i).getMsg());
							System.out.println("MsgID: " + response.getLog().get(i).getMsgID());
							System.out.println("-----------------------------------------");
						}
					}
				}
			}

		}


	}
}