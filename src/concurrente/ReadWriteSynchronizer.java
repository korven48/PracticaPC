package concurrente;

public interface ReadWriteSynchronizer {
	public void requestRead() throws InterruptedException;
	public void releaseRead() throws InterruptedException;
	public void requestWrite() throws InterruptedException;
	public void releaseWrite() throws InterruptedException;
}
