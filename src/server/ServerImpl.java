package server;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.InterfazDeServer;
import common.Licor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ServerImpl implements InterfazDeServer {
	private String url = "jdbc:mysql://localhost:3308/botilleriadb";
	private String username = "root";
	private String password_BD = "";
	
	public ServerImpl() throws RemoteException {
		UnicastRemoteObject.exportObject(this,0);
		
	}
	
	public void actualizarBD(ArrayList<Integer> ides) {
	    try (Connection connection = DriverManager.getConnection(url, username, password_BD)) {

	        System.out.println("Actualizando stock en la base de datos...");

	        String sql = "UPDATE licores SET stock = stock - 1 WHERE id = ?";

	        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

	            for (Integer id : ides) {
	                preparedStatement.setInt(1, id);
	                preparedStatement.executeUpdate();
	            }

	            System.out.println("Stock actualizado correctamente para los IDs: " + ides);

	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("No se pudo hacer la conexión a la base de datos o actualizar el stock.");
	    }
	}
	
	
	public Object[] verificarPromocion(int id) {
		String output = null;
		
		try {
			URL apiUrl = new URI("https://api-ofertas-deploy.onrender.com/licores").toURL();
			
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
			
			connection.setRequestMethod("GET");
			
			int responseCode = connection.getResponseCode();
			
			if(responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				
				
				while((inputLine = br.readLine()) != null) {
					response.append(inputLine);
				}
				
				br.close();
				output = response.toString();
			} else {
				System.out.println("Error al conectar con la API. Codigo de respuesta: " + responseCode);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			System.out.println("Parseando json");
			JsonNode jsonNode = objectMapper.readTree(output);
			
			for (JsonNode oferta : jsonNode) {
			    int idApi = oferta.get("id_producto_bd").asInt();
			    System.out.println(idApi);
				
			    if (idApi == id) {
			        int descuento = oferta.get("descuento").asInt();
			        System.out.println(descuento);
			        return new Object[] {idApi, descuento};
			    }
			}
			
		
			
			
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();

		}
		
		System.out.println("Retornando null");
		return null;
		
	}


	public ArrayList<Licor> buscarLicor(String nombre) {
		ArrayList<Licor> resultadoBD = new ArrayList<>();
		
		try (Connection connection = DriverManager.getConnection(url, username, password_BD)){
				
			System.out.println("Analizando resultados...");
			String sql = "SELECT * FROM licores WHERE LOWER(nombre) LIKE ?";
			
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			
                String searchTermPattern = "%" + nombre.toLowerCase() + "%";

                preparedStatement.setString(1, searchTermPattern);

				
                try (ResultSet resultados = preparedStatement.executeQuery()) {
                	while (resultados.next()) {
                        int id = resultados.getInt("id");
                        String tipo = resultados.getString("tipo");
                        String nombreL = resultados.getString("nombre"); 
                        int stock = resultados.getInt("stock");
                        String proveedor = resultados.getString("proveedor");
                        int precio = resultados.getInt("precio"); 

             
                        Licor newLicor = new Licor(id, nombreL, tipo, stock, proveedor, precio);

                        resultadoBD.add(newLicor);
                    }
                	
                }
			}
			connection.close();
			
			
			
		} catch(SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo hacer la conexion a la base de datos");
			
		}
		return resultadoBD;
		
		
	}
	
	private ArrayList<Licor> mostrarLicores() {
		
		ArrayList<Licor> BD_licores_copia = new ArrayList<>();
		
		Connection connection = null;
		Statement query = null;
		// PreparedStatement test = null , Esto es para evitar SQL injection
		ResultSet resultados = null;
		
		try {
				
			connection = DriverManager.getConnection(url, username, password_BD);
			System.out.println("Conexion con la BD establecida con exito!");
			
			query = connection.createStatement();
			String sql = "SELECT * FROM licores";
			
			resultados = query.executeQuery(sql);
			
			while(resultados.next()) {
				int id = resultados.getInt("id");
				String nombre = resultados.getString("nombre");
				String tipo = resultados.getString("tipo");
				int stock = resultados.getInt("stock");
				String proveedor = resultados.getString("proveedor");
				int precio = resultados.getInt("precio");
				
				Licor newLicor = new Licor(id, nombre, tipo, stock, proveedor, precio);
				
				BD_licores_copia.add(newLicor);
				
				
			}
			

			connection.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo hacer la conexion a la base de datos");
			
		}
		
		return BD_licores_copia;
	}
	
	public ArrayList<Licor> getLicor() {
		return mostrarLicores();
	}
	
	public void CrearLicor(String nombre, String tipo, int stock, String proveedor, double precio) {
	    try (Connection connection = DriverManager.getConnection(url, username, password_BD)) {

	        System.out.println("Insertando nuevo licor en la base de datos...");

	        String sql = "INSERT INTO licores (nombre, tipo, stock, proveedor, precio) VALUES (?, ?, ?, ?, ?)";

	        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

	            preparedStatement.setString(1, nombre);
	            preparedStatement.setString(2, tipo);
	            preparedStatement.setInt(3, stock);
	            preparedStatement.setString(4, proveedor);
	            preparedStatement.setDouble(5, precio);

	            preparedStatement.executeUpdate();

	            System.out.println("Licor insertado correctamente.");

	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("No se pudo insertar el licor en la base de datos.");
	    }
	}
}
