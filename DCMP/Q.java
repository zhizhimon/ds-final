package DCMP;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Q implements QRMI {
    ReentrantLock mutex;
    int id;
    // <K, V> = <MAN_ID, RANK OF THE MAN>
    public HashMap<Integer, Integer> rank;
    int partner;
    String[] peers;
    int[] ports;
    int nsize;
    Registry registry;
    QRMI stub;

    public boolean isActive;
    public int D;
    public int parent;

    public Q(int id, HashMap<Integer, Integer> rank, String[] peers, int[] ports){
        this.id = id;
        this.rank = rank;
        this.partner = -1;
        this.peers = peers;
        this.ports = ports;
        this.nsize = peers.length/2;
        this.mutex = new ReentrantLock();
        this.D = 0;
        this.parent = -1;
        this.isActive = false;
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[nsize+this.id]);
            registry = LocateRegistry.createRegistry(this.ports[nsize+this.id]);
            stub = (QRMI) UnicastRemoteObject.exportObject(this, this.ports[nsize+this.id]);
            registry.rebind("DCMP", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public Response Call(String rmi, Request req, int id){
       // Response callReply = null;
        PRMI stub;
        try{
            if(rmi.equals("Reject")) {
                Registry registry=LocateRegistry.getRegistry(this.ports[id]);
                stub=(PRMI) registry.lookup("DCMP");
                System.out.println("woman "+req.myId+" call Reject to man "+id);
                stub.RejectHandler(req);
            }
            else if(rmi.equals("Signal")){
                int portId;

                if(this.parent>=this.nsize && this.parent<2*this.nsize){
                    portId = id + this.nsize;
                    System.out.println("woman "+req.myId+" call Signal to woman "+id);
                }
                else {
                    portId = id;
                    System.out.println("woman "+req.myId+" call Signal to man "+id);
                }
                Registry registry=LocateRegistry.getRegistry(this.ports[portId]);
                stub=(PRMI) registry.lookup("DCMP");
                System.out.println("woman "+req.myId+" call Signal to man "+id);
                stub.SignalHandler(req);
            }
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return null;
    }

    @Override
    public synchronized Response ProposalHandler(Request req) throws RemoteException {
//        System.out.println("    woman "+this.id + " is in Propose handler requested from man "+req.myId);
//      System.out.println("        partner:"+this.partner+ "(rank"+rank.get(this.partner)+") thisman:"+req.myId+ "(rank"+ rank.get(req.myId)+")");

        if(this.parent==-1){
            this.isActive = true;
            this.parent = req.myId;
//            System.out.println("        woman"+ this.id +"'s parent is:"+this.parent);
        }
        else {
            Call("Signal",new Request(this.id, -1,'q'),req.myId);
        }

        int thisMan = req.myId;
            if (this.partner == -1) {
                this.partner = thisMan;

            }
            else if (rank.get(this.partner) < rank.get(thisMan)) {
//                System.out.println("        woman"+ this.id+ " will reject man "+thisMan);
                Call("Reject", new Request(this.id,-1, 'q'),thisMan);
                this.D = this.D+1;

            } else {
//                System.out.println("        woman"+ this.id+ " will reject man "+ this.partner);
                Call("Reject", new Request(this.id, -1, 'q'),this.partner);//???
                this.partner = thisMan;
                this.D = this.D+1;
            }
        this.isActive = false;
        if(this.D == 0 && this.parent!=-1 && this.isActive ==false){
            Call("Signal",new Request(this.id, -1,'q'),this.parent);
            this.parent = -1;
        }

            return new Response(true);

    }

    @Override
    public synchronized Response SignalHandler(Request req) throws RemoteException {
        this.D = this.D - 1;
        if(this.D == 0 && this.parent!=-1 && this.isActive ==false){
            Call("Signal",new Request(this.id, -1,'q'),this.parent);
            this.parent = -1;
        }
        return null;
    }
    public void Kill(){
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }
}
