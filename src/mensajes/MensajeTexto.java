package mensajes;


@SuppressWarnings("serial")
public class MensajeTexto extends Mensaje {
    private String contenido;
    
    public MensajeTexto(M tipo, String origen, String destino, String contenido) {
		super(tipo, origen, destino);
		this.contenido = contenido;
	}

	public String getContenido() {
        return contenido;
    }
}
