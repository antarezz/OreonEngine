package org.oreon.core.query;

import java.nio.IntBuffer;

import org.oreon.core.light.Light;
import org.oreon.core.scenegraph.Renderable;

public abstract class OcclusionQuery {

  private int id;
  private IntBuffer buffer;
  private int occlusionFactor;

  public abstract void doQuery(Renderable object);

  public abstract void doQuery(Light light);

  public abstract void delete();

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getOcclusionFactor() {
    return occlusionFactor;
  }

  public void setOcclusionFactor(int occlusionFactor) {
    this.occlusionFactor = occlusionFactor;
  }

  public IntBuffer getBuffer() {
    return buffer;
  }

  public void setBuffer(IntBuffer buffer) {
    this.buffer = buffer;
  }
}
