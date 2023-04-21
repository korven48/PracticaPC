package mensajes;

import java.util.List;
import java.util.Set;

public class MensajeInfo extends Mensaje {
	private static final long serialVersionUID = 1L;
	private List<String> peliculasDisponibles;
	
	public MensajeInfo(M tipo, String origen, String destino, List<String> peliculasDisponibles) {
		super(tipo, origen, destino);
		this.peliculasDisponibles = peliculasDisponibles;
	}
	
	public List<String> getPeliculasDisponibles() {
		return peliculasDisponibles;
	}
}
