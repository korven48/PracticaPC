package cliente;

import java.net.*;
import java.util.Scanner;

import concurrente.locks.Lock;
import concurrente.locks.LockBackery;

import java.io.*;

import mensajes.*;
import server.Usuario;


public class Cliente {
	private static Scanner sc;
	private static OyenteServidor oyente;
	private static ObjectOutputStream fout;
	private static ObjectInputStream fin;

	private static Usuario usuario;
	
	private static Lock lockConsola;
	
	private static String[] getPeliculasDisponibles() {
		File carpeta = new File("pelis");
		String[] peliculas = new String[0];
        if (carpeta.exists() && carpeta.isDirectory()) {
            peliculas = carpeta.list();
        } else {
        	carpeta.mkdir();
        }
        return peliculas;
	}
	
	public static synchronized void actualizarPelisUsuario() {
		String[] peliculas = getPeliculasDisponibles();
        usuario.setPeliculas(peliculas);
	}
	
	public static void actualizarPelisEnServidor() throws IOException {
        Mensaje m = new MensajeConexion(M.ACTUALIZAR_INFO, null, null, usuario);
        fout.writeObject(m);
	}
	
	private static int interfaz() {
		lockConsola.takeLock(0);
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
		lockConsola.releaseLock(0);

		return opcion;
	}

	public static void main(String[] args) throws IOException {
		// Crear un socket de conexi�n al servidor en el puerto 8888
		String ip = "192.168.1.128"; // "localhost" "192.168.1.128" comprobar en ipconfig (adaptador lan inalambrica)
		Socket servidor = new Socket(ip, 1024);
		System.out.println("Conexion establecida con " + servidor.getInetAddress());
		
		// Creamos los Locks
		lockConsola = new LockBackery(2); // solo cliente y oyenteServidor acceden a la consola
		
		// Al iniciar la aplicacion se pregunta al usuario por su nombre de usuario.
		sc = new Scanner(System.in);
		lockConsola.takeLock(0);
		System.out.print("Introduzca su nombre: ");
		String nombre_usuario = sc.nextLine();
		lockConsola.releaseLock(0);
		
		// Crea lista de peliculas del usuario
		File carpeta = new File("pelis");
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            carpeta.mkdir();
        }
        String[] peliculas = getPeliculasDisponibles();
		usuario = new Usuario(nombre_usuario, "", peliculas);

		oyente = new OyenteServidor(servidor, usuario, lockConsola);
		oyente.start();
		try {
			Thread.sleep(200); // no deberia ser asi
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		fout = oyente.getFout();
		fin = oyente.getFin();
		
		lockConsola.takeLock(1);
		Mensaje m = new MensajeConexion(M.CONEXION, "cliente", "servidor", usuario);
		fout.writeObject(m);

		try {
			while (true) {
				int opcion = interfaz();
				actualizarPelisUsuario();
				Mensaje mensaje;
				switch (opcion) {
				case 1:
					mensaje = new MensajeBasico(M.CONSULTAR_INFO, nombre_usuario, nombre_usuario);
					fout.writeObject(mensaje);
					lockConsola.takeLock(1);
					break;
				case 2:
					lockConsola.takeLock(0);
					System.out.print("Escribe el nombre de la pelicula que quieres descargar: ");
					String peli;
					peli = sc.nextLine();

					boolean encontrado = false;
			        peliculas = usuario.getIdPeliculas();
			        for (int i = 0; i < peliculas.length; i++) {
			            if (peliculas[i].equals(peli)) {
			                encontrado = true;
			                break;
			            }
			        }
			        
			        if (encontrado) {
			        	System.out.println("Ya tienes ese archivo");
			        } else {
			            m = new MensajeTexto(M.PEDIR_FICHERO, "cliente", "servidor", peli);
			            fout.writeObject(m);
			        }
			        lockConsola.releaseLock(0);
			        lockConsola.takeLock(1);
					break;
				case 3:
					lockConsola.takeLock(0);
					System.out.println("Tus peliculas son: ");
					for (String peliId : usuario.getIdPeliculas()) {
						System.out.println(peliId);
					}
					lockConsola.releaseLock(0);
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