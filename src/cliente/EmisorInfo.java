package cliente;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EmisorInfo extends Thread {
	ServerSocket socketEmisor;
	String nombrePelicula;
	
	public EmisorInfo(int puerto, String nombrePelicula) throws IOException {
		this.nombrePelicula = nombrePelicula;
		socketEmisor = new ServerSocket(puerto); // no se que puerto poner
	}
	
	public String getIp() {
		InetSocketAddress address = (InetSocketAddress) socketEmisor.getLocalSocketAddress();
		return address.getAddress().getHostAddress();
	}
	
	@Override
	public void run() {
		try {
			Socket cliente = socketEmisor.accept();
			OutputStream fout = cliente.getOutputStream();
			
			// Emitir fichero
			String rutaDelArchivo = "pelis/" + nombrePelicula;
			FileInputStream fileInputStream = new FileInputStream(rutaDelArchivo);
			byte[] buffer = new byte[1024];
			int bytesRead;
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
