package org.oreon.core.context;

import org.oreon.core.CoreEngine;
import org.oreon.core.RenderEngine;
import org.oreon.core.platform.GLFWInput;
import org.oreon.core.platform.Window;
import org.oreon.core.scenegraph.Camera;

public interface OreonContext<C extends Camera, W extends Window> {

  Config getConfig();

  GLFWInput getInput();

  RenderEngine getRenderEngine();

  C getCamera();

  W getWindow();

  CoreEngine getCoreEngine();

  void setRenderEngine(RenderEngine renderEngine);

}
