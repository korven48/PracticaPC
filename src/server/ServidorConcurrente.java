package server;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import auxiliar.Pair;

public class ServidorConcurrente {

	public static void main(String[] args) throws IOException {

		// Info del server
		HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>> clientChannels = new HashMap<String, Pair<ObjectOutputStream, ObjectInputStream>>();
//    	HashMap<Usuario, IP>
		HashMap<String, List<Usuario>> peliculas = new HashMap<String, List<Usuario>>(); // idPelicula -> lista de
																							// usuarios que la tienen

		// Semaforos y monitores etc

		// Crear un socket de escucha en el puerto 8888
		@SuppressWarnings("resource")
		ServerSocket servidor = new ServerSocket(8888);
		System.out.println("Servidor esperando conexiones en " + servidor.getLocalSocketAddress());

		// Esperar a que lleguen conexiones de clientes
		while (true) {
			Socket cliente = servidor.accept();
			
//			System.out.println("Se ha conectado alguien desde " + cliente.getInetAddress().getHostAddress());
			
			new OyenteCliente(cliente, clientChannels, peliculas).start();
		}
	}
}