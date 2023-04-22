package mensajes;

public class MensajeEmision extends Mensaje {
	private static final long serialVersionUID = 1L;
	private String nombrePelicula;
	private String ip;
	private int puerto;

	public MensajeEmision(M tipo, String origen, String destino, String nombrePelicula, String ip, int puerto) {
		super(tipo, origen, destino);
		this.nombrePelicula = nombrePelicula;
		this.ip = ip;
		this.puerto = puerto;
	}

	public String getNombrePelicula() {
		return nombrePelicula;
	}

	public String getIp() {
		return ip;
	}

	public int getPuerto() {
		return puerto;
	}
}
