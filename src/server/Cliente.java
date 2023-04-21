package server;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import mensajes.*;

import org.json.*;

public class Cliente {
	private static Scanner sc;
	private static OyenteServidor oyente;
	private static ObjectOutputStream fout;
	private static ObjectInputStream fin;

	private static String nombre_usuario;

	private void descargar() {

	}

	private void compartir() {
		/*
		 * Al margen de la voluntad del usuario, el programa cliente puede actuar como
		 * emisor de cualquier informacion de la que dispone compartida, como
		 * propietario de una informacion que otro cliente solicite. Esta accion sera
		 * llevada a cabo en un segundo plano permitiendo al usuario continuar con el
		 * uso normal de la aplicacion.
		 */

	}

	private static Set<Pelicula> seleccionaPeliculas() throws IOException {
		// Escoje 3 peliculas aleatorias de peliculas.json
		String jsonString = new String(Files.readAllBytes(Paths.get("info/peliculas.json")));
		JSONArray jsonArray = new JSONArray(jsonString);
		Random rand = new Random();
		List<Integer> indices = new ArrayList<>();
		while (indices.size() < 3) {
			int index = rand.nextInt(jsonArray.length());
			if (!indices.contains(index)) {
				indices.add(index);
			}
		}

		Set<Pelicula> peliculas = new LinkedHashSet<Pelicula>();
		for (int index : indices) {
			JSONObject objeto = jsonArray.getJSONObject(index);
			String titulo = objeto.getString("titulo");
			String descripcion = objeto.getString("descripcion");
			peliculas.add(new Pelicula(titulo, descripcion));
		}

		return peliculas;
	}

	private static int interfaz() {
		int opcion = 0;
		System.out.println("  ------------ Menu ------------");
		System.out.println("1 - Consultar peliculas disponibles");
		System.out.println("2 - Descargar pelicula");
		System.out.println("3 - Mis peliculas");
		System.out.println("4 - Salir");
		System.out.print("Elige una de las opciones: ");

		opcion = sc.nextInt();
		if (sc.hasNextLine()) {
			sc.nextLine(); // limpiar el búfer
		}

		return opcion;
	}

	public static void main(String[] args) throws IOException {
		// Crear un socket de conexi�n al servidor en el puerto 8888
		Socket servidor = new Socket("localhost", 8888); // cambiar "localhost" por la ip del server para multiples
															// ordenadores
		System.out.println("Conexion establecida con " + servidor.getInetAddress());

		// Al iniciar la aplicacion se pregunta al usuario por su nombre de usuario.
		sc = new Scanner(System.in);
		System.out.print("Introduzca su nombre: ");
		nombre_usuario = sc.nextLine();

		// Crea lista de peliculas del usuario
//		Set<Pelicula> peliculasCliente = seleccionaPeliculas();
		File carpeta = new File("pelis");
		String[] peliculas;
        if (carpeta.isDirectory()) {
            peliculas = carpeta.list();
            if (peliculas != null) {
                for (String file : peliculas) {
                    System.out.println(file);
                }
            }
        }

		Usuario usuario = new Usuario(nombre_usuario, servidor.getInetAddress().toString(), peliculas);

		oyente = new OyenteServidor(servidor, usuario);
		oyente.start();
		try {
			Thread.sleep(200); // no deberia ser asi
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		fout = oyente.getFout();
		fin = oyente.getFin();

		Mensaje m = new MensajeConexion(M.CONEXION, "cliente", "servidor", usuario);
		fout.writeObject(m);
		try {
			Thread.sleep(200); // no deberia ser asi
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			while (true) {
				int opcion = interfaz();
				Mensaje mensaje;
				switch (opcion) {
				case 1:
					mensaje = new MensajeBasico(M.CONSULTAR_INFO, nombre_usuario, nombre_usuario);
					fout.writeObject(mensaje);
					try {
						Thread.sleep(200); // no deberia ser asi
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					break;
				case 2:
					System.out.print("Escribe el nombre de la pelicula que quieres descargar: ");
					String peli;
					peli = sc.nextLine();

					m = new MensajeTexto(M.PEDIR_FICHERO, "cliente", "servidor", peli);
					fout.writeObject(peli);
					break;
				case 3:
					System.out.println("Tus peliculas son: ");
					for (String peliId : usuario.getIdPeliculas()) {
						System.out.println(peliId);
					}
					break;
				case 4:
					mensaje = new MensajeConexion(M.CERRAR_CONEXION, nombre_usuario, nombre_usuario, usuario);
					fout.writeObject(mensaje);
					System.exit(0);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}