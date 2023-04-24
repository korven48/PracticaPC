package concurrente.locks;

public class LockRompeEmpate implements Lock {
	private volatile int[] in;
	private volatile int[] last;
	private int N;
	
	public LockRompeEmpate(int N) {
		this.in = new int[N];
		this.last = new int[N];
		this.N = N;
	}
	
	@Override
	public void takeLock(int id) {
//		System.out.println("Taking lock " + id);
		for (int j = 0; j < N; j++) {
			in[id] = j;
			in = in;
			last[j] = id;
			last = last;
			for (int k = 0; k < N; k++) {
				if (k != id) {
					while (in[k] >= in[id] && last[j] == id) {} // <
				}
			}
		}
	}

	@Override
	public void releaseLock(int id) {
		in[id] = -1;
//		in = in;
	}

}
