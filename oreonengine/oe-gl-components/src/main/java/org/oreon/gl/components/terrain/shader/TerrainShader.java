package org.oreon.gl.components.terrain.shader;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import org.oreon.common.quadtree.ChunkConfig;
import org.oreon.common.quadtree.QuadtreeNode;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.context.GLOreonContext;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.math.Vec2f;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;
import org.oreon.gl.components.terrain.GLTerrainConfig;

public class TerrainShader extends GLShaderProgram {

	private static TerrainShader instance = null;

	public static TerrainShader getInstance() {
		
		if (instance == null) {
			instance = new TerrainShader();
		}
		return instance;
	}

	protected TerrainShader() {
		
		super();

		addVertexShader(ResourceLoader.loadShader("shaders/terrain/terrain.vert", "lib.glsl"));
		addTessellationControlShader(ResourceLoader.loadShader("shaders/terrain/terrain.tesc", "lib.glsl"));
		addTessellationEvaluationShader(ResourceLoader.loadShader("shaders/terrain/terrain.tese", "lib.glsl"));
		addGeometryShader(ResourceLoader.loadShader("shaders/terrain/terrain.geom", "lib.glsl"));
		addFragmentShader(ResourceLoader.loadShader("shaders/terrain/terrain.frag", "lib.glsl"));
		compileShader();

		addUniform("localMatrix");
		addUniform("worldMatrix");

		addUniform("index");
		addUniform("gap");
		addUniform("lod");
		addUniform("location");
		addUniform("isRefraction");
		addUniform("isReflection");
		addUniform("isCameraUnderWater");

		addUniform("caustics");
		addUniform("dudvCaustics");
		addUniform("distortionCaustics");
		addUniform("underwaterBlurFactor");

		addUniform("heightmap");
		addUniform("normalmap");
		addUniform("splatmap");
		addUniform("yScale");
		addUniform("reflectionOffset");

		for (int i = 0; i < 3; i++) {
			addUniform("materials[" + i + "].diffusemap");
			addUniform("materials[" + i + "].normalmap");
			addUniform("materials[" + i + "].heightmap");
			addUniform("materials[" + i + "].heightScaling");
			addUniform("materials[" + i + "].uvScaling");
		}
		
		addUniform("clipplane");

		addUniformBlock("Camera");
		addUniformBlock("DirectionalLight");
	}

	@Override
	public void updateUniforms(Renderable object)
	{	
		bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
		bindUniformBlock("DirectionalLight", Constants.DirectionalLightUniformBlockBinding);
		
		setUniform("clipplane", BaseOreonContext.getConfig().getClipplane());
		setUniformi("isRefraction", BaseOreonContext.getConfig().isRenderRefraction() ? 1 : 0);
		setUniformi("isReflection", BaseOreonContext.getConfig().isRenderReflection() ? 1 : 0);
		setUniformi("isCameraUnderWater", BaseOreonContext.getConfig().isRenderUnderwater() ? 1 : 0);
		
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
		terrConfig.getNormalmap().bind();
		setUniformi("normalmap", 1);
		
		glActiveTexture(GL_TEXTURE2);
		terrConfig.getSplatmap().bind();
		setUniformi("splatmap", 2);
		
		setUniformi("lod", lod);
		setUniform("index", index);
		setUniformf("gap", gap);
		setUniform("location", location);
		
		setUniformf("yScale", terrConfig.getVerticalScaling());
		setUniformf("reflectionOffset", terrConfig.getReflectionOffset());
		
		glActiveTexture(GL_TEXTURE3);
		GLOreonContext.getResources().getUnderwaterCausticsMap().bind();
		setUniformi("caustics", 3);
		glActiveTexture(GL_TEXTURE4);
		GLOreonContext.getResources().getUnderwaterDudvMap().bind();
		setUniformi("dudvCaustics", 4);
		if (GLOreonContext.getResources().getWaterConfig() != null){
			setUniformf("distortionCaustics", GLOreonContext.getResources().getWaterConfig().getDistortion());
			setUniformf("underwaterBlurFactor", GLOreonContext.getResources().getWaterConfig().getUnderwaterBlur());
		}
		
		int texUnit = 5;
		for (int i=0; i<3; i++){
			
			glActiveTexture(GL_TEXTURE0 + texUnit);
			terrConfig.getMaterials().get(i).getDiffusemap().bind();
			setUniformi("materials[" + i + "].diffusemap", texUnit);
			texUnit++;
			
			glActiveTexture(GL_TEXTURE0 + texUnit);
			terrConfig.getMaterials().get(i).getHeightmap().bind();
			setUniformi("materials[" + i + "].heightmap", texUnit);
			texUnit++;
			
			glActiveTexture(GL_TEXTURE0 + texUnit);
			terrConfig.getMaterials().get(i).getNormalmap().bind();
			setUniformi("materials[" + i + "].normalmap", texUnit);
			texUnit++;
			
			setUniformf("materials[" + i + "].heightScaling", terrConfig.getMaterials().get(i).getHeightScaling());
			setUniformf("materials[" + i + "].uvScaling", terrConfig.getMaterials().get(i).getHorizontalScaling());
		}
	}
}
