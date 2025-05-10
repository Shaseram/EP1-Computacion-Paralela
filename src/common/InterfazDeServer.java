package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote{
	public ArrayList<Licor> getLicor() throws RemoteException;
	public Licor CrearLicor(int id, String nombre, String tipo, int stock, String proveedor) throws RemoteException;
	String getDataFromApi() throws RemoteException;
}