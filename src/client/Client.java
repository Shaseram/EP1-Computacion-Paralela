package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.InterfazDeServer;
import common.Licor;




public class Client {
	private InterfazDeServer server;
	
	public Client() {}

	public void startClient() throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry("localhost", 7777);
		server = (InterfazDeServer) registry.lookup("server");
	}

	
	public void mostrarPersonas() throws RemoteException {
		ArrayList<Licor> Licores = server.getLicor();
		for(Licor licor: Licores) {
			System.out.println("Nombre:"+licor.getNombre());
			System.out.println("Tipo:"+licor.getTipo());
			System.out.println("Stock:"+licor.getStock());
			System.out.println("Proveedor:"+licor.getProveedor());
			System.out.println("");
		}
		
	}
	
	public String getDataFromApi() throws RemoteException {
		return server.getDataFromApi();
	}
	
	public void crearRegistro(String nombre, String tipo, int stock, String proveedor) throws RemoteException {
		server.CrearLicor(10, nombre, tipo, stock, proveedor);
		
		System.out.println("Registro creado existosamente!");
		
	}
}
	
			
