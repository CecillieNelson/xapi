package xapi.process.impl;

import static xapi.util.X_Debug.debug;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import xapi.collect.impl.AbstractMultiInitMap;
import xapi.log.X_Log;
import xapi.process.api.ConcurrentEnvironment;
import xapi.process.api.ConcurrentEnvironment.Priority;
import xapi.process.api.Process;
import xapi.process.api.ProcessController;
import xapi.process.service.ConcurrencyService;
import xapi.util.X_Runtime;
import xapi.util.X_Util;
import xapi.util.api.ConvertsValue;
import xapi.util.api.ReceivesValue;

public abstract class ConcurrencyServiceAbstract implements ConcurrencyService{

  protected class WrappedRunnable implements Runnable {

    private Runnable core;

    public WrappedRunnable(Runnable core) {
      this.core = core;
    }

    @Override
    public void run() {

      core.run();
      destroy(Thread.currentThread(), threadFlushTime());

      //Now that we've finished the job we were told to do,
      //let's attempt to reuse our current thread


      //We should choose how to "steal work" wisely,
      //using an algorithm which has known ETA times,
      //so a thread which knows about an upcoming task
      //can choose to reject long-running tasks;

      //This will require coordination around the workload of other threads.
      //If there is already one thread spinning, looking for work,
      //and that thread is spending less than X % of time working,
      //then we can take on any job.

      //There will also be a necessary priority calculation;
      //A big pending high priority job should take any live threads,
      //and we can spin up more replacements for other tasks if needed.

      //In order to prevent attention-starvation, a task's priority will get
      //bumped up if it has to wait too long.

      //Using a min-max latency and eta will help in determining the best job
      //to take.

      //First, check for immediate jobs scheduled by the current thread.
      //this will help in cases when a thread simply wants to yield,
      //or when a forking process wants to continue immediately,
      //or when gluing together methods into a process.


      //Next, check for high priority jobs scheduled by any thread.
      //Anything with a timeout at or near expiration should be taken.
      //If there are known tiny jobs available with a known eta less than
      //the wait time for the high priority job, that job should be taken as well.
      //If no such jobs exist, scan the work queue to elevate any starving tasks

      //If no high priority jobs are waiting, scan the work queue for either
      //a) work to do; if timeout ~expired, take and run immediately
      //b) tasks to elevate;
      //the iterator supplied when looking for work should elevate on its own.

      //if no task is found, die unless you are the last thread.
      //if timeout > now, drain the iterator to make sure anything needing
      //elevation gets it.
    }

  }

  protected class EnviroMap extends
    AbstractMultiInitMap<Thread,ConcurrentEnvironment,UncaughtExceptionHandler> {

    public EnviroMap() {
      super(new ConvertsValue<Thread,String>() {
        @Override
        public String convert(Thread from) {
          return from.getName();
        }
      });
    }

    @Override
    protected ConcurrentEnvironment initialize(Thread key, UncaughtExceptionHandler params) {
      if (key.getState() == State.TERMINATED) {
        //send an exception...
        params.uncaughtException(key, new ThreadDeath());
      }
      if (key.isInterrupted()) {
        params.uncaughtException(key, new InterruptedException());
      }
      X_Log.debug("Initializing Concurrent Environment", key);
      return initializeEnvironment(key, params);
    }
    @Override
    protected UncaughtExceptionHandler defaultParams() {
      return Thread.currentThread().getUncaughtExceptionHandler();
    }
  }


  protected abstract ConcurrentEnvironment initializeEnvironment(Thread key, UncaughtExceptionHandler params);

  protected int threadFlushTime() {
    return 2000;
  }

  private final EnviroMap environments = initMap();

  private AtomicInteger threadCount = new AtomicInteger();

  @Override
  public Thread newThread(Runnable cmd) {
    WrappedRunnable wrapped = wrap(cmd);
    Thread childThread = new Thread(wrapped);
    childThread.setName(cmd.getClass().getName()+"_"+threadCount.incrementAndGet());
    Thread running = Thread.currentThread();
    ConcurrentEnvironment enviro = environments.get(running, running.getUncaughtExceptionHandler());
    enviro.pushThread(childThread);
    return childThread;
  }

  /**
   * Allow all subclasses to wrap Runnables for custom behavior.
   * @param cmd - The supplied Runnable to execute.
   * @return - A wrapped Runnable, or cmd if not desired.
   *
   * This method is very useful for running benchmarks or testing assertions.
   */
  protected WrappedRunnable wrap(Runnable cmd) {
    if (cmd instanceof WrappedRunnable)
      return (WrappedRunnable)cmd;
    return new WrappedRunnable(cmd);
  }

  protected ConcurrentEnvironment currentEnvironment() {
    Thread running = Thread.currentThread();
    return environments.get(running, running.getUncaughtExceptionHandler());
  }

  protected EnviroMap initMap() {
    return new EnviroMap();
  }

  @Override
  public <T> ProcessController<T> newProcess(Process<T> process) {
    return new ProcessController<T>(process);
  }

  @Override
  public <T> void resolve(final Future<T> future, final ReceivesValue<T> receiver) {
    if (future.isDone()) {
      callback(future, receiver);
      return;
    }
    //The future isn't done.  Let's push a task into the enviro.
    Thread otherThread = getFuturesThread();
    ConcurrentEnvironment enviro = environments.get(otherThread, otherThread.getUncaughtExceptionHandler());
    enviro.monitor(Priority.Low, new Provider<Boolean>() {
      @Override
      public Boolean get() {
        return future.isDone();
      }
    }, new Runnable() {
      @Override
      public void run() {
        callback(future, receiver);
      }
    });
  }

  /**
   * Allows multi-threaded environments to have a single thread dedicated to
   * monitoring futures for completion.
   * @return - The thread to be used for monitoring futures
   */
  protected Thread getFuturesThread() {
    return Thread.currentThread();
  }

  protected <T> void callback(Future<T> future, ReceivesValue<T> receiver) {
    try {
      receiver.set(future.get());
      return;
    } catch (InterruptedException e) {
      debug(e);
      Thread.interrupted();
    } catch (ExecutionException e) {
      debug(e);
      throw X_Util.rethrow(X_Util.unwrap(e));
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean kill(Thread thread, int timeout) {
    if (destroy(thread, timeout))
      return true;
    try {
      thread.interrupt();
      return false;
    }catch (Exception e) {
      thread.stop();
      return false;
    }
  }

  private boolean destroy(Thread thread, int timeout) {
    if (environments.hasValue(thread.getName())) {
      ConcurrentEnvironment enviro = environments.get(thread, thread.getUncaughtExceptionHandler());
      boolean success = enviro.destroy(timeout);
      environments.removeValue(thread.getName());
      return success;
    }
    return true;
  }

  @Override
  public boolean trySleep(float millis) {
    if (Thread.interrupted())
      return false;
    float leftover = millis - ((int)millis);
    try {
      Thread.sleep((long)millis, (int)(leftover * 1000000));
      return true;
    }catch (InterruptedException e) {
      Thread.interrupted();
      return false;
    }
  }

  @Override
  public boolean flush(Thread thread, int timeout) {
    ConcurrentEnvironment enviro = environments.getValue(thread.getName());
    if (thread == Thread.currentThread()) {
      if (enviro == null)
        return true;// nothin' to do here!
      long deadline = System.currentTimeMillis()+timeout;
      while (enviro.flush((int)(deadline-System.currentTimeMillis()))) {
        int timeLeft = (int)(deadline-System.currentTimeMillis());
        if (timeLeft<1)
          return false;
        //join a thread, if available
        Iterator<Thread> iter = enviro.getThreads().iterator();
        try {
          Thread next;
          synchronized (enviro) {
            if (iter.hasNext()) {
              next = iter.next();
              iter.remove();
            }else
              return true;
          }

        if (next != null)
          next.join(timeLeft);
        } catch (InterruptedException e) {
          destroy(Thread.currentThread(), timeLeft);
          Thread.currentThread().interrupt();
        }
        if (System.currentTimeMillis()>deadline)
          return false;
      }
      return true;
    }else {
      if (enviro != null)
        enviro.scheduleFlush(timeout);
      return false;
    }
  }

  @Override
  public double now() {
    return System.currentTimeMillis();
  }

  @Override
  public double threadStartTime(Thread thread) {
    return environments.get(thread, thread.getUncaughtExceptionHandler()).startTime();
  }

  public boolean isMultiThreaded() {
    return X_Runtime.isMultithreaded();
  }


  @Override
  public void runDeferred(Runnable cmd) {
    ConcurrentEnvironment enviro = currentEnvironment();
    enviro.pushDeferred(cmd);
  }

  @Override
  public void runEventually(Runnable cmd) {
    ConcurrentEnvironment enviro = currentEnvironment();
    enviro.pushEventually(cmd);
  }

  @Override
  public void runFinally(Runnable cmd) {
    ConcurrentEnvironment enviro = currentEnvironment();
    enviro.pushFinally(cmd);
  }


}
