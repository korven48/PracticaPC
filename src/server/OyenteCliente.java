package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import auxiliar.Pair;
import mensajes.*;

public class OyenteCliente extends Thread { // ClientManager
	ObjectInputStream fin;
	ObjectOutputStream fout;
	Socket cliente;
	Usuario usuarioEscuchado;

	boolean terminated = false;

	// Info del server
	HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels;
	HashMap<String, List<Usuario>> peliculas;

	public OyenteCliente(Socket cliente, HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels,
			HashMap<String, List<Usuario>> peliculas) {
		this.cliente = cliente;
		this.peliculas = peliculas;
		this.clientChannels = clientChannels;
	}

	@Override
	public void run() {
		/*
		 * hacer lecturas del flujo de entrada correspondiente, realizar las acciones
		 * oportunas, y devolver los resultados en forma de mensajes que seran enviados
		 * al usuario o usuarios involucrados
		 */
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
					for (String peliculaId : usuario.getIdPeliculas()) {
						if (peliculas.containsKey(peliculaId)) {
							peliculas.get(peliculaId).add(usuario);
						} else {
							ArrayList<Usuario> lstUs = new ArrayList<Usuario>();
							lstUs.add(usuario);
							peliculas.put(peliculaId, lstUs);
						}
					}

					System.out.println(usuario.getId() + " se acaba de conectar al servidor");

					m = new MensajeBasico(M.CONFIRMAR_CONEX, "servidor", cliente.getInetAddress().getHostAddress());
					fout.writeObject(m);
					break;
				case CONSULTAR_INFO:
					m = new MensajeInfo(M.CONFIRMACION_CONSULTAR_INFO, "servidor", "cliente",
							new ArrayList<>(peliculas.keySet()));
					fout.writeObject(m);
					break;
				case PEDIR_FICHERO:
					System.out.println("Fichero pedido");
					MensajeTexto mensajeFichero = (MensajeTexto) m;
					String nombrePeli = mensajeFichero.getContenido();
					List<Usuario> usuariosConPeli;
					if (peliculas.containsKey(nombrePeli)) {
						// Lista de usuarios que tienen la pelicula pedida
						usuariosConPeli = peliculas.get(nombrePeli);

						// Elegimos al usuario segun un criterio
						Criterio criterio = new CriterioAleatorio();
						Usuario usuarioACompartir = criterio.seleccionaUsuario(usuariosConPeli);
						System.out.println("Usuario a compartir: " + usuarioACompartir.getId());

						// El que pide el fichero es el destino
						m = new MensajeTexto(M.EMITIR_FICHERO, usuarioACompartir.getId(), usuarioEscuchado.getId(),
								nombrePeli);

						// Obtenemos el output stream de el cliente emisor
						ObjectOutputStream emisourOut = clientChannels.get(usuarioACompartir.getId()).getFirst();
						emisourOut.writeObject(m);
					} else {
						System.out.println("Pelicula no disponible");
					}
					break;
				case PREPARADO_CS:
					MensajeEmision mensajeEmision = (MensajeEmision) m;
					// Este mensaje llega del emisor y va al receptor
					m = new MensajeEmision(M.PREPARADO_SC, m.getOrigen(), m.getDestino(),
							mensajeEmision.getNombrePelicula(), mensajeEmision.getIp(), mensajeEmision.getPuerto());
					
					// Obtenemos el output stream de el cliente receptor
					ObjectOutputStream emisourOut = clientChannels.get(m.getDestino()).getFirst();
					emisourOut.writeObject(m);
					break;
				case CERRAR_CONEXION:
					conex = (MensajeConexion) m;
					usuario = conex.getUsuario();
					clientChannels.remove(usuario.getId());
					for (String peliculaId : usuario.getIdPeliculas()) {
						if (peliculas.containsKey(peliculaId)) {
							List<Usuario> usuarios = peliculas.get(peliculaId);
							usuarios.remove(usuario);
							if (usuarios.isEmpty()) {
								peliculas.remove(peliculaId);
							}
						}
					}
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
		}
	}
}
