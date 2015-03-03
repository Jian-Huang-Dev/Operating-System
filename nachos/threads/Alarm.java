package nachos.threads;

import java.util.LinkedList;

import nachos.machine.Machine;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {

		// disable the interrupt status
		boolean intStatus = Machine.interrupt().disable();

		int i;
		// loop through to check every element in the queue
		// to see if the time is up
		// if so, remove the item and add it to the ready list
    	for (i = waitQueue.size() - 1 ; i >= 0 ; i--)
    	{
    		if (Machine.timer().getTime() >= waitQueue.get(i).wakeTime)
    		{
    			waitQueue.removeFirst().thread.ready();
    		}
    	}
		
		// restore interrupt status
		Machine.interrupt().restore(intStatus);

		// current thread yields and causes context switches
		KThread.yield();
	}

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
		// disable interrupt status
		boolean intStatus = Machine.interrupt().disable();

		// get the advanced alarm time
		long wakeTime = Machine.timer().getTime() + x;
		// add thread to the waitting list
		waitQueue.add(new Waiter(KThread.currentThread(), wakeTime));
		// put the thread to sleep
		KThread.sleep();

		// restore interrupt status
		Machine.interrupt().restore(intStatus);

		/*
		 * while (wakeTime > Machine.timer().getTime()) KThread.yield();
		 */
	}

	// waiter class to store KThead and wake-time
	protected class Waiter {
		private KThread thread;
		private long wakeTime;

		// constructor to store KThread and wake-time parameters
		Waiter(KThread thread, long wakeTime) {
			this.thread = thread;
			this.wakeTime = wakeTime;
		}
	}

	// create a queue to sort the waiters
	//private PriorityQueue<Waiter> waitQueue = new PriorityQueue<Waiter>();
	private LinkedList<Waiter> waitQueue = new LinkedList<Waiter>();
}
