import java.io.*;
import java.net.*;
import java.util.*;

public class Ballot implements Serializable {
	
	private int num;
	private int processID;

	public Ballot(int num, int processID) {
		this.num = num;
		this.processID = processID;
	}


	public int getNum() { return num; }
	public int getPID() { return processID; }

	//ballot.setNum(ballot.getNum() + 1);
	public void setNum(int num) { this.num = num; }
	public void setID(int id) { processID = id; }

	public boolean compare(Ballot b) {
		if(this.num > b.getNum() || (this.num == b.getNum() && this.processID > b.getPID())) 
			return true;
		else 
			return false;
	}

	//@Override
	public boolean equals(Ballot b) {
		if(this.num == b.getNum() && this.processID == b.getPID())
			return true;
		else 
			return false;
	}

	public boolean notEqual(Ballot b) {
		if(this.num != b.getNum() || this.processID != b.getPID())
			return true;
		else
			return false;
	}

}