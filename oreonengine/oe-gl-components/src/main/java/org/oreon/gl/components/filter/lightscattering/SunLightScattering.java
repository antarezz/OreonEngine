package org.oreon.gl.components.filter.lightscattering;

import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.image.Image.TextureWrapMode;

import lombok.Getter;

public class SunLightScattering {

	@Getter
	private GLTexture sunLightScatteringSceneTexture;
	@Getter
	private GLTexture sunLightScatteringTexture;
	private SunLightScatteringShader lightScatteringShader;
	private SunLightScatteringAdditiveBlendShader additiveBlendShader;
	
	public SunLightScattering() {
		
		lightScatteringShader = SunLightScatteringShader.getInstance();
		additiveBlendShader = SunLightScatteringAdditiveBlendShader.getInstance();
		
		sunLightScatteringTexture = new TextureImage2D(BaseOreonContext.getConfig().getFrameWidth(),
				BaseOreonContext.getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT,
				SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);
		
		sunLightScatteringSceneTexture = new TextureImage2D(BaseOreonContext.getConfig().getFrameWidth(),
						BaseOreonContext.getConfig().getFrameHeight(), ImageFormat.RGBA16FLOAT,
						SamplerFilter.Bilinear, TextureWrapMode.ClampToEdge);
	}
	
	public void render(GLTexture sceneSampler, GLTexture lightScatteringMask) {
		
		glFinish();
		lightScatteringShader.bind();
		glBindImageTexture(0, lightScatteringMask.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
		glBindImageTexture(1, sunLightScatteringTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
		lightScatteringShader.updateUniforms(BaseOreonContext.getConfig().getFrameWidth(),
											 BaseOreonContext.getConfig().getFrameHeight(),
										     BaseOreonContext.getCamera().getOriginViewProjectionMatrix());
		glDispatchCompute(BaseOreonContext.getConfig().getFrameWidth()/8, BaseOreonContext.getConfig().getFrameHeight()/8, 1);
		glFinish();
		
		additiveBlendShader.bind();
		glBindImageTexture(0, sunLightScatteringTexture.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
		glBindImageTexture(1, sceneSampler.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA16F);
		glBindImageTexture(2, sunLightScatteringSceneTexture.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
		glDispatchCompute(BaseOreonContext.getConfig().getFrameWidth()/8, BaseOreonContext.getConfig().getFrameHeight()/8, 1);
		glFinish();
	}
	
}
