package org.oreon.examples.gl.oreonworlds;

import lombok.extern.log4j.Log4j2;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.context.GLOreonContext;
import org.oreon.examples.gl.oreonworlds.ocean.Ocean;
import org.oreon.examples.gl.oreonworlds.terrain.Terrain;
import org.oreon.gl.components.atmosphere.Atmosphere;
import org.oreon.gl.components.terrain.shader.TerrainShader;
import org.oreon.gl.components.terrain.shader.TerrainShadowShader;
import org.oreon.gl.components.terrain.shader.TerrainWireframeShader;
import org.oreon.gl.engine.GLDeferredEngine;

@Log4j2
public class GLOreonworlds {

  public static void main(String[] args) {

    try {
      ContextHolder.setContext(new GLOreonContext());

      GLDeferredEngine renderEngine = new GLDeferredEngine();
      //		renderEngine.setGui(new GLSystemMonitor());
      renderEngine.init();

      renderEngine.getSceneGraph().addObject(new Atmosphere());
      renderEngine.getSceneGraph().setWater(new Ocean());
      renderEngine.getSceneGraph().setTerrain(new Terrain(TerrainShader.getInstance(),
          TerrainWireframeShader.getInstance(), TerrainShadowShader.getInstance()));

      //		renderEngine.getSceneGraph().getRoot().addChild(new Palm01ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Plant01ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Grass01ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Tree02ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Tree01ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Rock01ClusterGroup());
      //		renderEngine.getSceneGraph().getRoot().addChild(new Rock02ClusterGroup());

      ContextHolder.getContext().setRenderEngine(renderEngine);
      ContextHolder.getContext().getCoreEngine().start();
    } catch (Exception e) {
      log.error(e);
      System.exit(1);
    }
  }
}
