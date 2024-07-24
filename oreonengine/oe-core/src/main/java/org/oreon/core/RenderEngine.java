package org.oreon.core;

import lombok.Getter;

import org.oreon.core.context.Config;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.scenegraph.Camera;
import org.oreon.core.scenegraph.Scenegraph;

public abstract class RenderEngine {

  @Getter
  protected Scenegraph sceneGraph;
  protected Config config;
  protected Camera camera;

  public void init() {
    sceneGraph = new Scenegraph();
    config = ContextHolder.getContext().getConfig();
    camera = ContextHolder.getContext().getCamera();
    camera.init();
  }

  public abstract void render();

  public void update() {
    camera.update();
    sceneGraph.update();
    sceneGraph.updateLights();
  }

  public void shutdown() {

    // important to shutdown scenegraph before render-engine, since
    // thread safety of instancing clusters.
    // scenegraph sets isRunning to false, render-engine signals all
    // waiting threads to shutdown
    sceneGraph.shutdown();
  }

}
