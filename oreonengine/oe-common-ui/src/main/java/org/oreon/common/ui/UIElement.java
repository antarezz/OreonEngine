package org.oreon.common.ui;

import org.oreon.core.math.Matrix4f;
import org.oreon.core.scenegraph.Renderable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class UIElement extends Renderable {

  protected Matrix4f orthographicMatrix;

  public UIElement(int xPos, int yPos, int xScaling, int yScaling) {
    super();
    setOrthographicMatrix(new Matrix4f().Orthographic2D());
    getWorldTransform().setTranslation(xPos, yPos, 0);
    getWorldTransform().setScaling(xScaling, yScaling, 0);
    setOrthographicMatrix(getOrthographicMatrix().mul(getWorldTransform().getWorldMatrix()));
  }

  @Override
  public void update() {
  }

  ;

  public void update(String text) {
  }

  ;
}
