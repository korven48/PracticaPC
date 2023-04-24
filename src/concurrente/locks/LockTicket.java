package concurrente.locks;

import java.util.concurrent.atomic.AtomicInteger;

public class LockTicket implements Lock {
	private AtomicInteger number;
	private int next;
	private int[] turn;
	
	public LockTicket(int N) {
		this.number = new AtomicInteger(1);
		this.next = 1;
		turn = new int[N];
	}
	
	@Override
	public void takeLock(int i) {
		turn[i] = number.getAndIncrement();
		while (turn[i] != next) {}
	}

	@Override
	public void releaseLock(int id) {
		next++;
	}
}
