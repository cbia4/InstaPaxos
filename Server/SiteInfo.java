import java.io.*;
import java.util.*;
import java.net.*;

public class SiteInfo {

	private int siteID;
	private String ipAddress;
	private int port;

	public SiteInfo(int siteID, String ipAddress, int port) {
		this.siteID = siteID;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public int getSiteID() { return siteID; }
	public String getIPAddress() { return ipAddress; }
	public int getPort() { return port; }

	public void printInfo() {
		System.out.println("Site " + siteID + " Information:");
		System.out.println("IP Address: " + ipAddress);
		System.out.println("Port: " + port);
		System.out.println("---------------------------------");
	}


}