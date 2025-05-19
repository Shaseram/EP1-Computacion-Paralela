package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.InterfazDeServer;
import common.Licor;




public class Client {
	private InterfazDeServer server;
	private ArrayList<Licor> listaVenta = new ArrayList<>();
	
	public Client() {}

	public void startClient() throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry("localhost", 7777);
		server = (InterfazDeServer) registry.lookup("server");
	}
	
	public void venta() throws RemoteException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("----------------------------");
		System.out.println("    SECCIÓN DE VENTA      ");
		System.out.println("----------------------------");
		double valorTotal = 0.0;
		while(true) {
			System.out.println("Licores seleccionados para venta:");
			System.out.println(" ");
			
			for(Licor licor : listaVenta) {
				System.out.println("ID: " + licor.getId() + " " + "Licor: " + licor.getNombre() + " " + "Precio: " + licor.getPrecio());
				
			}
			System.out.println(" ");
			
			if (valorTotal == 0.0) {
				System.out.println("Valor Total: " + valorTotal);
			} else {
				System.out.println("Valor Total: " + valorTotal);
			}
			
			System.out.println(" ");
			
			System.out.println("Ingrese operacion a seguir");
			System.out.println("1. Búqueda de licor");
			System.out.println("2  Eliminar licor de la venta");
			System.out.println("3. Término de venta");
			String entrada = br.readLine();
			int opcion = Integer.parseInt(entrada);
			
			switch(opcion) {
				case 1:
					System.out.println("Ingrese el nombre del licor a buscar");
					String nombre = br.readLine();
					ArrayList<Licor> posiblesResultados = server.buscarLicor(nombre);
					if(posiblesResultados.size() > 0) {
						for(Licor licor: posiblesResultados) {
							System.out.println("Id: " + licor.getId() + " Nombre: " + licor.getNombre() + " Stock: "+ licor.getStock() + " Precio: " + licor.getPrecio());
							
							System.out.println("");
							
						}
						
						
						System.out.println("Elige el licor de la busqueda por su ID");
						String option = br.readLine();
						
						for(Licor licor: posiblesResultados) {
							if(licor.getId() == Integer.parseInt(option) && licor.getStock() > 0) {
								if(licor.getStock()>0) {
									
									Object[] valores_api = server.verificarPromocion(Integer.parseInt(option));
									
									if(valores_api != null) {
										int descuento = (int) valores_api[1];
										double precioOriginal = licor.getPrecio();
										double valorFinal = precioOriginal - (precioOriginal * (descuento / 100.0));
										
										System.out.println("Licor con descuento!");
										System.out.println(" ");
										System.out.println("Descuento: " + descuento);
										System.out.println("Valor antes: " + precioOriginal);
										System.out.println("Valor final: " + valorFinal);
										System.out.println(" ");
										
										valorTotal += valorFinal;
										licor.setPrecio(valorFinal);
										
									} else {
										valorTotal += licor.getPrecio();
									}
								}
								else {
									System.out.println("No hay stock de ese producto.");
								}
								listaVenta.add(licor);
							}
						}
						
					} else {
						System.out.println("No existen licores que coincidan con la busqueda");
					}
					
					
							
					break;
				
				case 2:
					
					System.out.println("Elige el licor a eliminar de la venta por su ID");
					String op = br.readLine();
					
					for(Licor licor: listaVenta) {
						if(licor.getId() == Integer.parseInt(op)) {
							valorTotal -= licor.getPrecio();
							listaVenta.remove(licor);
							System.out.println("Licor eliminado con éxito!");
							System.out.println(" ");
							System.out.println("--------------------------------");
							break;
						}
					}
					
					break;
					
				case 3:
					// Llamar al servidor y descontar el stock de los productos 
					ArrayList<Integer> idVentas = new ArrayList<>();
					for(Licor licor : listaVenta) {
						idVentas.add(licor.getId());
					}
					
					server.actualizarBD(idVentas);
					listaVenta.clear();
					
					System.out.println("Venta finalizada, stock actualizado");
					
					
					return;
			}
		}
	}

	
	public void mostrarLicores() throws RemoteException {
		ArrayList<Licor> Licores = server.getLicor();
		for(Licor licor: Licores) {
			System.out.println("Nombre:"+licor.getNombre());
			System.out.println("Tipo:"+licor.getTipo());
			System.out.println("Stock:"+licor.getStock());
			System.out.println("Proveedor:"+licor.getProveedor());
			System.out.println("Precio:"+licor.getPrecio());
			System.out.println("");
		}
	}
	
	public void crearRegistro(String nombre, String tipo, int stock, String proveedor, double precio) throws RemoteException {
		server.CrearLicor(nombre, tipo, stock, proveedor, precio);
		
		System.out.println("Registro creado existosamente!");
		
	}
}