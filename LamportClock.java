public class LamportClock {
    private int clock;
    
    public LamportClock() {
        this.clock = 0;
    }

    // Increments the local lamport clock
    public synchronized void increment() {
        clock++;
    }

    // Updates the clock based on the received timestamp
    public synchronized void update(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }

    // Gets the current clock value
    public synchronized int getClock() {
        return clock;
    }

    // Sets the current clock value
    public synchronized void setClock(int clock) {
        this.clock = clock;
    }
}