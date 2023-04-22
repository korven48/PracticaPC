package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import mensajes.*;
import server.Usuario;

public class OyenteServidor extends Thread { // En realidad ServerManajer
	private Socket servidor;
	private ObjectOutputStream fout;
	private ObjectInputStream fin;
	private Usuario usuario;

	public OyenteServidor(Socket servidor, Usuario usuario) {
		this.servidor = servidor;
		this.usuario = usuario;
	}

	public ObjectOutputStream getFout() {
		return fout;
	}

	public ObjectInputStream getFin() {
		return fin;
	}

	@Override
	public void run() {
		// Al descargar algo hay que actualizar la tabla del server

		try {
			fin = new ObjectInputStream(servidor.getInputStream());
			fout = new ObjectOutputStream(servidor.getOutputStream());

			while (true) {
				Mensaje m = (Mensaje) fin.readObject();
				switch (m.getTipo()) {
				case CONFIRMAR_CONEX:
					System.out.println("Conectado correctamente al servidor");

					break;
				case CONFIRMACION_CONSULTAR_INFO:
					MensajeInfo mInfo = (MensajeInfo) m;
					System.out.println("Las peliculas disponibles son: ");
					for (String peliId : mInfo.getPeliculasDisponibles()) {
						System.out.println(peliId);
					}
					break;

				case EMITIR_FICHERO:
					MensajeTexto mDatosPelicula = (MensajeTexto) m;
					int puerto = 1234;
					String nombrePelicula = mDatosPelicula.getContenido();
					EmisorInfo emisor = new EmisorInfo(puerto, nombrePelicula);
					String ip = emisor.getIp();
					emisor.start();
					
					// el fichero lo manda este cliente a m.getDestino(). 
					m = new MensajeEmision(M.PREPARADO_CS, usuario.getId(), m.getDestino(), nombrePelicula, ip, puerto); // puerto
					fout.writeObject(m);
					break;
				case PREPARADO_SC:
					MensajeEmision mDatosEmision = (MensajeEmision) m;
					nombrePelicula = mDatosEmision.getNombrePelicula();
					ip = mDatosEmision.getIp();
					puerto = mDatosEmision.getPuerto();
					new ReceptorInfo(nombrePelicula, ip, puerto).start();
					
					break;
				case CERRAR_CONEXION:
					servidor.close();
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
