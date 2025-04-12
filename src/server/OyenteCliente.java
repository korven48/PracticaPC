package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import auxiliar.Pair;
import concurrente.ReadWriteLock;
import concurrente.ReadWriteSynchronizer;
import mensajes.*;

public class OyenteCliente extends Thread { // ClientManager
	ObjectInputStream fin;
	ObjectOutputStream fout;
	Socket cliente;
	Usuario usuarioEscuchado;

	boolean terminated = false;

	// Info del server
	Map<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels;
	Map<String, List<Usuario>> peliculas;

	private final ReadWriteSynchronizer synchronizer = new ReadWriteLock();
	
	public OyenteCliente(Socket cliente, Map<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels,
			Map<String, List<Usuario>> peliculas) {
		this.cliente = cliente;
		this.clientChannels = clientChannels;
		this.peliculas = peliculas;
	}

	private void eliminarPeliculasUsuario(Usuario usuario) throws InterruptedException {
		synchronizer.requestWrite();
		Iterator<String> peliculaIterator = peliculas.keySet().iterator();
		while (peliculaIterator.hasNext()) {
		    String peliculaId = peliculaIterator.next();
		    List<Usuario> usuarios = peliculas.get(peliculaId);
		    usuarios.remove(usuario);
		    if (usuarios.isEmpty()) {
		        peliculaIterator.remove();
		    }
		}
		synchronizer.releaseWrite();
	}
	
	private void addPeliculasUsuario(Usuario usuario) throws InterruptedException {
		synchronizer.requestWrite();
		for (String peliculaId : usuario.getIdPeliculas()) { // escribir
			if (peliculas.containsKey(peliculaId)) {
				peliculas.get(peliculaId).add(usuario);
			} else {
				ArrayList<Usuario> lstUs = new ArrayList<Usuario>();
				lstUs.add(usuario);
				peliculas.put(peliculaId, lstUs);
			}
		}	
		synchronizer.releaseWrite();
	}
	
	private List<Usuario> getUsuarios(String nombrePeli) throws InterruptedException {
		synchronizer.releaseRead();
		List<Usuario> usuarios = null;
		if (peliculas.containsKey(nombrePeli)){
			usuarios = peliculas.get(nombrePeli); // leer			
		}
		synchronizer.releaseRead();
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
					
					clientChannels.put(usuario.getId(), new Pair<ObjectOutputStream, ObjectInputStream>(fout, fin));
					
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
					ObjectOutputStream emisourOut = clientChannels.get(usuarioACompartir.getId()).getFirst();
					
					emisourOut.writeObject(m);
					break;
				case PREPARADO_CS:
					MensajeEmision mensajeEmision = (MensajeEmision) m;
					// Este mensaje llega del emisor y va al receptor
					m = new MensajeEmision(M.PREPARADO_SC, m.getOrigen(), m.getDestino(),
							mensajeEmision.getNombrePelicula(), mensajeEmision.getIp(), mensajeEmision.getPuerto());
					
					// Obtenemos el output stream de el cliente receptor
					emisourOut = clientChannels.get(m.getDestino()).getFirst();					
					emisourOut.writeObject(m);
					break;
				case CERRAR_CONEXION:
					conex = (MensajeConexion) m;
					usuario = conex.getUsuario();
					
					
					clientChannels.remove(usuario.getId());
					
					
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
