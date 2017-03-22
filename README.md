# InstaPaxos
A distributed microblogging application that implements the Paxos algorithm to ensure fault-tolerance. Since paxos does not solve the deadlock problem that arises when attempting to acheive consensus between servers, a timeout has been implemented to break from deadlock and prevent the entire system from failing. 
