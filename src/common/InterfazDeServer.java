package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote{
	public ArrayList<Licor> getLicor() throws RemoteException;
	public void actualizarBD(ArrayList<Integer> ides) throws RemoteException;
	public Object[] verificarPromocion(int id) throws RemoteException;
	public ArrayList<Licor> buscarLicor(String nombre) throws RemoteException;
	public void CrearLicor(String nombre, String tipo, int stock, String proveedor, double precio) throws RemoteException;
}