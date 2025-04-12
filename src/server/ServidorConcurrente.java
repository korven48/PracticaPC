package server;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import auxiliar.Pair;
import concurrente.locks.Lock;
import concurrente.locks.LockBackery;
import concurrente.monitores.MonitorNormal;

public class ServidorConcurrente {
	private static Lock lockConsola;

	public static void main(String[] args) throws IOException {

		// Info del server
		Map<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels = new ConcurrentHashMap<String, Pair<ObjectOutputStream, ObjectInputStream>>();
		Map<String, List<Usuario>> peliculas = new HashMap<String, List<Usuario>>(); // idPelicula -> lista de
																							// usuarios que la tienen

		// Semaforos y monitores etc
		Lock lockConsola = new LockBackery(2); // no hace falta de momento porque el servidor no printea nada depues de
												// crear oyenteCliente
		// Crear un socket de escucha en el puerto 8888
		@SuppressWarnings("resource")
		ServerSocket servidor = new ServerSocket(1024);
		System.out.println("Servidor esperando conexiones en " + servidor.getLocalSocketAddress());

		// Esperar a que lleguen conexiones de clientes
		while (true) {
			Socket cliente = servidor.accept();

//			System.out.println("Se ha conectado alguien desde " + cliente.getInetAddress().getHostAddress());

			new OyenteCliente(cliente, clientChannels, peliculas).start();
		}
	}
}