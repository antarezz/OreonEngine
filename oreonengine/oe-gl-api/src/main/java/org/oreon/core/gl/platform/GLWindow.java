package org.oreon.core.gl.platform;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_TRUE;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.WGLEXTSwapControl;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.platform.Window;

public class GLWindow extends Window {

  public GLWindow() {
    super(
        ContextHolder.getContext().getConfig().getDisplayTitle(),
        ContextHolder.getContext().getConfig().getWindowWidth(),
        ContextHolder.getContext().getConfig().getWindowHeight());
  }

  public void create() {
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    setId(glfwCreateWindow(getWidth(), getHeight(), getTitle(), 0, 0));

    if (getId() == 0) {
      throw new RuntimeException("Failed to create window");
    }

    setIcon("textures/logo/oreon_lwjgl_icon32.png");

    glfwMakeContextCurrent(getId());

    glfwSwapInterval(0);

    if (ContextHolder.getContext().getConfig().isGlfwGLVSync()) {
      WGLEXTSwapControl.wglSwapIntervalEXT(1);
      glfwSwapInterval(1);
    }

    GL.createCapabilities();
  }

  public void show() {
    glfwShowWindow(getId());
  }

  public void draw() {
    glfwSwapBuffers(getId());
  }

  public void shutdown() {
    glfwDestroyWindow(getId());
  }

  public boolean isCloseRequested() {
    return glfwWindowShouldClose(getId());
  }

  public void resize(int width, int height) {
    glfwSetWindowSize(getId(), width, height);
    setHeight(height);
    setWidth(width);
    ContextHolder.getContext().getConfig().setWindowWidth(width);
    ContextHolder.getContext().getConfig().setWindowHeight(height);
    // TODO set camera projection
  }
}
