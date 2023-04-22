package server;

import java.io.FileInputStream;
import java.io.IOException;

public class Pruebas {
	public static void main(String[] args) throws IOException {
		String nombrePelicula = "peli2.txt";
		String rutaDelArchivo = "pelis/" + nombrePelicula;
		FileInputStream fileInputStream = new FileInputStream(rutaDelArchivo);
		byte[] buffer = new byte[1024];
		int bytesRead;		
		while ((bytesRead = fileInputStream.read(buffer)) != -1) {
			System.out.println("mandando bytes");
//			fout.write(buffer, 0, bytesRead);
		}
		fileInputStream.close();
	}
}
