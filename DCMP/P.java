package DCMP;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class P implements PRMI {
    ReentrantLock mutex;
    public int id;
    //mpref[i] = woman id of rank i
    public int[] mpref;
    //K = STEP V = List of ConflictPairs at this point
    public HashMap<Integer, LinkedList<ConflictPair>> prerequisite;
    public int curIdx;
    public String[] peers;
    public int[] ports;
    public int nsize;
    Registry registry;
    PRMI stub;
    private int n;
    boolean isActive = false;
    int D = 0;
    int parent = -1;


    public P(int id, int[] mpref, HashMap<Integer, LinkedList<ConflictPair>> prerequisite, String[] peers, int[] ports) {
        this.id = id;
        this.mpref = mpref;
        this.prerequisite = prerequisite;
        curIdx = -1;
        this.peers = peers;
        this.ports = ports;
        this.nsize = peers.length / 2;
        try {
            System.setProperty("java.rmi.server.hostname", this.peers[this.id]);
            registry = LocateRegistry.createRegistry(this.ports[this.id]);
            stub = (PRMI) UnicastRemoteObject.exportObject(this, this.ports[this.id]);
            registry.rebind("DCMP", stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.n = mpref.length;
        this.mutex = new ReentrantLock();
    }

    public synchronized void InitHandler(Request req) {
        isActive = true;
//        System.out.println("    man " + this.id + " is in Init handler requested from woman " + req.myId);
        if (parent == -1) {
           // parent = req.myId;
            if(req.gender!='q')
                parent = req.myId;
            else
                parent = req.myId + this.nsize;
//            System.out.println("        man"+ this.id +"'s parent is:"+this.parent);
        } else {
            Runnable r = new PClient("Signal", new Request(this.id, curIdx, 'p'), req.myId, this.ports);
            new Thread(r).start();
        }
        D++;
        Runnable r = new PClient("Propose", new Request(this.id, curIdx++,'p'), this.mpref[0], this.ports);
        new Thread(r).start();
        isActive = false;
    }

    @Override
    public synchronized void RejectHandler(Request req) throws RemoteException {
        isActive = true;
//        System.out.println("    man " + this.id + " is in Rejection handler requested from woman " + req.myId);
        boolean justJoinedTree = false;
        if (parent == -1) {
           // parent = req.myId;
            parent = req.myId + this.nsize;
//            System.out.println("        man"+ this.id +"'s parent is:"+this.parent);
            //justJoinedTree = true;

        } else {
            Runnable r = new PClient("Signal", new Request(this.id, curIdx,'q'), req.myId, this.ports);
            new Thread(r).start();
        }

        if (curIdx != -1 && mpref[curIdx] == req.myId) {
            if (curIdx == n - 1) {
                // do i need to broadcast this message?
                System.out.println("no constrained stable marriage possible");
                Runnable r = new PClient("Fail", new Request(id, curIdx,'e'), 2*nsize, this.ports);
                new Thread(r).start();
                return;

            } else {
                curIdx++;
                if (prerequisite != null && this.prerequisite.containsKey(curIdx)) {
                    LinkedList<ConflictPair> conflictPairs = this.prerequisite.get(curIdx);
                    if (conflictPairs != null) {
                        for (ConflictPair cp : conflictPairs) {
                            D++;
                            Runnable r = new PClient("Advance", new Request(id, cp.regret,'p'), cp.pId, this.ports);
                            new Thread(r).start();
                        }
                    }
                }
                D++;
                Runnable r = new PClient("Propose", new Request(id, curIdx,'q'), mpref[curIdx], this.ports);
                new Thread(r).start();

            }
        }
        if(this.D == 0 && this.parent!=-1){
            char toGender;
            int toId;
            if(parent<nsize) {
                toGender = 'p';
                toId = this.parent;
            }
            else if(parent==2*nsize) {
                toGender = 'e';
                toId = this.parent;
            }
            else {
                toGender = 'q';
                toId = parent - nsize;
            }
            Runnable r = new PClient("Signal", new Request(this.id, curIdx, toGender), toId, this.ports);
//            System.out.println("            man "+this.id+ " will leave the tree by signaling "+ this.parent);
            new Thread(r).start();
            parent = -1;
        }
        isActive = false;
    }

    @Override
    public synchronized void AdvanceHandler(Request req) throws RemoteException {
//        System.out.println("    man " + this.id + " is in Advance handler requested from woman " + req.myId);
        isActive = true;
        if (parent == -1) {
           // parent = req.myId;
            parent = req.myId;
//            System.out.println("       man"+ this.id +"'s parent is:"+this.parent);
        } else {
            Runnable r = new PClient("Signal", new Request(this.id, curIdx, 'p'), req.myId, this.ports);
            new Thread(r).start();
        }
        while (curIdx < req.regret) {
            curIdx++;
            if (prerequisite != null && prerequisite.containsKey(curIdx)) {
                LinkedList<ConflictPair> conflictPairs = prerequisite.get(curIdx);
                if (conflictPairs != null) {
                    for (ConflictPair cp : conflictPairs) {
                        D++;
                        Runnable r = new PClient("Advance", new Request(id, cp.regret,'p'), cp.pId, this.ports);
                        new Thread(r).start();
                    }
                }
            }
        }
        D++;
        Runnable r1 = new PClient("Propose", new Request(id, curIdx,'q'), mpref[curIdx], this.ports);
        new Thread(r1).start();

        if(this.D == 0 && this.parent!=-1){
            char toGender;
            int toId;
            if(parent<nsize) {
                toGender = 'p';
                toId = this.parent;
            }
            else if(parent==2*nsize) {
                toGender = 'e';
                toId = this.parent;
            }
            else {
                toGender = 'q';
                toId = parent - nsize;
            }
            Runnable r = new PClient("Signal", new Request(this.id, curIdx, toGender), toId, this.ports);
//            System.out.println("            man "+this.id+ " will leave the tree by signaling "+ this.parent);
            new Thread(r).start();
            parent = -1;
        }
        isActive = false;
    }

    @Override
    public synchronized void SignalHandler(Request req) throws RemoteException {
        D--;
        if (!isActive && D == 0 && parent != -1) {
            char toGender;
            int toId;
            if(parent<nsize) {
                toGender = 'p';
                toId = this.parent;
            }
            else if(parent==2*nsize) {
                toGender = 'e';
                toId = this.parent;
            }
            else {
                toGender = 'q';
                toId = parent - nsize;
            }
            Runnable r = new PClient("Signal", new Request(this.id, curIdx, toGender), toId, this.ports);
//            System.out.println("            man "+this.id+ " will leave the tree by signaling "+ this.parent);
            new Thread(r).start();
            parent = -1;
        }
    }

    public void Kill() {
        if (this.registry != null) {
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch (Exception e) {
                System.out.println("None reference");
            }
        }
    }

}
