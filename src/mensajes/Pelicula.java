package mensajes;

import java.io.Serializable;

public class Pelicula implements Serializable{
	private static final long serialVersionUID = 1L;
	private String titulo;
	private String descripcion;
	
	public Pelicula(String titulo, String descripcion) {
		this.titulo = titulo;
		this.descripcion = descripcion;
	}
	
	public String getTitulo() {
		return titulo;
	}
	public String getDescripcion() {
		return descripcion;
	}
}
