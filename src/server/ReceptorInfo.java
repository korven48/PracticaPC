package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceptorInfo extends Thread {
	
	@Override
	public void run() {
		String ipEmisor = "localhost";
		int puertoEmisor = 1234;
		Socket socketEmisor;
		try {
			socketEmisor = new Socket(ipEmisor, puertoEmisor);

		InputStream inputStream = socketEmisor.getInputStream();
		
		String nombrePelicula = "a";
		String rutaDelArchivo = "peliculas/" + nombrePelicula;
		FileOutputStream fileOutputStream = new FileOutputStream(rutaDelArchivo);
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
		    fileOutputStream.write(buffer, 0, bytesRead);
		}

		inputStream.close();
		fileOutputStream.close();
		socketEmisor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
