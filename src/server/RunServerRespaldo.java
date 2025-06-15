package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.InterfazDeServer;

public class RunServerRespaldo {
	
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		
		InterfazDeServer server = new ServerImplRespaldo();
		Registry registry = LocateRegistry.createRegistry(7778);
		registry.bind("server", server);
		
		System.out.println("Servidor de Respaldo Arriba!!");
		
	}

}
