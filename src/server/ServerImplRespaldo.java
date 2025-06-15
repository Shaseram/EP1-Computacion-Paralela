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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock; // Importar ReentrantLock
import java.util.concurrent.TimeUnit; // Importar TimeUnit


public class ServerImplRespaldo implements InterfazDeServer {
    private String url = "jdbc:mysql://localhost:3308/botilleriadb";
    private String username = "root";
    private String password_BD = "";

    private final ReentrantLock databaseLock = new ReentrantLock(); // Instancia del bloqueo

    public ServerImplRespaldo() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);

    }

    public void actualizarBD(ArrayList<Integer> ides) {
        System.out.println("Intentando adquirir bloqueo para actualizar stock (respaldo)...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquirido bloqueo para actualizar stock (respaldo).");
                    try (Connection connection = DriverManager.getConnection(url, username, password_BD)) {

                        System.out.println("Actualizando stock en la base de datos (respaldo)...");

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
                        System.out.println("No se pudo hacer la conexión a la base de datos o actualizar el stock (respaldo).");
                    } finally {
                        System.out.println("Liberando bloqueo después de actualizar stock (respaldo)...");
                        databaseLock.unlock(); // Liberar el bloqueo
                    }
                } finally {
                 
                }
            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para actualizar stock (respaldo).");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para actualizar stock (respaldo): " + e.getMessage());
        }
    }


    public Object[] verificarPromocion(int id) {
        // Este método consulta una API externa, no la base de datos local,
        // por lo que no necesita exclusión mutua con las operaciones de BD.
        String output = null;

        try {
            URL apiUrl = new URI("https://api-ofertas-deploy.onrender.com/licores").toURL();

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();


                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                br.close();
                output = response.toString();
            } else {
                System.out.println("Error al conectar con la API. Codigo de respuesta: " + responseCode);
            }

        } catch (Exception e) {
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
                    return new Object[]{idApi, descuento};
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
        System.out.println("Intentando adquirir bloqueo para buscar licor (respaldo)...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquiriendo bloqueo para buscar licor (respaldo)...");


                    System.out.println("Analizando resultados...");
                    String sql = "SELECT * FROM licores WHERE LOWER(nombre) LIKE ?";

                    try (Connection connection = DriverManager.getConnection(url, username, password_BD);
                         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

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

                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("No se pudo hacer la conexion a la base de datos");

                    } finally {
                      
                    }


                } finally {
                    System.out.println("Liberando bloqueo después de buscar licor (respaldo)...");
                    databaseLock.unlock(); // Liberar el bloqueo
                }

            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para buscar licor (respaldo).");
                return new ArrayList<>(); // Return an empty list
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para buscar licor (respaldo): " + e.getMessage());
            return new ArrayList<>(); // Return an empty list
        }
        return resultadoBD;

    }

    private ArrayList<Licor> mostrarLicores() {
        System.out.println("Intentando adquirir bloqueo para mostrar licores (respaldo)...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquiriendo bloqueo para mostrar licores (respaldo)...");
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

                    
                        while (resultados.next()) {
                            int id = resultados.getInt("id");
                            String nombre = resultados.getString("nombre");
                            String tipo = resultados.getString("tipo");
                            int stock = resultados.getInt("stock");
                            String proveedor = resultados.getString("proveedor");
                            int precio = resultados.getInt("precio");

                            Licor newLicor = new Licor(id, nombre, tipo, stock, proveedor, precio);

                            BD_licores_copia.add(newLicor);


                        }


                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("No se pudo hacer la conexion a la base de datos");

                    } finally {
                        try {
                            if (resultados != null) resultados.close();
                            if (query != null) query.close();
                            if (connection != null) connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }


                    return BD_licores_copia;
                } finally {
                    System.out.println("Liberando bloqueo después de mostrar licores...");
                    databaseLock.unlock(); // Liberar el bloqueo
                }
            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para mostrar licores.");
                return new ArrayList<>();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para mostrar licores: " + e.getMessage());
            return new ArrayList<>();
        }

    }

    public ArrayList<Licor> getLicor() {
        return mostrarLicores(); // showLicores ya tiene el bloqueo
    }

    public void CrearLicor(String nombre, String tipo, int stock, String proveedor, double precio) {
        System.out.println("Intentando adquirir bloqueo para crear licor...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquiriendo bloqueo para crear licor...");

                    System.out.println("Insertando nuevo licor en la base de datos...");

                    String sql = "INSERT INTO licores (nombre, tipo, stock, proveedor, precio) VALUES (?, ?, ?, ?, ?)";

                    try (Connection connection = DriverManager.getConnection(url, username, password_BD);
                         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                        preparedStatement.setString(1, nombre);
                        preparedStatement.setString(2, tipo);
                        preparedStatement.setInt(3, stock);
                        preparedStatement.setString(4, proveedor);
                        preparedStatement.setDouble(5, precio);

                        preparedStatement.executeUpdate();

                        System.out.println("Licor insertado correctamente.");

                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("No se pudo insertar el licor en la base de datos.");
                    }


                } finally {
                    System.out.println("Liberando bloqueo después de crear licor...");
                    databaseLock.unlock(); // Liberar el bloqueo
                }
            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para crear licor.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para crear licor: " + e.getMessage());
        }
    }

    public void actualizarLicor(String id, String opcion, String nuevoValor) {
        System.out.println("Intentando adquirir bloqueo para actualizar licor...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquiriendo bloqueo para actualizar licor...");
                    String sql = null;
                    PreparedStatement preparedStatement = null;

                    System.out.println("Actualizando licor en BD...");

                    try (Connection connection = DriverManager.getConnection(url, username, password_BD)) {

                        switch (Integer.parseInt(opcion)) {

                            case 1:
                                sql = "UPDATE Licores SET nombre = ? WHERE id = ?";
                                preparedStatement = connection.prepareStatement(sql);

                                preparedStatement.setString(1, nuevoValor);
                                preparedStatement.setInt(2, Integer.parseInt(id));


                                preparedStatement.executeUpdate();

                                break;

                            case 2:
                                sql = "UPDATE Licores SET stock = ? WHERE id = ?";
                                preparedStatement = connection.prepareStatement(sql);


                                preparedStatement.setInt(1, Integer.parseInt(nuevoValor));
                                preparedStatement.setInt(2, Integer.parseInt(id));


                                preparedStatement.executeUpdate();

                                break;

                            case 3:
                                sql = "UPDATE Licores SET precio = ? WHERE id = ?";
                                preparedStatement = connection.prepareStatement(sql);

                                preparedStatement.setDouble(1, Double.parseDouble(nuevoValor));
                                preparedStatement.setInt(2, Integer.parseInt(id));


                                preparedStatement.executeUpdate();

                                break;
                        }


                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("No se pudo actualizar el licor en la base de datos.");
                    } finally {

                    }
                } finally {
                    System.out.println("Liberando bloqueo después de actualizar licor...");
                    databaseLock.unlock(); // Liberar el bloqueo
                }
            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para actualizar licor.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para actualizar licor: " + e.getMessage());
        }
    }

    public void eliminarLicor(String id) throws RemoteException {
        System.out.println("Intentando adquirir bloqueo para eliminar licor...");
        try {
            if (databaseLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Adquiriendo bloqueo para eliminar licor...");

                    System.out.println("Eliminando Licor en la BD...");

                    String sql = "DELETE FROM Licores WHERE id = ?";

                    try (Connection connection = DriverManager.getConnection(url, username, password_BD);
                         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                        preparedStatement.setInt(1, Integer.parseInt(id));


                        preparedStatement.executeUpdate();

                        System.out.println("Licor eliminado correctamente.");

                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("No se pudo eliminar el licor en la base de datos.");
                    }


                } finally {
                    System.out.println("Liberando bloqueo después de eliminar licor...");
                    databaseLock.unlock(); // Liberar el bloqueo
                }
            } else {
                System.out.println("Tiempo de espera excedido al intentar adquirir el bloqueo para eliminar licor.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupción al intentar adquirir el bloqueo para eliminar licor: " + e.getMessage());
        }

    }


    @Override
    public int heartbeat() throws RemoteException {
        // El heartbeat no accede a la base de datos, no necesita bloqueo.
        return 0;
    }
}