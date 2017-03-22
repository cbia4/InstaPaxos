import java.util.*;
import java.io.*;
import java.net.*;

public class CLI extends Thread {

	private int myID;
	private String myIP;
	private int myPort;

	public CLI(SiteInfo myInfo) {
		myID = myInfo.getSiteID();
		myIP = myInfo.getIPAddress();
		myPort = myInfo.getPort();
	}

	@Override 
	public void run() {
		String input;
		Scanner sc = new Scanner(System.in);
		Socket commSocket;
		Message m;

		while(true) {
			input = sc.nextLine();
			m = null;
			if(input.equals("Fail")) 
				m = new Message("Fail");
			if(input.equals("Restore")) 
				m = new Message("Restore");
			if(m != null) {
				try {
					commSocket = new Socket(myIP,myPort);
					OutputStream os = commSocket.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(m);
					oos.close();
					os.close();
					commSocket.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}