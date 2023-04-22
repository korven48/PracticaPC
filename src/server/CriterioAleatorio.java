package server;

import java.util.List;
import java.util.Random;


public class CriterioAleatorio extends Criterio {

	@Override
	public Usuario seleccionaUsuario(List<Usuario> usuarios) {
		Random rand = new Random();
		int index = rand.nextInt(usuarios.size());
		return usuarios.get(index);
	}

}
