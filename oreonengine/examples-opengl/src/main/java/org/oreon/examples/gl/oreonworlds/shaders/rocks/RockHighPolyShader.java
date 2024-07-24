package org.oreon.examples.gl.oreonworlds.shaders.rocks;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.context.GLOreonContext;
import org.oreon.core.gl.instanced.GLInstancedCluster;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.instanced.InstancedCluster;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.model.Material;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.Constants;
import org.oreon.core.util.ResourceLoader;

public class RockHighPolyShader extends GLShaderProgram{

	private static RockHighPolyShader instance = null;

	public static RockHighPolyShader getInstance() 
	{
	    if(instance == null) 
	    {
	    	instance = new RockHighPolyShader();
	    }
	      return instance;
	}
	
	protected RockHighPolyShader()
	{
		super();
		
		addVertexShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockHighPoly_VS.glsl"));
		addGeometryShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockHighPoly_GS.glsl"));
		addFragmentShader(ResourceLoader.loadShader("oreonworlds/shaders/assets/Rock_Shader/RockHighPoly_FS.glsl"));
		compileShader();
		
		addUniform("material.diffusemap");
		addUniform("material.normalmap");
		addUniform("material.shininess");
		addUniform("material.emission");
		addUniform("clipplane");
		addUniform("scalingMatrix");
		addUniform("isReflection");
		addUniform("isRefraction");
		addUniform("isCameraUnderWater");
		
		addUniform("caustics");
		addUniform("dudvCaustics");
		addUniform("distortionCaustics");
		
		addUniformBlock("worldMatrices");
		addUniformBlock("modelMatrices");
		addUniformBlock("Camera");
		
		for (int i=0; i<100; i++)
		{
			addUniform("matrixIndices[" + i + "]");
		}
	}	
	
	public void updateUniforms(Renderable object)
	{
		bindUniformBlock("Camera", Constants.CameraUniformBlockBinding);
		((GLInstancedCluster) object.getParentNode()).getWorldMatricesBuffer().bindBufferBase(0);
		bindUniformBlock("worldMatrices", 0);
		((GLInstancedCluster) object.getParentNode()).getModelMatricesBuffer().bindBufferBase(1);
		bindUniformBlock("modelMatrices", 1);
		
		setUniformi("isReflection", BaseOreonContext.getConfig().isRenderReflection() ? 1 : 0);
		setUniformi("isRefraction", BaseOreonContext.getConfig().isRenderRefraction() ? 1 : 0);
		setUniform("scalingMatrix", new Matrix4f().Scaling(object.getWorldTransform().getScaling()));
		setUniform("clipplane", BaseOreonContext.getConfig().getClipplane());
		
		
		setUniformi("isCameraUnderWater", BaseOreonContext.getConfig().isRenderUnderwater() ? 1 : 0);
		
		Material material = (Material) object.getComponent(NodeComponentType.MATERIAL0);

		glActiveTexture(GL_TEXTURE0);
		material.getDiffusemap().bind();
		setUniformi("material.diffusemap", 0);
		
		glActiveTexture(GL_TEXTURE1);
		material.getNormalmap().bind();
		setUniformi("material.normalmap", 1);
		
		setUniformf("material.shininess", material.getShininess());
		setUniformf("material.emission", material.getEmission());
		
		glActiveTexture(GL_TEXTURE2);
		GLOreonContext.getResources().getUnderwaterCausticsMap().bind();
		setUniformi("caustics", 2);
		glActiveTexture(GL_TEXTURE3);
		GLOreonContext.getResources().getUnderwaterDudvMap().bind();
		setUniformi("dudvCaustics", 3);
		setUniformf("distortionCaustics", GLOreonContext.getResources().getWaterConfig().getDistortion());
		
		List<Integer> indices = ((InstancedCluster) object.getParentNode()).getHighPolyIndices();
		
		for (int i=0; i<indices.size(); i++)
		{
			setUniformi("matrixIndices[" + i +"]", indices.get(i));	
		}
	}
}
