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
    private ArrayList<Licor> listaVenta = new ArrayList<>();
    private static InterfazDeServer stub;
    private String host = "localhost";
    private int primaryPort = 7777;
    private int backupPort = 7778;
    private boolean connectedToPrimary = true;
    private boolean running = true;

    public Client() throws RemoteException, NotBoundException {
        conectarSvPrincipal();
        startHeartbeat();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                    
                    Thread.sleep(1000);
                 
                    if (connectedToPrimary) {
                        stub.heartbeat();
                    }
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("Heartbeat fallido, cambiando al servidor de respaldo...");
                        cambiarSvRespaldo();
                    } else {
                        System.err.println("Heartbeat fallido en el servidor de respaldo. Terminando ejecución...");
                        terminarEjecucion();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Heartbeat interrumpido: " + e.getMessage());
                }
            }
        }).start();
    }

    private void conectarSvPrincipal() throws RemoteException, NotBoundException {
        stub = establecerConexion(host, primaryPort, "server");

        if (stub == null) {
            System.out.println("Conectando al servidor de respaldo...");
            cambiarSvRespaldo();
        } else {
            connectedToPrimary = true;
        }

        if (stub == null) {
            throw new RemoteException("No se pudo conectar a ningún servidor.");
        }
    }

    private void cambiarSvRespaldo() {
        stub = establecerConexion(host, backupPort, "server");
        if (stub == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
            terminarEjecucion();
        } else {
            System.out.println("Conectado al servidor de respaldo.");
            connectedToPrimary = false;
        }
    }

    private InterfazDeServer establecerConexion(String host, int port, String bindingName) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazDeServer) registry.lookup(bindingName);
        } catch (Exception e) {
            System.err.println("Excepción conectando a " + bindingName + ": " + e.toString());
            return null;
        }
    }

    private void terminarEjecucion() {
        running = false;
        System.err.println("Terminando ejecución debido a la falta de respuesta del servidor de respaldo.");
        System.exit(1);
    }


    public void venta() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("----------------------------");
        System.out.println("    SECCIÓN DE VENTA      ");
        System.out.println("----------------------------");
        double valorTotal = 0.0;
        while (true) {
            System.out.println("Licores seleccionados para venta:");
            System.out.println(" ");

            for (Licor licor : listaVenta) {
                System.out.println("ID: " + licor.getId() + " " + "Licor: " + licor.getNombre() + " " + "Precio: " + licor.getPrecio());
            }
            System.out.println(" ");
            System.out.println("Valor Total: " + valorTotal);
            System.out.println(" ");
            System.out.println("Ingrese operacion a seguir");
            System.out.println("1. Búqueda de licor");
            System.out.println("2. Eliminar licor de la venta");
            System.out.println("3. Término de venta");
            String entrada = br.readLine();
            int opcion = Integer.parseInt(entrada);

            switch (opcion) {
                case 1:
                    try {
                        System.out.println("Ingrese el nombre del licor a buscar");
                        String nombre = br.readLine();
                        
                        
                        ArrayList<Licor> posiblesResultados;
                        try {
                            posiblesResultados = stub.buscarLicor(nombre);
                        } catch (RemoteException e) {
                            cambiarSvRespaldo();
                            posiblesResultados = stub != null ? stub.buscarLicor(nombre) : new ArrayList<>();
                        }
                        

                        if (posiblesResultados != null && posiblesResultados.size() > 0) {
                            for (Licor licor : posiblesResultados) {
                                System.out.println("Id: " + licor.getId() + " Nombre: " + licor.getNombre() + " Stock: " + licor.getStock() + " Precio: " + licor.getPrecio());
                                System.out.println("");
                            }

                            System.out.println("Elige el licor de la busqueda por su ID");
                            String option = br.readLine();

                            for (Licor licor : posiblesResultados) {
                                if (licor.getId() == Integer.parseInt(option)) {
                                    if (licor.getStock() > 0) {
                                        
                                        Object[] valores_api;
                                        try {
                                            valores_api = stub.verificarPromocion(Integer.parseInt(option));
                                        } catch (RemoteException e) {
                                            cambiarSvRespaldo();
                                            valores_api = stub != null ? stub.verificarPromocion(Integer.parseInt(option)) : null;
                                        }
                                        

                                        if (valores_api != null) {
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
                                        listaVenta.add(licor);
                                    } else {
                                        System.out.println("No hay stock de ese producto.");
                                    }
                                }
                            }
                        } else {
                            System.out.println("No existen licores que coincidan con la busqueda");
                        }
                    } catch(RemoteException e) {
                        System.err.println("No se pudo completar la operación. El servidor de respaldo tampoco responde.");
                    }
                    break;

                case 2:
                    System.out.println("Elige el licor a eliminar de la venta por su ID");
                    String op = br.readLine();
                    Licor aEliminar = null;
                    for (Licor licor : listaVenta) {
                        if (licor.getId() == Integer.parseInt(op)) {
                           aEliminar = licor;
                           break;
                        }
                    }
                    if(aEliminar != null){
                        valorTotal -= aEliminar.getPrecio();
                        listaVenta.remove(aEliminar);
                        System.out.println("Licor eliminado con éxito!");
                        System.out.println(" ");
                        System.out.println("--------------------------------");
                    }
                    break;

                case 3:
                    ArrayList<Integer> idVentas = new ArrayList<>();
                    for (Licor licor : listaVenta) {
                        idVentas.add(licor.getId());
                    }
                    
                
                    try {
                        stub.actualizarBD(idVentas);
                    } catch (RemoteException e) {
                        cambiarSvRespaldo();
                        if (stub != null) stub.actualizarBD(idVentas);
                    }
                   
                    
                    listaVenta.clear();
                    System.out.println("Venta finalizada, stock actualizado");
                    return;
            }
        }
    }

    public void mostrarLicores() throws RemoteException {
        ArrayList<Licor> licores;
  
        try {
            licores = stub.getLicor();
        } catch (RemoteException e) {
            cambiarSvRespaldo();
            licores = stub != null ? stub.getLicor() : new ArrayList<>();
        }

        if (licores != null) {
            for (Licor licor : licores) {
            	System.out.println("ID:" + licor.getId());
                System.out.println("Nombre:" + licor.getNombre());
                System.out.println("Tipo:" + licor.getTipo());
                System.out.println("Stock:" + licor.getStock());
                System.out.println("Proveedor:" + licor.getProveedor());
                System.out.println("Precio:" + licor.getPrecio());
                System.out.println("");
            }
        }
    }

    public void crearRegistro(String nombre, String tipo, int stock, String proveedor, double precio) throws RemoteException {
        try {
            stub.CrearLicor(nombre, tipo, stock, proveedor, precio);
        } catch (RemoteException e) {
            cambiarSvRespaldo();
            if (stub != null) {
                stub.CrearLicor(nombre, tipo, stock, proveedor, precio);
            } else {
                 throw e;
            }
        }
        System.out.println("Registro creado existosamente!");
    }
    
    public void actualizarRegistro() throws RemoteException, IOException {
    	mostrarLicores();
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	System.out.println("Elija por ID el licor a actualizar");
    	String id = br.readLine();
    	
    	System.out.println("Indique que parametro actualizar");
    	System.out.println("1. Nombre");
    	System.out.println("2. Stock");
    	System.out.println("3. Precio");
    	String opcion = br.readLine();
    	
    	switch(Integer.parseInt(opcion)) {
	    	case 1:
	    		System.out.println("Ingrese el nuevo Nombre");
	    		String nombre = br.readLine();
	    		stub.actualizarLicor(id, opcion, nombre);
	    		
	    		System.out.println("Nombre actualizado con Éxito!");
	    		
	    		break;
	    	case 2:
	    		System.out.println("Ingrese el nuevo Stock");
	    		String stock = br.readLine();
	    		stub.actualizarLicor(id, opcion, stock);
	    		
	    		System.out.println("Stock actualizado con Éxito!");
	    		break;
	    		
	    	case 3:
	    		System.out.println("Ingrese el nuevo Precio");
	    		String precio = br.readLine();
	    		stub.actualizarLicor(id, opcion, precio);
	    		
	    		System.out.println("Precio actualizado con Éxito!");
	    		
	    		break;
    	}
    	
    	return;
    	
    }
    
    public void eliminarRegistro() throws RemoteException, IOException {
    	mostrarLicores();
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	System.out.println("Elija por ID el licor a eliminar");
    	String id = br.readLine();
    	
    	stub.eliminarLicor(id);
    	
    	System.out.println("Licor eliminado con Éxito!!");
    	
    	return;
    	
    }
}