package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import concurrente.locks.Lock;
import mensajes.*;
import server.Usuario;

public class OyenteServidor extends Thread { // En realidad ServerManager
	private Socket socket;
	private ObjectOutputStream fout;
	private ObjectInputStream fin;
	private Usuario usuario;
	private static int puertoEmision;
	
	
	private Lock lockConsola;

	public OyenteServidor(Socket socket, Usuario usuario, Lock lockConsola) {
		this.socket = socket;
		this.usuario = usuario;
		this.lockConsola = lockConsola;
		this.puertoEmision = 1025;
	}

	public ObjectOutputStream getFout() {
		return fout;
	}

	public ObjectInputStream getFin() {
		return fin;
	}
	
	private synchronized int getPuertoDisponible(){
		puertoEmision++;
		return puertoEmision;
	}

	@Override
	public void run() {
		// Al descargar algo hay que actualizar la tabla del server
		try {
			fin = new ObjectInputStream(socket.getInputStream());
			fout = new ObjectOutputStream(socket.getOutputStream());

			while (true) {
				Mensaje m = (Mensaje) fin.readObject();
				switch (m.getTipo()) {
				case CONFIRMAR_CONEX:
					usuario.setIp(m.getDestino());
					lockConsola.takeLock(1);
					System.out.println("Conectado correctamente al socket");
					lockConsola.releaseLock(1);
					break;
				case CONFIRMACION_CONSULTAR_INFO:
					MensajeInfo mInfo = (MensajeInfo) m;
					lockConsola.takeLock(1);
					System.out.println("Las peliculas disponibles son: ");
					for (String peliId : mInfo.getPeliculasDisponibles()) {
						System.out.println(peliId);
					}
					lockConsola.releaseLock(1);
					break;

				case EMITIR_FICHERO:
					MensajeTexto mDatosPelicula = (MensajeTexto) m;
					String nombrePelicula = mDatosPelicula.getContenido();
					int puerto = getPuertoDisponible();
					EmisorInfo emisor = new EmisorInfo(puerto, nombrePelicula);
					String ip = usuario.getIp();
					emisor.start();
					
					lockConsola.takeLock(1);
					System.out.println("Ip emisor: " + ip);
					lockConsola.releaseLock(1);
					
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
					socket.close();
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace(); // Debería haber lock para el error?
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
