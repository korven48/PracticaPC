package cliente;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceptorInfo extends Thread {
	private String nombrePelicula;
	private String ip;
	private int puerto;
	
	public ReceptorInfo(String nombrePelicula, String ip, int puerto) {
		this.nombrePelicula = nombrePelicula;
		this.ip = ip;
		this.puerto = puerto;
	}

	@Override
	public void run() {
		Socket socketEmisor;
		try {
		socketEmisor = new Socket(ip, puerto);

		InputStream inputStream = socketEmisor.getInputStream();
		
		String rutaDelArchivo = "pelis/" + nombrePelicula;
		FileOutputStream fileOutputStream = new FileOutputStream(rutaDelArchivo);
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
//		System.out.println("Ha comenzado la descarga de " + nombrePelicula);
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			fileOutputStream.write(buffer, 0, bytesRead);
		}
		
		Cliente.actualizarPelisUsuario();
		Cliente.actualizarPelisEnServidor();
		inputStream.close();
		fileOutputStream.close();
		socketEmisor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
