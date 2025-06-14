package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.InterfazDeServer;

public class RunServerRespaldo {
	
	public static void main(String[] args) throws RemoteException, AlreadyBoundException {
		
		InterfazDeServer server = new ServerImpl();
		Registry registry = LocateRegistry.createRegistry(7778);
		registry.bind("server-respaldo", server);
		
		System.out.println("Servidor de Respaldo Arriba!!");
		
	}

}
