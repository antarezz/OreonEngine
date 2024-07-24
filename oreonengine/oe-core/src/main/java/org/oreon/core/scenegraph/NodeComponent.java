package org.oreon.core.scenegraph;

import org.oreon.core.math.Transform;

public abstract class NodeComponent implements Cloneable {

  private Renderable parent;

  public void update() {
  }

  ;

  public void input() {
  }

  ;

  public void render() {
  }

  ;

  public void shutdown() {
  }

  ;

  public Renderable getParent() {
    return parent;
  }

  public void setParent(Renderable parent) {
    this.parent = parent;
  }

  public Transform getTransform() {
    return parent.getWorldTransform();
  }

  public NodeComponent clone() throws CloneNotSupportedException {
    return (NodeComponent) super.clone();
  }
}