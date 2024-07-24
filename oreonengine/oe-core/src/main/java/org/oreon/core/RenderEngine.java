package org.oreon.core;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.context.Config;
import org.oreon.core.scenegraph.Camera;
import org.oreon.core.scenegraph.Scenegraph;

import lombok.Getter;

public abstract class RenderEngine {

	@Getter
	protected Scenegraph sceneGraph;
	protected Config config;
	protected Camera camera;
	
	public void init()
	{
		sceneGraph = new Scenegraph();
		config = BaseOreonContext.getConfig();
		camera = BaseOreonContext.getCamera();
		camera.init();
	}
	
	public abstract void render();
	
	public void update()
	{
		camera.update();
		sceneGraph.update();
		sceneGraph.updateLights();
	}
	
	public void shutdown(){
		
		// important to shutdown scenegraph before render-engine, since
		// thread safety of instancing clusters.
		// scenegraph sets isRunning to false, render-engine signals all
		// waiting threads to shutdown
		sceneGraph.shutdown();
	}
	
}
