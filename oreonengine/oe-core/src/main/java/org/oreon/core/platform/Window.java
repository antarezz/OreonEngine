package org.oreon.core.platform;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWImage;
import org.oreon.core.util.ResourceLoader;

public abstract class Window {

  private long id;
  private int width;
  private int height;
  private String title;

  public abstract void create();

  public abstract void show();

  public abstract void draw();

  public abstract void shutdown();

  public abstract boolean isCloseRequested();

  public abstract void resize(int x, int y);

  public Window(String title, int width, int height) {

    this.width = width;
    this.height = height;
    this.title = title;
  }

  public void setIcon(String path) {

    ByteBuffer bufferedImage = ResourceLoader.loadImageToByteBuffer(path);

    GLFWImage image = GLFWImage.malloc();

    image.set(32, 32, bufferedImage);

    GLFWImage.Buffer images = GLFWImage.malloc(1);
    images.put(0, image);

    glfwSetWindowIcon(getId(), images);
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public long getId() {
    return id;
  }

  public void setId(long window) {
    this.id = window;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
