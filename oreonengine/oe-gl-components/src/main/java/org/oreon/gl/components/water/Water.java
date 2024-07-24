package org.oreon.gl.components.water;

import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_CW;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_CLIP_DISTANCE6;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.oreon.common.water.WaterConfig;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.context.GLOreonContext;
import org.oreon.core.gl.framebuffer.GLFramebuffer;
import org.oreon.core.gl.memory.GLPatchVBO;
import org.oreon.core.gl.memory.GLShaderStorageBuffer;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.scenegraph.GLRenderInfo;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.parameter.WaterRenderParameter;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.math.Vec4f;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.RenderList;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.scenegraph.Scenegraph;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.Constants;
import org.oreon.core.util.MeshGenerator;
import org.oreon.gl.components.fft.FFT;
import org.oreon.gl.components.terrain.GLTerrain;
import org.oreon.gl.components.util.NormalRenderer;

import lombok.Getter;
import lombok.Setter;

public class Water extends Renderable{
	
	@Setter
	private Vec4f clipplane;
	@Setter
	private int clip_offset;
	@Getter
	private float motion;
	@Getter
	private GLTexture dudv;
	
	private GLFramebuffer reflection_fbo;
	private GLFramebuffer refraction_fbo;
	@Getter
	private GLTexture reflection_texture;
	@Getter
	private GLTexture refraction_texture;
	private RenderList reflectionRenderList;
	private RenderList refractionRenderList;
	
	@Getter
	private FFT fft;
	@Getter
	private NormalRenderer normalmapRenderer;
	private boolean isCameraUnderwater;
	
	private WaterRenderParameter renderConfig;
	
	@Getter
	private WaterConfig config;
	
	private GLShaderStorageBuffer ssbo;
	
	@Getter
	private float t_motion;
	@Getter
	private float t_distortion;
	private long systemTime = System.currentTimeMillis();

	public Water(int patches, GLShaderProgram shader, GLShaderProgram wireframeShader)
	{		
		config = new WaterConfig();
		config.loadFile("water-config.properties");
		GLOreonContext.getResources().setWaterConfig(config);
		
		GLPatchVBO meshBuffer = new GLPatchVBO();
		meshBuffer.addData(MeshGenerator.generatePatch2D4x4(patches),16);
		
		renderConfig = new WaterRenderParameter();
		
		GLRenderInfo renderInfo = new GLRenderInfo(shader, renderConfig, meshBuffer);
		GLRenderInfo wireframeRenderInfo = new GLRenderInfo(wireframeShader, renderConfig, meshBuffer);
		
		dudv = new TextureImage2D("textures/water/dudv/dudv1.jpg", SamplerFilter.Trilinear);
		
		addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
		addComponent(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);

		fft = new FFT(config.getN(), config.getL(),
				config.getAmplitude(), config.getWindDirection(),
				config.getAlignment(), config.getWindSpeed(),
				config.getCapillarWavesSupression());
		fft.setT_delta(config.getT_delta());
		fft.setChoppy(config.isChoppy());
		fft.init();
		
		normalmapRenderer = new NormalRenderer(config.getN());
		getNormalmapRenderer().setStrength(config.getNormalStrength());
		
		reflection_texture = new TextureImage2D(BaseOreonContext.getConfig().getFrameWidth()/2,
				BaseOreonContext.getConfig().getFrameHeight()/2,
				ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
		
		IntBuffer drawBuffers = BufferUtil.createIntBuffer(1);
		drawBuffers.put(GL_COLOR_ATTACHMENT0);
		drawBuffers.flip();
		
		reflection_fbo = new GLFramebuffer();
		reflection_fbo.bind();
		reflection_fbo.createColorTextureAttachment(reflection_texture.getHandle(),0);
		reflection_fbo.createDepthBufferAttachment(BaseOreonContext.getConfig().getFrameWidth()/2,
				BaseOreonContext.getConfig().getFrameHeight()/2);
		reflection_fbo.setDrawBuffers(drawBuffers);
		reflection_fbo.checkStatus();
		reflection_fbo.unbind();
		
		refraction_texture = new TextureImage2D(BaseOreonContext.getConfig().getFrameWidth()/2,
				BaseOreonContext.getConfig().getFrameHeight()/2,
				ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
		
		refraction_fbo = new GLFramebuffer();
		refraction_fbo.bind();
		refraction_fbo.createColorTextureAttachment(refraction_texture.getHandle(),0);
		refraction_fbo.createDepthBufferAttachment(BaseOreonContext.getConfig().getFrameWidth()/2,
				BaseOreonContext.getConfig().getFrameHeight()/2);
		refraction_fbo.setDrawBuffers(drawBuffers);
		refraction_fbo.checkStatus();
		refraction_fbo.unbind();
		
		refractionRenderList = new RenderList();
		reflectionRenderList = new RenderList();
	}	
	
	public void update()
	{
		isCameraUnderwater = BaseOreonContext.getCamera().getPosition().getY() < (getWorldTransform().getTranslation().getY());
		t_motion += (System.currentTimeMillis() - systemTime) * config.getWaveMotion();
		t_distortion += (System.currentTimeMillis() - systemTime) * config.getDistortion();
		systemTime = System.currentTimeMillis();
	}
	
	public void renderWireframe(){
		
		fft.render();
		
		ssbo.bindBufferBase(1);
		
		super.renderWireframe();
		
		glFinish();
	}
	
	public void render()
	{
		if (!isCameraUnderwater){
			glEnable(GL_CLIP_DISTANCE6);
			BaseOreonContext.getConfig().setRenderUnderwater(false);
		}
		else {
			BaseOreonContext.getConfig().setRenderUnderwater(true);
		}
		
		Scenegraph scenegraph = ((Scenegraph) getParentNode());
		
		BaseOreonContext.getConfig().setClipplane(clipplane);
			
		//-----------------------------------//
		//     mirror scene to clipplane     //
		//-----------------------------------//
		
		scenegraph.getWorldTransform().setScaling(1,-1,1);
			
		if (scenegraph.hasTerrain()){
				
			GLTerrain.getConfig().setVerticalScaling(
					GLTerrain.getConfig().getVerticalScaling() * -1f);
			GLTerrain.getConfig().setReflectionOffset(
					clip_offset * 2);
		}
		scenegraph.update();
		
		//-----------------------------------//
		//    render reflection to texture   //
		//-----------------------------------//

		int tempScreenResolutionX = BaseOreonContext.getConfig().getFrameWidth();
		int tempScreenResolutionY = BaseOreonContext.getConfig().getFrameHeight();
		BaseOreonContext.getConfig().setFrameWidth(tempScreenResolutionX/2);
		BaseOreonContext.getConfig().setFrameHeight(tempScreenResolutionY/2);
		glViewport(0,0,tempScreenResolutionX/2, tempScreenResolutionY/2);
		
		BaseOreonContext.getConfig().setRenderReflection(true);
		
		reflection_fbo.bind();
		renderConfig.clearScreenDeepOcean();
		glFrontFace(GL_CCW);
		
		if (!isCameraUnderwater){
			
			scenegraph.record(reflectionRenderList);
			
			reflectionRenderList.remove(this.id);
			
			reflectionRenderList.getValues().forEach(object ->
			{
				object.render();
			});
		}
		
		// glFinish() important, to prevent conflicts with following compute shaders
		glFinish(); 
		glFrontFace(GL_CW);
		reflection_fbo.unbind();
		
		BaseOreonContext.getConfig().setRenderReflection(false);
		
		//-----------------------------------//
		//   antimirror scene to clipplane   //
		//-----------------------------------//
	
		scenegraph.getWorldTransform().setScaling(1,1,1);

		if (scenegraph.hasTerrain()){
			GLTerrain.getConfig().setVerticalScaling(
					GLTerrain.getConfig().getVerticalScaling() / -1f);
			GLTerrain.getConfig().setReflectionOffset(0);
		}

		scenegraph.update();
		
		//-----------------------------------//
		//    render refraction to texture   //
		//-----------------------------------//
		
		BaseOreonContext.getConfig().setRenderRefraction(true);
		
		refraction_fbo.bind();
		renderConfig.clearScreenDeepOcean();
	
		scenegraph.record(refractionRenderList);
		
		refractionRenderList.remove(this.id);
		
		refractionRenderList.getValues().forEach(object ->
		{
			object.render();
		});
		
		// glFinish() important, to prevent conflicts with following compute shaders
		glFinish();
		refraction_fbo.unbind();
		
		//-----------------------------------//
		//     reset rendering settings      //
		//-----------------------------------//
		
		BaseOreonContext.getConfig().setRenderRefraction(false);
		
		glDisable(GL_CLIP_DISTANCE6);
		BaseOreonContext.getConfig().setClipplane(Constants.ZEROPLANE);
	
		glViewport(0,0,tempScreenResolutionX, tempScreenResolutionY);
		BaseOreonContext.getConfig().setFrameWidth(tempScreenResolutionX);
		BaseOreonContext.getConfig().setFrameHeight(tempScreenResolutionY);
		
		GLOreonContext.getResources().getPrimaryFbo().bind();
		
		//-----------------------------------//
		//            render FFT'S           //
		//-----------------------------------//
		fft.render();
		normalmapRenderer.render(fft.getDy());

		ssbo.bindBufferBase(1);
		
		super.render();
		
		// glFinish() important, to prevent conflicts with following compute shaders
		glFinish();
	}
	
	public void initShaderBuffer() {
		
		ssbo = new GLShaderStorageBuffer();
		ByteBuffer byteBuffer = memAlloc(Float.BYTES * 33 + Integer.BYTES * 6);
		byteBuffer.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrix()));
		byteBuffer.putInt(config.getUvScale());
		byteBuffer.putInt(config.getTessellationFactor());
		byteBuffer.putFloat(config.getTessellationSlope());
		byteBuffer.putFloat(config.getTessellationShift());
		byteBuffer.putFloat(config.getDisplacementScale());
		byteBuffer.putInt(config.getHighDetailRange());
		byteBuffer.putFloat(config.getChoppiness());
		byteBuffer.putFloat(config.getKReflection());
		byteBuffer.putFloat(config.getKRefraction());
		byteBuffer.putInt(BaseOreonContext.getConfig().getFrameWidth());
		byteBuffer.putInt(BaseOreonContext.getConfig().getFrameHeight());
		byteBuffer.putInt(config.isDiffuse() ? 1 : 0);
		byteBuffer.putFloat(config.getEmission());
		byteBuffer.putFloat(config.getSpecularFactor());
		byteBuffer.putFloat(config.getSpecularAmplifier());
		byteBuffer.putFloat(config.getReflectionBlendFactor());
		byteBuffer.putFloat(config.getBaseColor().getX());
		byteBuffer.putFloat(config.getBaseColor().getY());
		byteBuffer.putFloat(config.getBaseColor().getZ());
		byteBuffer.putFloat(config.getFresnelFactor());
		byteBuffer.putFloat(config.getCapillarStrength());
		byteBuffer.putFloat(config.getCapillarDownsampling());
		byteBuffer.putFloat(config.getDudvDownsampling());
		byteBuffer.flip();
		ssbo.addData(byteBuffer);
	}

}
