package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 *
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T result;
	private long startTime;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		result = null;
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 *
	 */
	public T get()  {
		synchronized (this){ //really need to block all?
			if(result == null) //there is no need to while because when there is notify only after there is a result (that can not be destroyed)
			{
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
			//this.notifyAll(); //notify()? this.notifyAll()??
			return result;
		}
	}

	/**
	 * Resolves the result of this Future object.
	 */
	public void resolve (T result) {
		synchronized (this){ //really need to block all?
			this.result =result;
			this.notifyAll(); //notify() - can not do notify if get !notify
		}
	}

	/**
	 * @return true if this object has been resolved, false otherwise
	 */
	public boolean isDone() {
		synchronized (this) { //really need to block all?
			return result != null;
		}
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved,
	 * This method is non-blocking, it has a limited amount of time determined
	 * by {@code timeout}
	 * <p>
	 * @param timeout 	the maximal amount of time units to wait for the result.
	 * @param unit		the {@link TimeUnit} time units to wait.
	 * @return return the result of type T if it is available, if not,
	 * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
	 *         elapsed, return null.
	 */
	public T get(long timeout, TimeUnit unit)  {
		synchronized (this){ //really need to block all?
			if(result == null) //dont need while because when there is notify the is an result that can not be destroyed
			{
				startTime = System.nanoTime();
				//while(System.nanoTime()-startTime<100)
				try {
					this.wait(unit.toNanos(timeout));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//this.notifyAll(); //notify()? this.notifyAll()??
			return result; //can be also null
		}
	}
}