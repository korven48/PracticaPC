package mensajes;


import java.io.Serializable;

public abstract class Mensaje implements Serializable {
	M tipo;
	String origen;
	String destino;
	public Mensaje(M tipo, String origen, String destino) {
		super();
		this.tipo = tipo;
		this.origen = origen;
		this.destino = destino;
	}
	
	public M getTipo() {
		return tipo;
	}
	
	public String getOrigen() {
		return origen;
	}
	
	public String getDestino() {
		return destino;
	}
}
