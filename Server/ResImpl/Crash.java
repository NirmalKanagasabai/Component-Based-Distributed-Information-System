package servercode.ResImpl;


public enum Crash {
	
	P_B_SAVEWS, 		//txn will be aborted
	P_A_SAVEWS,			//txnManager will catch exception so txn will be aborted
	P_A_COMMITRECV,		//txnManager decides; RM ask at recovery time
	C_B_VR,
	C_A_ONEREPLY,
	C_A_ALLREPLY,
	C_A_DECISION,
	C_A_ONECOMMIT,
	C_A_ALLCOMMIT
}
 
