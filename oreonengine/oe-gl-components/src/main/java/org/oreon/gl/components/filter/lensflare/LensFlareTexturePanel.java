package org.oreon.gl.components.filter.lensflare;

import org.oreon.core.gl.surface.FullScreenQuad;
import org.oreon.core.gl.wrapper.parameter.AdditiveBlending;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Transform;

public class LensFlareTexturePanel extends FullScreenQuad {

  private float spacing;
  private float transparency;
  private float brightness;
  private Transform orthoTransform;
  private Matrix4f orthographicMatrix;

  public LensFlareTexturePanel() {

    super();

    orthographicMatrix = new Matrix4f().Orthographic2D();
    orthoTransform = new Transform();

    setConfig(new AdditiveBlending());
    setShader(LensFlareShader.getInstance());
  }

  public void render() {

    getConfig().enable();
    getShader().bind();
    getShader().updateUniforms(getOrthographicMatrix());
    getShader().updateUniforms(getTexture(), transparency * brightness);
    getVao().draw();
    getConfig().disable();
  }

  public float getSpacing() {
    return spacing;
  }

  public void setSpacing(float spacing) {
    this.spacing = spacing;
  }

  public float getTransparency() {
    return transparency;
  }

  public void setTransparency(float transparency) {
    this.transparency = transparency;
  }

  public Transform getOrthoTransform() {
    return orthoTransform;
  }

  public void setOrthoTransform(Transform orthoTransform) {
    this.orthoTransform = orthoTransform;
  }

  public Matrix4f getOrthographicMatrix() {
    return orthographicMatrix;
  }

  public void setOrthographicMatrix(Matrix4f orthographicMatrix) {
    this.orthographicMatrix = orthographicMatrix;
  }

  public float getBrightness() {
    return brightness;
  }

  public void setBrightness(float brightness) {
    this.brightness = brightness;
  }
}
