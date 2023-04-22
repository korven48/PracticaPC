package server;


import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import mensajes.Pelicula;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String ip;
	private String[] peliculas; // lista de ids de las peliculas
	// informacion compartida
	
	public Usuario(String id, String ip, String[] peliculas) {
		super();
		this.id = id;
		this.ip = ip;
		this.peliculas = peliculas;
	}

	public String getId() {
		return id;
	}
	
	public String getIp() {
		return ip;
	}
	
//	public Set<String> getIdPeliculas() {
//		Set<String> ids = new HashSet<String>();
//		for (Pelicula peli: informaciones) {
//			ids.add(peli.getTitulo());
//		}
//		return ids;
//	}
	
	public void setPeliculas(String[] peliculas) {
		this.peliculas = peliculas;
	}
	
	public String[] getIdPeliculas() {
		return peliculas;
	}
	
//	private Set<Pelicula> getPeliculas() {
//		return this.informaciones;
//	}
	
}
