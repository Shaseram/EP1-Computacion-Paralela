package client;

import java.rmi.NotBoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.rmi.RemoteException;


public class RunClient {

	public static void main(String[] args) throws RemoteException, NotBoundException, IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		
		Client client = new Client();
		client.startClient();
		
		System.out.println("Cliente Arriba!");
		
		System.out.println("=============================================");
		System.out.println("      SISTEMA DE GESTION INVENTARIO Y VENTA      ");
		System.out.println("=============================================");
		
		
		while(true) {
			System.out.println(" ---------------------------------------");
			System.out.println(" -> Indique la opcion a operar");
			System.out.println("0. Proceder con Venta.");
			System.out.println("1. Mostrar licores de la BD.");
			System.out.println("2. Crear nuevo Licor.");
			System.out.println("3. Terminar conexión y cerrar programa.");
			System.out.println("Ingrese su opción");
			String entrada = br.readLine();
			int opcion = Integer.parseInt(entrada);
			
			while(opcion > 3 || opcion < 0) {
				System.out.println("Entrada inválida, ingrese una opción válida");
				entrada = br.readLine();
				opcion = Integer.parseInt(entrada);
			}
			
			switch(opcion) {
				case 0:
					client.venta();
					break;
				case 1:
					client.mostrarLicores();
					
					break;
				case 2:
					
					System.out.println("Ingrese el nombre del licor a registrar:");
					String nombre = br.readLine();
					
					System.out.println("Ingrese el tipo del licor a registrar:");
					String tipo = br.readLine();
					
					System.out.println("Ingrese el stock del licor a registrar:");
					String stock = br.readLine();
					
					System.out.println("Ingrese el proveedor del licor a registrar:");
					String proveedor = br.readLine();
					
					System.out.println("Ingrese el precio del licor a registrar:");
					String precio = br.readLine();
					
					client.crearRegistro(nombre, tipo, Integer.parseInt(stock), proveedor, Double.parseDouble(precio));
					break;
				
				case 3:
					System.out.println("Saliendo del programa...");
					System.exit(0);
					break;
				
			}
			
		}
		
	}
	
}
