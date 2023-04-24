package concurrente.locks;

import java.util.Arrays;

public class LockBackery implements Lock {
    volatile int[] turns;

    public LockBackery(int numThreads) {
        turns = new int[numThreads];
        Arrays.fill(turns, 0);
    }
    
    private int max(int[] arr) {
    	int max = 0;
    	for (int i = 0; i < arr.length; i++) {
    		if (arr[i] > max) {
    			max = arr[i];
    		}
    	}
    	return max;
	}

    public void takeLock(int id) {
    	turns[id] = 1;
        int maxturns = max(turns);
        turns[id] = maxturns + 1;
        turns = turns;
        
        for (int i = 0; i < turns.length; i++) {
            if (i != id) {
                while (turns[i] != 0 && (turns[id] > turns[i] || (turns[id] == turns[i] && id > i))) {
                    // Espera a que el otro hilo complete su sección crítica
                }
            }
        }
    }

    public void releaseLock(int id) {
        turns[id] = 0;
        turns = turns;
    }
}
