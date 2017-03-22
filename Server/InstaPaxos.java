import java.util.*;
import java.io.*;
import java.net.*;


public class InstaPaxos {
	public static void main(String[] args) {
		File file = new File("configuration.txt");
		ArrayList<SiteInfo> siteInfo = new ArrayList<SiteInfo>();
		Scanner sc = null;

		try {
			sc = new Scanner(file);
		} catch(FileNotFoundException e) {
			System.out.println("Configuration file not found.");
			return;
		}

		String input;
		String delims = " ";
		StringTokenizer st;
		int siteNumber = 0;
		int lineNumber = 1;


		SiteInfo myInfo = null;
		int siteID = 0;
		String ipAddress = " ";
		int port = 0;


		while(sc.hasNextLine()) {
			input = sc.nextLine();
			st = new StringTokenizer(input,delims);

			if(lineNumber == 1) {
				siteID = Integer.parseInt(st.nextElement().toString());
			} else {
				ipAddress = st.nextElement().toString();
				port = Integer.parseInt(st.nextElement().toString());
				siteInfo.add(new SiteInfo(siteNumber,ipAddress,port));
				if(siteNumber == siteID) {
					myInfo = new SiteInfo(siteID,ipAddress,port);
				}
				siteNumber++;
			}
			lineNumber++;
		}

		sc.close();

		//myInfo.printInfo();

		Thread cli = new CLI(myInfo);
		Thread comm = new Comm(myInfo,siteInfo);

		cli.start();
		comm.start();

		
	}
}