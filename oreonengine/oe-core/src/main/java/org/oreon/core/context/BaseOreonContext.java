package org.oreon.core.context;

import lombok.Getter;
import lombok.Setter;
import org.oreon.core.CoreEngine;
import org.oreon.core.RenderEngine;
import org.oreon.core.platform.GLFWInput;
import org.oreon.core.platform.Window;
import org.oreon.core.scenegraph.Camera;

@Getter
public class BaseOreonContext<C extends Camera, W extends Window, R extends OreonResource> implements
    OreonContext<C, W> {

  private Config config;
  private GLFWInput input;
  @Setter
  private RenderEngine renderEngine;
  @Setter
  private CoreEngine coreEngine;

  private C camera;
  private W window;
  private R resources;

  protected BaseOreonContext(final C camera, final W window, final R resources,
      final Config config, final RenderEngine renderEngine) {
    this.camera = camera;
    this.window = window;
    this.resources = resources;

    this.coreEngine = new CoreEngine();
    this.config = config;
    this.input = new GLFWInput();

    this.renderEngine = renderEngine;
  }
}
