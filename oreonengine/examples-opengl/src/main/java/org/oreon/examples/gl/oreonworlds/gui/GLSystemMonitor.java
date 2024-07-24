package org.oreon.examples.gl.oreonworlds.gui;

import java.lang.management.ManagementFactory;
import org.oreon.common.ui.UIScreen;
import org.oreon.core.CoreEngine;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec4f;
import org.oreon.gl.components.ui.GLColorPanel;
import org.oreon.gl.components.ui.GLDynamicTextPanel;
import org.oreon.gl.components.ui.GLGUI;
import org.oreon.gl.components.ui.GLStaticTextPanel;
import org.oreon.gl.components.ui.GLTexturePanel;

public class GLSystemMonitor extends GLGUI {

  @SuppressWarnings("restriction")
  private com.sun.management.OperatingSystemMXBean bean;

  @SuppressWarnings("restriction")
  public void init() {
    super.init();
    bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    float vHeaderOffset =
        (float) ContextHolder.getContext().getConfig().getWindowHeight() / (float) ContextHolder.getContext()
            .getConfig().getFrameHeight();

    UIScreen screen0 = new UIScreen();
    screen0.getElements().add(new GLColorPanel(new Vec4f(0, 0, 0, 0.5f), 0,
        ContextHolder.getContext().getConfig().getWindowHeight() - ((int) (180 * vHeaderOffset)), 240, 260,
        panelMeshBuffer));
    screen0.getElements().add(new GLStaticTextPanel("FPS:", 10,
        ContextHolder.getContext().getConfig().getWindowHeight() - ((int) (40 * vHeaderOffset)), 40, 40, fontsTexture));
    screen0.getElements().add(new GLStaticTextPanel("CPU:", 10,
        ContextHolder.getContext().getConfig().getWindowHeight() - ((int) (80 * vHeaderOffset)), 40, 40, fontsTexture));
    screen0.getElements().add(new GLDynamicTextPanel("000", 110,
        ContextHolder.getContext().getConfig().getWindowHeight() - ((int) (40 * vHeaderOffset)), 40, 40, fontsTexture));
    screen0.getElements().add(new GLDynamicTextPanel("000", 110,
        ContextHolder.getContext().getConfig().getWindowHeight() - ((int) (80 * vHeaderOffset)), 40, 40, fontsTexture));
    screen0.getElements().add(new GLTexturePanel("textures/logo/OpenGL_Logo.png", 0,
        ContextHolder.getContext().getConfig().getWindowHeight() - 175, 240, 100, panelMeshBuffer));
    getScreens().add(screen0);
  }

  @SuppressWarnings("restriction")
  public void update() {

    getScreens().get(0).getElements().get(3).update(Integer.toString(CoreEngine.getFps()));
    String cpuLoad = Double.toString(bean.getSystemCpuLoad());
    if (cpuLoad.length() == 3) {
      cpuLoad = cpuLoad.substring(2, 3);
    } else {
      cpuLoad = cpuLoad.substring(2, 4);
    }
    getScreens().get(0).getElements().get(4).update(cpuLoad + "%");
  }
}
