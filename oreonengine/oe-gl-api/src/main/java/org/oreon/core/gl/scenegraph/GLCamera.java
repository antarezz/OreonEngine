package org.oreon.core.gl.scenegraph;

import org.oreon.core.gl.memory.GLUniformBuffer;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.Camera;
import org.oreon.core.util.Constants;

public class GLCamera extends Camera {

  private GLUniformBuffer ubo;

  public GLCamera() {

    super(new Vec3f(580.4513f, 103.191055f, -2971.5518f), new Vec3f(0.54644656f, -0.16165589f, 0.82174426f),
        new Vec3f(0.08951365f, 0.98684716f, 0.13461028f));
  }

  @Override
  public void init() {

    ubo = new GLUniformBuffer();
    ubo.setBinding_point_index(Constants.CameraUniformBlockBinding);
    ubo.bindBufferBase();
    ubo.allocate(bufferSize);
  }

  @Override
  public void update() {
    super.update();

    ubo.updateData(floatBuffer, bufferSize);
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    // destroy ubo
  }

}
