package xapi.time.impl;

import xapi.annotation.inject.SingletonDefault;
import xapi.time.api.Moment;
import xapi.time.api.TimeService;

@SingletonDefault(implFor = TimeService.class)
public class TimeServiceDefault extends ImmutableMoment implements TimeService {

  private static final long serialVersionUID = 1130197439830993337L;

  public TimeServiceDefault() {
    super(System.currentTimeMillis());
  }

  // multiplicity normalizer for nanos.
  // also a handy static lock.
  protected static final Double nano = 0.000000001;

  protected double now;
  private double delta;

  @Override
  public double birth() {
    return super.millis();
  }

  @Override
  public Moment now() {
    return new ImmutableMoment(System.currentTimeMillis());
  }

  @Override
  public Moment clone(Moment moment) {
    return new ImmutableMoment(moment.millis());
  }

  @Override
  public double millis() {
    return now;
  }

  @Override
  public Moment nowPlusOne() {
    double later;
    // lets lock on a private field instead of ourself.
    synchronized (nano) {
      later = (delta += nano);
    }
    return new ImmutableMoment(later);
  }

  @Override
  public void tick() {
    // low precision, but deterministic and atomic
    now = System.currentTimeMillis();
    delta = 0;
  }

  @Override
  public void runLater(Runnable runnable) {
    new Thread(runnable).start();
  }
}
