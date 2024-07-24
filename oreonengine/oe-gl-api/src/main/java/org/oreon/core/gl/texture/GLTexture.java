package org.oreon.core.gl.texture;

import static org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameteriEXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureMirrorClamp.GL_MIRROR_CLAMP_TO_EDGE_EXT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetFloat;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL32.glTexImage2DMultisample;
import static org.lwjgl.opengl.GL42.glTexStorage2D;
import static org.lwjgl.opengl.GL42.glTexStorage3D;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL;
import org.oreon.core.image.Image;
import org.oreon.core.image.ImageMetaData;

import lombok.Getter;

@Getter
public class GLTexture extends Image {

  private int handle;
  private int target;

  public GLTexture(int target, int width, int height) {

    generate();
    this.target = target;
    metaData = new ImageMetaData();
    metaData.setWidth(width);
    metaData.setHeight(height);
  }

  public GLTexture(String file) {

    generate();
    loadFromFile(file);
  }

  public void loadFromFile(String file) {

    target = GL_TEXTURE_2D;
    metaData = GLImageLoader.loadImage(file, handle);
  }

  public void bind() {

    glBindTexture(target, handle);
  }

  public void unbind() {

    glBindTexture(target, 0);
  }

  public void generate() {

    handle = glGenTextures();
  }

  public void delete() {

    glDeleteTextures(handle);
  }


  public void nearestFilter() {

    glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
  }

  public void bilinearFilter() {

    glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  }

  public void trilinearFilter() {

    glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glGenerateMipmap(target);
    glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
  }

  public void anisotropicFilter() {

    if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
      float maxfilterLevel = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
      glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxfilterLevel);
    } else {
      System.err.println("anisotropic not supported");
    }
  }

  public void clampToEdge() {

    glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
  }

  public void clampToBorder() {

    glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
  }

  public void repeat() {

    glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_REPEAT);
  }

  public void mirrorRepeat() {

    glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
    glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
  }

  public void mirrorRepeatEXT() {

    if (GL.getCapabilities().GL_EXT_texture_mirror_clamp) {
      glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_EDGE_EXT);
      glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_MIRROR_CLAMP_TO_EDGE_EXT);
    } else {
      System.err.println("texture mirror not supported");
    }
  }

  public void clampToEdgeDirectAccessEXT() {

    glTextureParameteriEXT(handle, target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTextureParameteriEXT(handle, target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
  }

  public void mirrorRepeatDirectAccessEXT() {

    glTextureParameteriEXT(handle, target, GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_EDGE_EXT);
    glTextureParameteriEXT(handle, target, GL_TEXTURE_WRAP_T, GL_MIRROR_CLAMP_TO_EDGE_EXT);
  }

  public void activeTextureUnit(int unit) {

    glActiveTexture(GL_TEXTURE0 + unit);
  }

  public void allocateImage2D(int internalFormat, int format, int type, ByteBuffer data) {

    glTexImage2D(target, 0, internalFormat, metaData.getWidth(), metaData.getHeight(), 0, format, type, data);
  }

  public void allocateImage2D(int internalFormat, int format, int type) {

    glTexImage2D(target, 0, internalFormat, metaData.getWidth(), metaData.getHeight(), 0, format, type,
        (ByteBuffer) null);
  }

  public void allocateImage2DMultisample(int samples, int internalFormat) {

    glTexImage2DMultisample(target, samples, internalFormat, metaData.getWidth(), metaData.getHeight(), true);
  }

  public void allocateStorage2D(int levels, int internalFormat) {

    glTexStorage2D(target, levels, internalFormat, metaData.getWidth(), metaData.getHeight());
  }

  public void allocateStorage3D(int levels, int layers, int internalFormat) {

    glTexStorage3D(target, levels, internalFormat, metaData.getWidth(), metaData.getHeight(), layers);
  }

}
