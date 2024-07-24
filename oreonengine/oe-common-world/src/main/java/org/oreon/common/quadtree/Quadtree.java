package org.oreon.common.quadtree;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.Node;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;

public abstract class Quadtree extends Node implements Runnable {

  private Thread thread;
  private Lock startUpdateQuadtreeLock;
  private Condition startUpdateQuadtreeCondition;
  private boolean isRunning;
  private int updateCounter;
  private int updateThreshold = 2;

  @Getter
  protected QuadtreeCache quadtreeCache;

  public Quadtree() {

    isRunning = false;
    startUpdateQuadtreeLock = new ReentrantLock();
    startUpdateQuadtreeCondition = startUpdateQuadtreeLock.newCondition();
    thread = new Thread(this);
    quadtreeCache = new QuadtreeCache();
  }

  public void updateQuadtree() {

    if (ContextHolder.getContext().getCamera().isCameraMoved()) {
      updateCounter++;
    }

    if (updateCounter == updateThreshold) {
      for (Node node : getChildren()) {
        ((QuadtreeNode) node).updateQuadtree();
      }
      updateCounter = 0;
    }
  }

  public void start() {
    thread.start();
  }

  @Override
  public void run() {

    isRunning = true;

    while (isRunning) {

      startUpdateQuadtreeLock.lock();
      try {
        startUpdateQuadtreeCondition.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        startUpdateQuadtreeLock.unlock();
      }

      updateQuadtree();
    }
  }

  ;

  public void signal() {

    startUpdateQuadtreeLock.lock();
    try {
      startUpdateQuadtreeCondition.signal();
    } finally {
      startUpdateQuadtreeLock.unlock();
    }
  }

  @Override
  public void shutdown() {

    isRunning = false;
  }

  ;

  @Override
  public void update() {
  }

  public abstract QuadtreeNode createChildChunk(Map<NodeComponentType, NodeComponent> components,
      QuadtreeCache quadtreeCache, Transform worldTransform,
      Vec2f location, int levelOfDetail, Vec2f index);

}
