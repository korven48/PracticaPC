package auxiliar;

import java.util.HashMap;

import concurrente.ReadWriteSynchronizer;
import concurrente.monitores.MonitorNormal;

public class ConcurrentHashMap<K, V> extends HashMap<K, V>{
	// Not all methods are implemented
	private static final long serialVersionUID = 1L;
	private static final ReadWriteSynchronizer synchronizer = new MonitorNormal();
	
	@Override
	public V put(K key, V value) {
		try {
			synchronizer.requestWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		V result = super.put(key, value);
		try {
			synchronizer.releaseWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public V remove(Object key) {
		try {
			synchronizer.requestWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		V result = super.remove(key);
		try {
			synchronizer.releaseWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		try {
			synchronizer.requestWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean result = super.remove(key, value);
		try {
			synchronizer.releaseWrite();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public V get(Object key) {
		try {
			synchronizer.requestRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		V result = super.get(key);
		try {
			synchronizer.releaseRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		try {
			synchronizer.requestRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		V result = super.getOrDefault(key, defaultValue);
		try {
			synchronizer.releaseRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
