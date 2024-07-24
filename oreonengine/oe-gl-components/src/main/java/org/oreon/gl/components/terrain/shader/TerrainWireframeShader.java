package org.oreon.gl.components.terrain.shader;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.common.quadtree.ChunkConfig;
import org.oreon.common.quadtree.QuadtreeNode;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;
import org.oreon.gl.components.terrain.GLTerrainConfig;

public class TerrainWireframeShader extends GLShaderProgram{
	
	private static TerrainWireframeShader instance = null;
	
	public static TerrainWireframeShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new TerrainWireframeShader();
	    }
	      return instance;
	}
	
	protected TerrainWireframeShader()
	{
		super();

		addVertexShader(ResourceLoader.loadShader("shaders/terrain/terrain.vert", "lib.glsl"));
		addTessellationControlShader(ResourceLoader.loadShader("shaders/terrain/terrain.tesc", "lib.glsl"));
		addTessellationEvaluationShader(ResourceLoader.loadShader("shaders/terrain/terrain.tese", "lib.glsl"));
		addGeometryShader(ResourceLoader.loadShader("shaders/terrain/terrain_wireframe.geom", "lib.glsl"));
		addFragmentShader(ResourceLoader.loadShader("shaders/terrain/terrain_wireframe.frag"));
		compileShader();
		
		addUniform("localMatrix");
		addUniform("worldMatrix");
		
		addUniform("index");
		addUniform("gap");
		addUniform("lod");
		addUniform("location");
		
		addUniform("yScale");
		addUniform("reflectionOffset");
		
		addUniform("heightmap");
		addUniform("splatmap");
		
		for (int i=0; i<4; i++){
			addUniform("materials[" + i + "].heightmap");
			addUniform("materials[" + i + "].heightScaling");
			addUniform("materials[" + i + "].uvScaling");
		}
		
		addUniform("clipplane");
		
		addUniformBlock("Camera");
	}
	
	public void updateUniforms(Renderable object)
	{	
		bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
		
		setUniform("clipplane", BaseOreonContext.getConfig().getClipplane());
		
		GLTerrainConfig terrConfig = object.getComponent(NodeComponentType.CONFIGURATION);
		
		ChunkConfig vChunkConfig = ((QuadtreeNode) object).getChunkConfig();
		
		int lod = vChunkConfig.getLod();
		Vec2f index = vChunkConfig.getIndex();
		float gap = vChunkConfig.getGap();
		Vec2f location = vChunkConfig.getLocation();
		
		setUniform("localMatrix", object.getLocalTransform().getWorldMatrix());
		setUniform("worldMatrix", object.getWorldTransform().getWorldMatrix());
			
		glActiveTexture(GL_TEXTURE0);
		terrConfig.getHeightmap().bind();
		setUniformi("heightmap", 0);
		
		glActiveTexture(GL_TEXTURE1);
		terrConfig.getSplatmap().bind();
		setUniformi("splatmap", 1);
		
		setUniformi("lod", lod);
		setUniform("index", index);
		setUniformf("gap", gap);
		setUniform("location", location);
		
		setUniformf("yScale", terrConfig.getVerticalScaling());
		setUniformf("reflectionOffset", terrConfig.getReflectionOffset());
		
		int texUnit = 4;
		for (int i=0; i<4; i++){
			
			glActiveTexture(GL_TEXTURE0 + texUnit);
			terrConfig.getMaterials().get(i).getHeightmap().bind();
			setUniformi("materials[" + i + "].heightmap", texUnit);
			texUnit++;
			
			setUniformf("materials[" + i + "].heightScaling", terrConfig.getMaterials().get(i).getHeightScaling());
			setUniformf("materials[" + i + "].uvScaling", terrConfig.getMaterials().get(i).getHorizontalScaling());
		}
	}

}
