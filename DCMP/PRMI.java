package DCMP;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PRMI extends Remote {
    void InitHandler(Request req) throws RemoteException;
    void RejectHandler(Request req) throws RemoteException;
    void AdvanceHandler(Request req) throws RemoteException;
    void SignalHandler(Request req) throws RemoteException;
}