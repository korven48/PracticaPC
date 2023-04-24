package mensajes;

public class MensajeBool extends Mensaje {
	private static final long serialVersionUID = 1L;
	private boolean bool;
	
	public MensajeBool(M tipo, String origen, String destino, boolean bool) {
		super(tipo, origen, destino);
		this.bool = bool;
	}
	
	public boolean getBool() {
		return bool;
	}
	
}
