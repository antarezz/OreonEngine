
package org.oreon.examples.vk.oreonworlds;

import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.vk.components.atmosphere.Atmosphere;
import org.oreon.vk.components.water.Water;
import org.oreon.vk.engine.VkDeferredEngine;

public class VkOreonworlds {
	
	public static void main(String[] args) {
		
		VkOreonContext.create();

		VkDeferredEngine renderEngine = new VkDeferredEngine();
		renderEngine.setGui(new VkSystemMonitor());
		renderEngine.init();

		renderEngine.getSceneGraph().setWater(new Water());
		renderEngine.getSceneGraph().addObject(new Atmosphere());
//		renderEngine.getSceneGraph().setTerrain(new Planet());
		
		VkOreonContext.setRenderEngine(renderEngine);
		VkOreonContext.getCoreEngine().start();
	}
}
