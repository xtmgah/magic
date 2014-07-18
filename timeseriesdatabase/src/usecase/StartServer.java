package usecase;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import timeseriesdatabase.TimeSeriesDatabase;
import timeseriesdatabase.TimeSeriesDatabaseFactory;
import timeseriesdatabase.server.TSDServer;
import timeseriesdatabase.server.TSDServerInterface;

public class StartServer {
	
	public static final String SERVER_NAME = "tsdserver";

	public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException {
		System.out.println("start...");
		
		TimeSeriesDatabase timeSeriesDatabase = TimeSeriesDatabaseFactory.createDefault();

        try {
            TSDServer tsdserver = new TSDServer(timeSeriesDatabase);
            TSDServerInterface stub = (TSDServerInterface) UnicastRemoteObject.exportObject(tsdserver, 0);
            System.out.println("create registry...");
            Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            System.out.println("bind tsdserver...");
            registry.rebind(SERVER_NAME, stub);
            System.out.println("...tsdserver bound");
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

}
