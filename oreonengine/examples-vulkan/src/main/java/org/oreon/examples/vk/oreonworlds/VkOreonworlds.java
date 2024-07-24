
package org.oreon.examples.vk.oreonworlds;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.vk.components.atmosphere.Atmosphere;
import org.oreon.vk.components.water.Water;
import org.oreon.vk.engine.VkDeferredEngine;

public class VkOreonworlds {

  public static void main(String[] args) {
    ContextHolder.setContext(new VkOreonContext());

    VkDeferredEngine renderEngine = new VkDeferredEngine();
    renderEngine.setGui(new VkSystemMonitor());
    renderEngine.init();

    renderEngine.getSceneGraph().setWater(new Water());
    renderEngine.getSceneGraph().addObject(new Atmosphere());
//		renderEngine.getSceneGraph().setTerrain(new Planet());

    ContextHolder.getContext().setRenderEngine(renderEngine);
    ContextHolder.getContext().getCoreEngine().start();
  }
}
