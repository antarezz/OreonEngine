package org.oreon.core.platform;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.oreon.core.math.Vec2f;

public class GLFWInput implements Input {

  private Set<Integer> pushedKeys = new HashSet<>();
  private Set<Integer> keysHolding = new HashSet<>();
  private Set<Integer> releasedKeys = new HashSet<>();

  private Set<Integer> pushedButtons = new HashSet<>();
  private Set<Integer> buttonsHolding = new HashSet<>();
  private Set<Integer> releasedButtons = new HashSet<>();

  private Vec2f cursorPosition;
  private Vec2f lockedCursorPosition;
  private float scrollOffset;

  private boolean pause = false;

  private GLFWKeyCallback keyCallback;

  private GLFWCursorPosCallback cursorPosCallback;

  private GLFWMouseButtonCallback mouseButtonCallback;

  private GLFWScrollCallback scrollCallback;

  private GLFWFramebufferSizeCallback framebufferSizeCallback;

  public GLFWInput() {
    cursorPosition = new Vec2f();
  }

  public void create(long window) {

    glfwSetFramebufferSizeCallback(window, (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
      @Override
      public void invoke(long window, int width, int height) {
        // Todo
      }
    }));

    glfwSetKeyCallback(window, (keyCallback = new GLFWKeyCallback() {

      @Override
      public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
          if (!pushedKeys.contains(key)) {
            pushedKeys.add(key);
            keysHolding.add(key);
          }
        }

        if (action == GLFW_RELEASE) {
          //noinspection UnnecessaryBoxing // Calling remove(Object) not remove(int)
          keysHolding.remove(Integer.valueOf(key));
          releasedKeys.add(key);
        }
      }
    }));

    glfwSetMouseButtonCallback(window, (mouseButtonCallback = new GLFWMouseButtonCallback() {

      @Override
      public void invoke(long window, int button, int action, int mods) {
        if ((button == 2 || button == 0) && action == GLFW_PRESS) {
          lockedCursorPosition = new Vec2f(cursorPosition);
          glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        }

        if ((button == 2 || button == 0) && action == GLFW_RELEASE) {
          glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        if (action == GLFW_PRESS) {
          if (!pushedButtons.contains(button)) {
            pushedButtons.add(button);
            buttonsHolding.add(button);
          }
        }

        if (action == GLFW_RELEASE) {
          releasedButtons.add(button);
          //noinspection UnnecessaryBoxing // Calling remove(Object) not remove(int)
          buttonsHolding.remove(Integer.valueOf(button));
        }
      }
    }));

    glfwSetCursorPosCallback(window, (cursorPosCallback = new GLFWCursorPosCallback() {

      @Override
      public void invoke(long window, double xpos, double ypos) {
        cursorPosition.setX((float) xpos);
        cursorPosition.setY((float) ypos);
      }

    }));

    glfwSetScrollCallback(window, (scrollCallback = new GLFWScrollCallback() {

      @Override
      public void invoke(long window, double xoffset, double yoffset) {
        setScrollOffset((float) yoffset);
      }
    }));
  }

  public void update() {

    setScrollOffset(0);
    pushedKeys.clear();
    releasedKeys.clear();
    pushedButtons.clear();
    releasedButtons.clear();

    glfwPollEvents();
  }

  @Override
  public void shutdown() {
    keyCallback.free();
    cursorPosCallback.free();
    mouseButtonCallback.free();
    scrollCallback.free();
    framebufferSizeCallback.free();
  }

  public boolean isKeyPushed(int key) {
    return pushedKeys.contains(key);
  }

  public boolean isKeyReleased(int key) {
    return releasedKeys.contains(key);
  }

  public boolean isKeyHolding(int key) {
    return keysHolding.contains(key);
  }

  public boolean isButtonPushed(int key) {
    return pushedButtons.contains(key);
  }

  public boolean isButtonReleased(int key) {
    return releasedButtons.contains(key);
  }

  public boolean isButtonHolding(int key) {
    return buttonsHolding.contains(key);
  }

  public boolean isPause() {
    return pause;
  }

  public void setPause(boolean pause) {
    this.pause = pause;
  }

  public Vec2f getCursorPosition() {
    return cursorPosition;
  }

  public void setCursorPosition(Vec2f cursorPosition, long window) {
    this.cursorPosition = cursorPosition;

    glfwSetCursorPos(window,
        cursorPosition.getX(),
        cursorPosition.getY());
  }

  public Vec2f getLockedCursorPosition() {
    return lockedCursorPosition;
  }

  public void setLockedCursorPosition(Vec2f lockedCursorPosition) {
    this.lockedCursorPosition = lockedCursorPosition;
  }

  public Set<Integer> getPushedKeys() {
    return pushedKeys;
  }

  public Set<Integer> getButtonsHolding() {
    return buttonsHolding;
  }

  public float getScrollOffset() {
    return scrollOffset;
  }

  public void setScrollOffset(float scrollOffset) {
    this.scrollOffset = scrollOffset;
  }

  public Set<Integer> getKeysHolding() {
    return keysHolding;
  }

  public Set<Integer> getPushedButtons() {
    return pushedButtons;
  }
}
