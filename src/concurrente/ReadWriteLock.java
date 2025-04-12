package concurrente;

import java.util.concurrent.Semaphore;

public class ReadWriteLock implements ReadWriteSynchronizer {
	private static int nr = 0, nw = 0, dr = 0, dw = 0;
	private static final Semaphore entry = new Semaphore(1), readers = new Semaphore(1), writers = new Semaphore(1);
	@Override
	public void requestRead() throws InterruptedException {
		entry.acquire();
		if (nw > 0) {
			dr++;
			entry.release();
			readers.acquire();
		}
		nr++;
		if (dr > 0) { // Los lectores pueden leer a la vez
			dr--;
			readers.release();
		} else {
			entry.release();
		}
	}

	@Override
	public void releaseRead() throws InterruptedException {
		entry.acquire();
		nr--;
		if (nr == 0 && dw > 0) { // Si queda algun escritor esperando
			dw--;
			writers.release(); // despierto a uno
		} else { 	
			entry.release();
		}
	}

	@Override
	public void requestWrite() throws InterruptedException {
		entry.acquire();
		if (nr > 0 || nw > 0) {
			dw++;
			entry.release();
			writers.acquire();
		}
		nw++;
		entry.release();
	}

	@Override
	public void releaseWrite() throws InterruptedException {
		entry.acquire();
		nw--;
		if (dw > 0) { // Si queda algun escritor esperando
			dw--;
			writers.release(); // despierto a uno
		} else if (dr > 0) { // si no, a algun reader
			dr--;
			readers.release();
		} else { // si no pues suelto el testigo
			entry.release();
		}
	}

}
