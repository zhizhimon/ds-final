package DCMP;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QRMI extends Remote {
    Response ProposalHandler(Request req) throws RemoteException;
    Response SignalHandler(Request req) throws RemoteException;
}

