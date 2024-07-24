package org.oreon.examples.gl.oreonworlds.terrain;

import org.lwjgl.glfw.GLFW;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.gl.components.terrain.GLTerrain;

public class Terrain extends GLTerrain {

  public Terrain(GLShaderProgram shader, GLShaderProgram wireframe, GLShaderProgram shadow) {
    super(shader, wireframe, shadow);
  }

  @Override
  public void update() {

    super.update();

    // create new heightmap from random fractals
    if (ContextHolder.getContext().getInput().isKeyPushed(GLFW.GLFW_KEY_L)) {

//			List<FractalMap> newFractals = new ArrayList<>();
//			
//			for (FractalMap fractal : getConfiguration().getFractals()){
//				fractal.getHeightmap().delete();
//				
//				FractalMap newfractal = new FractalMap(fractal.getN(), fractal.getL(),
//						fractal.getAmplitude(), fractal.getDirection(), fractal.getIntensity(),
//						fractal.getCapillar(), fractal.getAlignment(), fractal.isChoppy(),
//						fractal.getScaling(), fractal.getHeightStrength(), fractal.getNormalStrength(),
//						new Random().nextInt(1000));
//				newFractals.add(newfractal);
//			}
//			
//			// update configurations
//			getConfiguration().getFractals().clear();
//			for (FractalMap newFracral : newFractals){
//				getConfiguration().getFractals().add(newFracral);
//			}
//			memFree(getConfiguration().getHeightmapDataBuffer());
//			getConfiguration().renderFractalMap();
//			getConfiguration().createHeightmapDataBuffer();

    }
  }
}
