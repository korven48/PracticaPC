package server;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import mensajes.Pelicula;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String ip;
	private String[] informaciones; // lista de ids de las peliculas
	// informacion compartida
	
	public Usuario(String id, String ip, String[] informaciones) {
		super();
		this.id = id;
		this.ip = ip;
		this.informaciones = informaciones;
	}

	public String getId() {
		return id;
	}
	
	public String getIp() {
		return ip;
	}
	
	public Set<String> getIdPeliculas() {
		Set<String> ids = new HashSet<String>();
		for (Pelicula peli: informaciones) {
			ids.add(peli.getTitulo());
		}
		return ids;
	}
	
	private Set<Pelicula> getPeliculas() {
		return this.informaciones;
	}
	
}
