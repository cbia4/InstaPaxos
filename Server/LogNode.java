import java.io.*;
import java.net.*;
import java.util.*;

public class LogNode implements Serializable {

	private String msg;
	private int msgID;

	public LogNode(String msg,int msgID) {
		this.msg = msg;
		this.msgID = msgID;
	}

	public String getMsg() { return msg; }
	public int getMsgID() { return msgID; }
	
}