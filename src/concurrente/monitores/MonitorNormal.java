package concurrente.monitores;

import concurrente.ReadWriteSynchronizer;

public class MonitorNormal implements ReadWriteSynchronizer{
    private int numReaders;  // contador de procesos lectores
    private int numWriters;  // contador de procesos escritores

    public MonitorNormal() {
        numReaders = 0;
        numWriters = 0;
    }

    public synchronized void requestRead() throws InterruptedException {
        numReaders++;  // aumenta el contador de procesos lectores
        while (numWriters > 0) {
            wait();  // espera a que todos los procesos terminen de escribir
        }
    }
    
    public synchronized void releaseRead() {
        numReaders--;  // disminuye el contador de procesos lectores
        notifyAll();  // notifica a todos los procesos que han terminado de leer
    }
    
    

    public synchronized void requestWrite() throws InterruptedException {
        numWriters++;  // aumenta el contador de procesos escritores
        while (numReaders > 0 || numWriters > 1) {
            wait();  // espera a que todos los procesos terminen de leer y escribir
        }
    }
    
    public synchronized void releaseWrite() {
        numWriters--;  // disminuye el contador de procesos escritores
        notifyAll();  // notifica a todos los procesos que han terminado de escribir
    }
}
