package DCMP;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PClient implements Runnable {
    public String rmi = "";
    public Request request = null;
    public int toId = -1;
    public int[] ports;
    public int nsize;

    public PClient(String rmi, Request request, int toId, int[] ports) {
        this.rmi = rmi;
        this.request = request;
        this.toId = toId;
        this.ports = ports;
        this.nsize = ports.length / 2;
    }

    public void Call(String rmi, Request req, int id) {
        Registry registry = null;
        try {
            if (rmi.equals("Advance")) {
                registry = LocateRegistry.getRegistry(this.ports[id]);
                PRMI stub = (PRMI) registry.lookup("DCMP");
                System.out.println("man " + req.myId + " call Advance to man " + id);
                stub.AdvanceHandler(req);
            } else if (rmi.equals("Signal")) {
                char gender = req.gender;
                if (gender=='p') {
                    registry = LocateRegistry.getRegistry(this.ports[id]);
                    System.out.println("man " + req.myId + " call Signal to man " + id);
                    PRMI stub = (PRMI) registry.lookup("DCMP");
                    stub.SignalHandler(req);
                } else if (gender=='q') {
                    registry = LocateRegistry.getRegistry(this.ports[this.nsize+id]);
                    System.out.println("man " + req.myId + " call Signal to woman " + id);
                    QRMI stub = (QRMI) registry.lookup("DCMP");
                    stub.SignalHandler(req);
                } else {
                    registry = LocateRegistry.getRegistry(this.ports[2*this.nsize]);
                    System.out.println("man " + req.myId + " call Signal to env.");
                    ERMI stub = (ERMI) registry.lookup("DCMP");
                    stub.SignalHandler(req);
                }
            } else if (rmi.equals("Propose")) {
                registry = LocateRegistry.getRegistry(this.ports[nsize + id]);
                QRMI stub = (QRMI) registry.lookup("DCMP");
                System.out.println("man " + req.myId + " call Propose to woman " + id);
                stub.ProposalHandler(req);
            } else if(rmi.equals("Fail")){
                registry = LocateRegistry.getRegistry(this.ports[2*nsize]);
                ERMI stub = (ERMI) registry.lookup("DCMP");
                stub.FailHandler(req);
            }
            else {
                System.out.println("Wrong parameter.");
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void run() {
        try {
            Call(rmi, request, toId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
