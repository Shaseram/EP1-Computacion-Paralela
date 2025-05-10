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
		
		System.out.println("=========================================");
		System.out.println("      BIENVENIDO AL NEGOCIO XXXXXXXX      ");
		System.out.println("=========================================");
		
		
		while(true) {
			System.out.println(" ---------------------------------------");
			System.out.println(" -> Indique la opcion a operar");
			System.out.println("1. Mostrar licores de la BD.");
			System.out.println("2. Crear nuevo Licor.");
			System.out.println("3. Obtener informacion de la API.");
			System.out.println("4. Terminar conexión y cerrar programa.");
			System.out.println("Ingrese su opción");
			String entrada = br.readLine();
			int opcion = Integer.parseInt(entrada);
			
			while(opcion > 3 || opcion < 1) {
				System.out.println("Entrada inválida, ingrese una opción válida");
				entrada = br.readLine();
			}
			
			switch(opcion) {
				case 1:
					client.mostrarPersonas();
					
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
					
					client.crearRegistro(nombre, tipo, Integer.parseInt(stock), proveedor);
					
					
					break;
				
				case 3:
					System.out.println(client.getDataFromApi());
					
					break;
				
					
				case 4:
					System.out.println("Saliendo del programa...");
					System.exit(0);
					break;
				
			}
			
		}
		
		
		
		
		
		
	}
	
}
