package concurrente.locks;

public interface Lock {
	public void takeLock(int id);
	public void releaseLock(int id);
}
