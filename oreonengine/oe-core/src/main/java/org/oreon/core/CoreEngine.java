package org.oreon.core;

import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.platform.Input;
import org.oreon.core.platform.Window;
import org.oreon.core.util.Constants;

public class CoreEngine {

  private static int fps;
  private static float framerate = 1000;
  private static float frameTime = 1.0f / framerate;
  public static float currentFrameTime = 0;
  private boolean isRunning;

  private Window window;
  private Input input;
  private RenderEngine renderEngine;
  private GLFWErrorCallback errorCallback;

  private void init() {
    glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

    renderEngine = ContextHolder.getContext().getRenderEngine();
    window = ContextHolder.getContext().getWindow();
    input = ContextHolder.getContext().getInput();

    input.create(window.getId());
    window.show();
  }

  public void start() {
    init();

    if (isRunning) {
      return;
    }

    run();
  }

  public void run() {

    this.isRunning = true;

    int frames = 0;
    long frameCounter = 0;

    long lastTime = System.nanoTime();
    double unprocessedTime = 0;

    // Rendering Loop
    while (isRunning) {
      boolean render = false;

      long startTime = System.nanoTime();
      long passedTime = startTime - lastTime;
      lastTime = startTime;

      unprocessedTime += passedTime / (double) Constants.NANOSECOND;
      frameCounter += passedTime;

      while (unprocessedTime > frameTime) {
        render = true;
        unprocessedTime -= frameTime;

        if (ContextHolder.getContext().getWindow().isCloseRequested()) {
          stop();
        }

        if (frameCounter >= Constants.NANOSECOND) {
          setFps(frames);
          currentFrameTime = 1.0f / fps;
          frames = 0;
          frameCounter = 0;
        }
      }
      if (render) {
        update();
        render();
        frames++;
      } else {
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
      }
    }

    shutdown();
  }

  private void stop() {
    if (!isRunning) {
      return;
    }

    isRunning = false;
  }

  private void render() {
    renderEngine.render();
    window.draw();
  }

  private void update() {
    input.update();
    renderEngine.update();
  }

  private void shutdown() {
    window.shutdown();
    input.shutdown();
    renderEngine.shutdown();
    errorCallback.free();
    glfwTerminate();
  }

  public static float getFrameTime() {
    return frameTime;
  }

  public static int getFps() {
    return fps;
  }

  public static void setFps(int fps) {
    CoreEngine.fps = fps;
  }

}
