package org.oreon.gl.engine;

import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT2;

import java.nio.IntBuffer;

import org.oreon.core.gl.framebuffer.GLFrameBufferObject;
import org.oreon.core.gl.framebuffer.GLFramebuffer;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.util.BufferUtil;

public class TransparencyFbo extends GLFrameBufferObject {

  public TransparencyFbo(int width, int height) {

    GLTexture albedoAttachment = new TextureImage2D(width, height, ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
    GLTexture alphaAttachment = new TextureImage2D(width, height, ImageFormat.RGBA16FLOAT, SamplerFilter.Nearest);
    GLTexture lightScatteringAttachment = new TextureImage2D(width, height, ImageFormat.RGBA16FLOAT,
        SamplerFilter.Nearest);
    GLTexture depthAttachment = new TextureImage2D(width, height, ImageFormat.DEPTH32FLOAT, SamplerFilter.Nearest);

    attachments.put(Attachment.COLOR, albedoAttachment);
    attachments.put(Attachment.ALPHA, alphaAttachment);
    attachments.put(Attachment.LIGHT_SCATTERING, lightScatteringAttachment);
    attachments.put(Attachment.DEPTH, depthAttachment);

    IntBuffer drawBuffers = BufferUtil.createIntBuffer(3);
    drawBuffers.put(GL_COLOR_ATTACHMENT0);
    drawBuffers.put(GL_COLOR_ATTACHMENT1);
    drawBuffers.put(GL_COLOR_ATTACHMENT2);
    drawBuffers.flip();

    frameBuffer = new GLFramebuffer();
    frameBuffer.bind();
    frameBuffer.createColorTextureAttachment(albedoAttachment.getHandle(), 0);
    frameBuffer.createColorTextureAttachment(alphaAttachment.getHandle(), 1);
    frameBuffer.createColorTextureAttachment(lightScatteringAttachment.getHandle(), 2);
    frameBuffer.createDepthTextureAttachment(depthAttachment.getHandle());
    frameBuffer.setDrawBuffers(drawBuffers);
    frameBuffer.checkStatus();
    frameBuffer.unbind();
  }
}
