package org.oreon.core.gl.query;

import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT_AVAILABLE;
import static org.lwjgl.opengl.GL15.GL_SAMPLES_PASSED;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glDeleteQueries;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL15.glGetQueryObjectiv;
import static org.lwjgl.opengl.GL15.glGetQueryObjectui;

import org.oreon.core.light.Light;
import org.oreon.core.query.OcclusionQuery;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.BufferUtil;

public class GLOcclusionQuery extends OcclusionQuery {

  public GLOcclusionQuery() {
    setBuffer(BufferUtil.createIntBuffer(1));
    setId(glGenQueries());
  }

  public void doQuery(Renderable object) {

    glColorMask(false, false, false, false);
    glDepthMask(false);

    // Begin occlusion query
    glBeginQuery(GL_SAMPLES_PASSED, getId());
    // Every pixel that passes the depth test now gets added to the result

    object.render();
    glFinish();
    glEndQuery(GL_SAMPLES_PASSED);

    // Now get the number of pixels passed
    int querystate = 0;
    while (querystate == 0) {
      querystate = glGetQueryObjectui(getId(), GL_QUERY_RESULT_AVAILABLE);
    }
    glFinish();
    glGetQueryObjectiv(getId(), GL_QUERY_RESULT, getBuffer());

    setOcclusionFactor(getBuffer().get(0));

    glColorMask(true, true, true, true);
    glDepthMask(true);
  }

  public void doQuery(Light light) {

    glColorMask(false, false, false, false);
    glDepthMask(false);

    // Begin occlusion query
    glBeginQuery(GL_SAMPLES_PASSED, getId());
    // Every pixel that passes the depth test now gets added to the result

    light.render();
    glFinish();
    glEndQuery(GL_SAMPLES_PASSED);

    // Now get the number of pixels passed
    int querystate = 0;
    while (querystate == 0) {
      querystate = glGetQueryObjectui(getId(), GL_QUERY_RESULT_AVAILABLE);
    }
    glFinish();
    glGetQueryObjectiv(getId(), GL_QUERY_RESULT, getBuffer());

    setOcclusionFactor(getBuffer().get(0));

    if (getOcclusionFactor() < 8000) {
      setOcclusionFactor(0);
    }
    ;

    glColorMask(true, true, true, true);
    glDepthMask(true);
  }

  public void delete() {
    glDeleteQueries(getId());
  }
}
