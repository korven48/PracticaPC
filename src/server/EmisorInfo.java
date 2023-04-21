package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EmisorInfo extends Thread {
	ServerSocket socketEmisor;
	public EmisorInfo(int puerto) throws IOException {
		socketEmisor = new ServerSocket(puerto); // no se que puerto poner
	}
	
	@Override
	public void run() {
		try {
			Socket cliente = socketEmisor.accept();
			OutputStream fout = cliente.getOutputStream();
			
			// Emitir fichero
			String rutaDelArchivo = "peliculas/";
			FileInputStream fileInputStream = new FileInputStream(rutaDelArchivo);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				fout.write(buffer, 0, bytesRead);
			}
			
			// Cerramos las conexiones
			fileInputStream.close();
			fout.close();
			cliente.close();
			socketEmisor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
