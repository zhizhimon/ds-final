package DCMP;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ERMI extends Remote{
    Response SignalHandler(Request req) throws RemoteException;
    Response FailHandler(Request req) throws RemoteException;
}
