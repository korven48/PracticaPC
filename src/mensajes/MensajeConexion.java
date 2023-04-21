package mensajes;

import server.Usuario;

@SuppressWarnings("serial")
public class MensajeConexion extends Mensaje{
	Usuario usuario;
	
	public MensajeConexion(M tipo, String origen, String destino, Usuario usuario) {
		super(tipo, origen, destino);
		this.usuario = usuario;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
}
