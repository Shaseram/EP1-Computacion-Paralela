package common;

import java.io.Serializable;

public class Licor implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int id;
	private String nombre;
	private String tipo;
	private int stock;
	private String proveedor;
	private double precio;
	
	public Licor(int id, String nombre, String tipo, int stock, String proveedor, int precio) {
		this.id = id;
		this.nombre = nombre;
		this.tipo = tipo;
		this.stock = stock;
		this.proveedor = proveedor;
		this.precio = precio;
	}
	
	public double getPrecio() {
		return this.precio;
	}
	
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public String getNombre() {
		return this.nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
	
	//enviar como parametro al servidor, reciba nombre y devuelva x cosa
 	
}
