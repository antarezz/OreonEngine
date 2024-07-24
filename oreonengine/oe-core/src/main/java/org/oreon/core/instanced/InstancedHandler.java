package org.oreon.core.instanced;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InstancedHandler {

  private Lock lock = new ReentrantLock();
  private Condition condition = lock.newCondition();

  private static InstancedHandler instance = null;

  public static InstancedHandler getInstance() {
    if (instance == null) {
      instance = new InstancedHandler();
    }
    return instance;
  }

  public void signalAll() {
    lock.lock();
    try {
      condition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public Lock getLock() {
    return lock;
  }

  public void setLock(Lock lock) {
    this.lock = lock;
  }
}
