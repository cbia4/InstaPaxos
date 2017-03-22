import java.util.*;
import java.io.*;
import java.net.*;

public class Comm extends Thread {

	// Site Information variables
	private int myID;
	private String myIP;
	private int myPort;
	private ArrayList<SiteInfo> siteInfo;

	// Client variables
	private String clientIP;
	private int clientPort;

	// Network variables
	private ServerSocket requestListener;
	private boolean isOnline;

	// Paxos variables
	private boolean proposingMsg;
	private boolean inPhase1;
	private boolean inPhase2;

	private int ackCounter;
	private int acceptCounter;

	private Ballot ballotNum;
	private Ballot acceptNum;
	private String acceptVal;
	private String msgToAdd;

	private ArrayList<LogNode> log;

	public Comm(SiteInfo myInfo,ArrayList<SiteInfo> siteInfo) {

		// Init site info variables
		myID = myInfo.getSiteID();
		myIP = myInfo.getIPAddress();
		myPort = myInfo.getPort();
		this.siteInfo = siteInfo;

		// Init client variables 
		clientIP = null;
		clientPort = 0;

		// Init network variables
		try {
			requestListener = new ServerSocket(myPort);
			requestListener.setSoTimeout(10000);
		} catch(SocketException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		} 

		isOnline = true;


		// Init paxos variables
		proposingMsg = false;
		inPhase1 = false;
		inPhase2 = false;

		ballotNum = new Ballot(0,0);
		acceptNum = new Ballot(0,0);
		acceptVal = null;
		msgToAdd = null;

		log = new ArrayList<LogNode>();

	}

	@Override 
	public void run() {
		Socket incoming;
		Message ackArray[] = new Message[2];
		Ballot prevBallot = null;

		while(true) {

			Message request = null;
			try {
				incoming = requestListener.accept();
				InputStream is = incoming.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				request = (Message) ois.readObject();
				ois.close();
				is.close();
				incoming.close();
			} catch(SocketTimeoutException e) {
				if(inPhase1 == true && ackCounter < 2) {
					System.out.println("Paxos phase 1 failed!");
					inPhase1 = false;
					ackCounter = 0;
					proposingMsg = false;
				}
				if(inPhase2 == true && acceptCounter < 2) {
					System.out.println("Paxos phase 2 failed!");
					inPhase2 = false;
					acceptCounter = 0;
					proposingMsg = false;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			if(request != null) {

				// set the state of the server
				if(request.getType().equals("Fail")) {
					isOnline = false;
					System.out.println("Server is in fail mode!");
				}
				if(request.getType().equals("Restore") && isOnline == false) {
					isOnline = true;
					System.out.println("Server is online!");
					Message recovery = new Message("Recovery", myID);
					for(int i = 0; i < 5; i++) {
						if(i != myID)
							send(recovery,siteInfo.get(i).getIPAddress(),siteInfo.get(i).getPort());
					}
					
				}

				if(isOnline == true) {
					//TODO


					if(request.getType().equals("Recovery")) {
						Message recoverPoint = new Message("recoverPoint", myID, log.size());
						send(recoverPoint,siteInfo.get(request.getSiteID()).getIPAddress(),siteInfo.get(request.getSiteID()).getPort());
					}

					if(request.getType().equals("recoverPoint")) {
						if(request.getlogSize() > log.size()) {
							Message requestLog = new Message("LogRequest",myID);
							send(requestLog,siteInfo.get(request.getSiteID()).getIPAddress(),siteInfo.get(request.getSiteID()).getPort());
						}
					}

					if(request.getType().equals("LogRequest")) {
						Message logUpdate = new Message("LogUpdate",log);
						send(logUpdate,siteInfo.get(request.getSiteID()).getIPAddress(),siteInfo.get(request.getSiteID()).getPort());
					}

					if(request.getType().equals("LogUpdate")) {
						log = request.getLog();
					}
					if(request.getType().equals("Post") && proposingMsg == false) {
						// begin paxos round if not already in one 

						// this will be reset to false after round 
						// succeeds or fails
						proposingMsg = true;
						inPhase1 = true;
						ackCounter = 0;

						clientIP = request.getClientIP();
						clientPort = request.getClientPort();
						msgToAdd = request.getMsg();

						ballotNum.setNum(ballotNum.getNum() + 1);
						ballotNum.setID(myID);
						prepare();
					}
					if(request.getType().equals("Read")) {
						clientIP = request.getClientIP();
						clientPort = request.getClientPort();
						clientResponse("Read");
					}
					if(request.getType().equals("Prepare")) {
						//System.out.println("Prepare request received!");
						prepEval(request);
					}
					if(request.getType().equals("Ack") && inPhase1 == true) {

						if(ackCounter == 0) {
							ackArray[0] = request;
						}
						if(ackCounter == 1) {
							ackArray[1] = request;
						}

						ackCounter++;


						//System.out.println("Received 'Ack' " + ackCounter);

						if(ackCounter == 2) {
							inPhase1 = false;

							if(ackArray[0].getMsg() == null && ackArray[1].getMsg() == null) {
								acceptVal = msgToAdd;
							}
							else {
								acceptVal = msgToAdd;
								if(ackArray[0].getAcceptNum().compare(ackArray[1].getAcceptNum())) {
									if(ackArray[0].getAcceptNum().compare(acceptNum)) {
										acceptVal = ackArray[0].getMsg();
									}
								}
								else if(ackArray[1].getAcceptNum().compare(ackArray[0].getAcceptNum())) {
									if(ackArray[1].getAcceptNum().compare(acceptNum)) {
										acceptVal = ackArray[1].getMsg();
									}
								}
							}

							inPhase2 = true;
							ackCounter = 0;
							//System.out.println("End Phase 1 \nBegin Phase 2");
							accept();
						}
					}
					if(request.getType().equals("Accept")) {
						if((request.getBallot().compare(ballotNum) || request.getBallot().equals(ballotNum)) && request.getBallot().notEqual(acceptNum)) {
							acceptCounter = 0;
							acceptNum = request.getBallot();
							acceptVal = request.getMsg();
							if(inPhase2 != true)
								accept();
							acceptCounter++;
						}
						else if(request.getBallot().equals(acceptNum)) {
							acceptCounter++;
						}
						if(acceptCounter == 2) {
							log.add(new LogNode(acceptVal,acceptNum.getNum()));

							if(inPhase2 == true && msgToAdd.equals(acceptVal)) {
								clientResponse("Post");
							}
							inPhase2 = false;
							proposingMsg = false;
							msgToAdd = null;
							
						}
					}
				}
			}
		}
	}//end of run


	private void prepare() {
		Message prepareRequest = new Message("Prepare",ballotNum,myID);
		for(int i = 0; i < 5; i++) {
			if(i != myID) {
				send(prepareRequest,siteInfo.get(i).getIPAddress(),siteInfo.get(i).getPort());
			}
		}
	}

	private void accept() {
		Message acceptRequest = new Message("Accept",ballotNum,acceptVal);
		for(int i = 0; i < 5; i++) {
			if(i != myID) {
				send(acceptRequest,siteInfo.get(i).getIPAddress(),siteInfo.get(i).getPort());
				//System.out.println("sent an accept message to site " + i);
			}
		}
	}

	private void prepEval(Message m) {

		// type, ballotNum, acceptNum, acceptVal
		int replyID = m.getSiteID();
		Ballot proposedBallot = m.getBallot();
		String replyIP = siteInfo.get(replyID).getIPAddress();
		int replyPort = siteInfo.get(replyID).getPort();

		if(proposedBallot.compare(ballotNum)) {
			ballotNum = proposedBallot;
			Message reply = new Message("Ack",ballotNum,acceptNum,acceptVal);
			send(reply,replyIP,replyPort);
		}
	}

	private void clientResponse(String type) {
		Message response = null;
		if(type.equals("Post")) {
			response = new Message("Post",log.get(log.size() - 1));
		}
		if(type.equals("Read")) {
			response = new Message("Read",log);
		}
		send(response,clientIP,clientPort);
	}


	private void send(Message m, String ipAddress, int port) {
		Socket outgoing;
		try {
			outgoing = new Socket(ipAddress,port);
			OutputStream os = outgoing.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(m);
			oos.close();
			os.close();
			outgoing.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}