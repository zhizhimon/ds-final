package DCMP;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class E implements ERMI {
     public int id;
     public boolean isTerminate;
     public int D;
     public boolean isSuccess;
     //int parent;
     public String[] peers;
     public int[] ports;
     Registry registry;
     ERMI stub;
     int nsize ;

     public void Start(){
        //Response callReply = null;
        Registry registry = null;
        try{
            for(int i=0;i<nsize;i++){
                registry= LocateRegistry.getRegistry(this.ports[i]);
                PRMI stub = (PRMI) registry.lookup("DCMP");
                System.out.println("env calls Init to man "+i);
                stub.InitHandler(new Request(this.id, -1, 'e'));
                D++;
            }

        } catch(Exception e){
            return;
        }
        return;
     }

     public E(int id, String[] peers, int[] ports){
         this.isTerminate = false;
         this.D = 0;
         this.ports = ports;
         this.peers = peers;
         this.id = id;
         this.nsize = peers.length/2;
         this.isSuccess = true;
         try{
             System.setProperty("java.rmi.server.hostname", this.peers[this.id]);
             registry = LocateRegistry.createRegistry(this.ports[this.id]);
             stub = (ERMI) UnicastRemoteObject.exportObject(this, this.ports[this.id]);
             registry.rebind("DCMP", stub);
         } catch(Exception e){
             e.printStackTrace();
         }

     }
    @Override
    public synchronized Response SignalHandler(Request req) throws RemoteException{
         int childId = req.myId;
//         System.out.println("E is in SignalHandler requested from "+childId);
         this.D = this.D-1;
         if(this.D==0){
             this.isTerminate = true;
         }
         return new Response(true);
    }
    @Override
    public synchronized Response FailHandler(Request req) throws RemoteException {

//        System.out.println("E is in FailHandler requested from "+ req.myId);
        this.isTerminate = true;
        this.isSuccess = false;
        return null;
    }
}
