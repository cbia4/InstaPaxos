import java.io.*;
import java.net.*;
import java.util.*;

public class Message implements Serializable {

	private String type;
	private String msg;
	private int siteID;
	private int msgID;
	private String clientIP;
	private int clientPort;
	private int logSize;

	private ArrayList<LogNode> log;

	private Ballot ballot;
	private Ballot acceptedNum;

	private LogNode node;


	// Client Request Message ('r'=read, 'a'=append)
	public Message(String type, String msg, String clientIP, int clientPort) {
		this.type = type;
		this.msg = msg;
		this.clientIP = clientIP;
		this.clientPort = clientPort;
	}

	// Ballot Request Message ("Prepare")
	public Message(String type, Ballot b,int siteID) {
		this.type = type;
		ballot = b;
		this.siteID = siteID;

	}


	// Ballot Reply Message ("Ack")
	public Message(String type, Ballot ballot, Ballot acceptedNum, String msg) {
		this.type = type;
		this.ballot = ballot;
		this.acceptedNum = acceptedNum;
		this.msg = msg;
	}

	// FAIL and RESTORE signal
	public Message(String type) {
		this.type = type;
	}
	//Recovery message
	public Message(String type, int siteID) {
		this.type = type;
		this.siteID = siteID;
	}

	//Recovery message reply
	public Message(String type, int siteID, int logSize) {
		this.type = type;
		this.siteID = siteID;
		this.logSize = logSize;
	}
	
	// Accept Request Message ("Accept")
	public Message(String type, Ballot b,String msg) {
		this.type = type;
		ballot = b;
		this.msg = msg;
	}

	public Message(String type, LogNode node) {
		this.type = type;
		this.node = node;
	}

	public Message(String type, ArrayList<LogNode> log) {
		this.type = type;
		this.log = log;
	}



	public String getType() { return type; }
	public String getMsg() { return msg; }
	public int getSiteID() { return siteID; }
	public int getMsgID() { return msgID; }
	public int getlogSize() {return logSize;}

	public String getClientIP() { return clientIP; }
	public int getClientPort() { return clientPort; }

	public ArrayList<LogNode> getLog() { return log; }

	public Ballot getBallot() { return ballot; }
	public Ballot getAcceptNum() { return acceptedNum; }

	public LogNode getNode() { return node; }

	
}