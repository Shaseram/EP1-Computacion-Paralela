package server;

import java.util.ArrayList;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ServerImpl implements InterfazDeServer {
	
	private ArrayList<Licor> BD_licores_copia = new ArrayList<>();
	private String url = "jdbc:mysql://localhost:3308/botilleriadb";
	private String username = "root";
	private String password_BD = "";
	
	public ServerImpl() throws RemoteException {
		conectarBD();
		UnicastRemoteObject.exportObject(this,0);
		
	}

	
	private void conectarBD() {
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
				
				Licor newLicor = new Licor(id, nombre, tipo, stock, proveedor);
				
				BD_licores_copia.add(newLicor);
				
				
			}
			
			//System.out.println(BD_licores_copia.get(3).getId() + " " + BD_licores_copia.get(3).getNombre());

			
			connection.close();
			
			
		} catch(SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo hacer la conexion a la base de datos");
			
		}
	}
	
	public String getDataFromApi() {
		String output = null;
		
		// Agregar la API para la botilleria, una idea era Promociones.
		try {
			URL apiUrl = new URI("http://localhost:1234/movies").toURL();
			
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
		
		return output;
		
	}
	
	public ArrayList<Licor> getLicor() {
		return BD_licores_copia;
	}
	
	
	public Licor CrearLicor(int id, String nombre, String tipo, int stock, String proveedor) {
		Licor licor = new Licor(id, nombre, tipo, stock, proveedor);
		BD_licores_copia.add(licor);
		return licor;
	}
}
