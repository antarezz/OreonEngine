package org.oreon.core.platform;

import org.oreon.core.math.Vec2f;

public interface Input {

  public void create(long windowId);

  public void update();

  public void shutdown();

  public boolean isKeyPushed(int key);

  public boolean isKeyHolding(int key);

  public boolean isKeyReleased(int key);

  public boolean isButtonPushed(int key);

  public boolean isButtonHolding(int key);

  public boolean isButtonReleased(int key);

  public float getScrollOffset();

  public Vec2f getCursorPosition();

  public Vec2f getLockedCursorPosition();
}
