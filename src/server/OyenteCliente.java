package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import auxiliar.Pair;
import concurrente.monitores.MonitorNormal;
import mensajes.*;

public class OyenteCliente extends Thread { // ClientManager
	ObjectInputStream fin;
	ObjectOutputStream fout;
	Socket cliente;
	Usuario usuarioEscuchado;

	boolean terminated = false;
	
	private static MonitorNormal monitorClientChannels = new MonitorNormal();
	private static int nr = 0, nw = 0, dr = 0, dw = 0;
	private static Semaphore entry = new Semaphore(1), readers = new Semaphore(1), writers = new Semaphore(1);

	// Info del server
	HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels;
	HashMap<String, List<Usuario>> peliculas;

	public OyenteCliente(Socket cliente, HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels,
			HashMap<String, List<Usuario>> peliculas) {
		this.cliente = cliente;
		this.peliculas = peliculas;
	}

	private void eliminarPeliculasUsuario(Usuario usuario) throws InterruptedException {
		entry.acquire();
		if (nr > 0 || nw > 0) {
			dw++;
			entry.release();
			writers.acquire();
		}
		nw++;
		entry.release();
		Iterator<String> peliculaIterator = peliculas.keySet().iterator();
		while (peliculaIterator.hasNext()) {
		    String peliculaId = peliculaIterator.next();
		    List<Usuario> usuarios = peliculas.get(peliculaId);
		    usuarios.remove(usuario);
		    if (usuarios.isEmpty()) {
		        peliculaIterator.remove();
		    }
		}
		entry.acquire();
		nw--;
		if (dw > 0) { // Si queda algun escritor esperando
			dw--;
			writers.release(); // despierto a uno
		} else if (dr > 0) { // si no, a algun reader
			dr--;
			readers.release();
		} else { // si no pues suelto el testigo
			entry.release();
		}
		
	}
	
	private void addPeliculasUsuario(Usuario usuario) throws InterruptedException {
		entry.acquire();
		if (nr > 0 || nw > 0) {
			dw++;
			entry.release();
			writers.acquire();
		}
		nw++;
		entry.release();
		for (String peliculaId : usuario.getIdPeliculas()) { // escribir
			if (peliculas.containsKey(peliculaId)) {
				peliculas.get(peliculaId).add(usuario);
			} else {
				ArrayList<Usuario> lstUs = new ArrayList<Usuario>();
				lstUs.add(usuario);
				peliculas.put(peliculaId, lstUs);
			}
		}
		entry.acquire();
		nw--;
		if (dw > 0) { // Si queda algun escritor esperando
			dw--;
			writers.release(); // despierto a uno
		} else if (dr > 0) { // si no, a algun reader
			dr--;
			readers.release();
		} else { // si no pues suelto el testigo
			entry.release();
		}
		
	}
	
	private List<Usuario> getUsuarios(String nombrePeli) throws InterruptedException {
		entry.acquire();
		if (nw > 0) {
			dr++;
			entry.release();
			readers.acquire();
		}
		nr++;
		if (dr > 0) { // Los lectores pueden leer a la vez
			dr--;
			readers.release();
		} else {
			entry.release();
		}
		List<Usuario> usuarios = null;
		if (peliculas.containsKey(nombrePeli)){
			usuarios = peliculas.get(nombrePeli); // leer			
		}
		entry.acquire();
		nr--;
		if (nr == 0 && dw > 0) { // Si queda algun escritor esperando
			dw--;
			writers.release(); // despierto a uno
		} else { 	
			entry.release();
		}
		return usuarios;
	}
	
	@Override
	public void run() {
		try (
				// Crear los flujos de entrada y salida de objetos
				ObjectOutputStream fout = new ObjectOutputStream(cliente.getOutputStream());
				ObjectInputStream fin = new ObjectInputStream(cliente.getInputStream());) {
			while (!terminated) {
				Mensaje m = (Mensaje) fin.readObject();
				switch (m.getTipo()) {
				case CONEXION:
					MensajeConexion conex = (MensajeConexion) m;
					Usuario usuario = conex.getUsuario();
					this.usuarioEscuchado = usuario;
					
					monitorClientChannels.requestWrite();
					clientChannels.put(usuario.getId(), new Pair<ObjectOutputStream, ObjectInputStream>(fout, fin));
					monitorClientChannels.releaseWrite();

					// Añade el usuario a la lista de usuarios que tienen la pelicula
					addPeliculasUsuario(usuario);

					System.out.println(usuario.getId() + " se acaba de conectar al servidor");

					m = new MensajeBasico(M.CONFIRMAR_CONEX, "servidor", cliente.getInetAddress().getHostAddress());
					fout.writeObject(m);
					break;
				case ACTUALIZAR_INFO:
					conex = (MensajeConexion) m;
					usuario = conex.getUsuario();
					// Esto es terriblemente ineficiente pero solo ocurre al descagar una peli, asi que asi queda
					eliminarPeliculasUsuario(usuario);
					addPeliculasUsuario(usuario);
					break;
				case CONSULTAR_INFO:
					m = new MensajeInfo(M.CONFIRMACION_CONSULTAR_INFO, "servidor", "cliente",
							new ArrayList<>(peliculas.keySet()));
					fout.writeObject(m);
					break;
				case PEDIR_FICHERO:
					MensajeTexto mensajeFichero = (MensajeTexto) m;
					String nombrePeli = mensajeFichero.getContenido();

					// Lista de usuarios que tienen la pelicula pedida
					List<Usuario> usuariosConPeli = getUsuarios(nombrePeli);

					if (usuariosConPeli == null) {
						m = new MensajeBool(M.CONFIRMACION_PEDIR_FICHERO, null, null, false);
						fout.writeObject(m);
						continue;
					}
					m = new MensajeBool(M.CONFIRMACION_PEDIR_FICHERO, null, null, true);
					fout.writeObject(m);
					// Elegimos al usuario segun un criterio
					Criterio criterio = new CriterioAleatorio();
					Usuario usuarioACompartir = criterio.seleccionaUsuario(usuariosConPeli);
					System.out.println(usuarioACompartir.getId() + " le manda " + nombrePeli + " a " + usuarioEscuchado.getId());
					
					// El que pide el fichero es el destino
					m = new MensajeTexto(M.EMITIR_FICHERO, usuarioACompartir.getId(), usuarioEscuchado.getId(),
							nombrePeli);

					// Obtenemos el output stream de el cliente emisor
					monitorClientChannels.requestRead();
					ObjectOutputStream emisourOut = clientChannels.get(usuarioACompartir.getId()).getFirst();
					monitorClientChannels.releaseRead();
					
					emisourOut.writeObject(m);
					break;
				case PREPARADO_CS:
					MensajeEmision mensajeEmision = (MensajeEmision) m;
					// Este mensaje llega del emisor y va al receptor
					m = new MensajeEmision(M.PREPARADO_SC, m.getOrigen(), m.getDestino(),
							mensajeEmision.getNombrePelicula(), mensajeEmision.getIp(), mensajeEmision.getPuerto());
					
					// Obtenemos el output stream de el cliente receptor
					monitorClientChannels.requestRead();
					emisourOut = clientChannels.get(m.getDestino()).getFirst();
					monitorClientChannels.requestRead();
					
					emisourOut.writeObject(m);
					break;
				case CERRAR_CONEXION:
					conex = (MensajeConexion) m;
					usuario = conex.getUsuario();
					
					monitorClientChannels.releaseWrite();
					clientChannels.remove(usuario.getId());
					monitorClientChannels.releaseWrite();
					
					eliminarPeliculasUsuario(usuario);
					m = new MensajeBasico(M.CONFIRMAR_CIERRE, "servidor", "cliente");
					fout.writeObject(m);
					System.out.println(usuario.getId() + " se ha desconectado");
					terminated = true;
					break;
				default:
					System.out.println("Mensaje no reconocido");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
