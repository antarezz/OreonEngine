package org.oreon.core.math;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.context.ContextHolder;

public class Transform {

  private Vec3f translation;
  private Vec3f rotation;
  private Vec3f scaling;

  private Vec3f localTranslation;
  private Vec3f localRotation;
  private Vec3f localScaling;

  public Transform() {
    setTranslation(new Vec3f(0, 0, 0));
    setRotation(new Vec3f(0, 0, 0));
    setScaling(new Vec3f(1, 1, 1));

    setLocalTranslation(new Vec3f(0, 0, 0));
    setLocalRotation(new Vec3f(0, 0, 0));
    setLocalScaling(new Vec3f(1, 1, 1));
  }

  public Matrix4f getWorldMatrix() {
    final Matrix4f matrix4f = new Matrix4f();
    return matrix4f.MakeTransform(translation, rotation, scaling);
  }

  public Matrix4f getWorldMatrixRTS() {

    Matrix4f translationMatrix = new Matrix4f().Translation(translation);
    Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);
    Matrix4f scalingMatrix = new Matrix4f().Scaling(scaling);

    return rotationMatrix.mul(translationMatrix.mul(scalingMatrix));
  }

  public Matrix4f getWorldMatrixSRT() {

    Matrix4f translationMatrix = new Matrix4f().Translation(translation);
    Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);
    Matrix4f scalingMatrix = new Matrix4f().Scaling(scaling);

    return scalingMatrix.mul(rotationMatrix.mul(translationMatrix));
  }

  public Matrix4f getModelMatrix() {
    Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);

    return rotationMatrix;
  }

  public Matrix4f getModelViewProjectionMatrix() {
    return ContextHolder.getContext().getCamera().getViewProjectionMatrix().mul(getWorldMatrix());
  }

  public Matrix4f getOriginModelViewProjectionMatrix() {
    return ContextHolder.getContext().getCamera().getOriginViewProjectionMatrix().mul(getWorldMatrix());
  }

  public Matrix4f getXzOriginModelViewProjectionMatrix() {
    return ContextHolder.getContext().getCamera().getXzOriginViewProjectionMatrix().mul(getWorldMatrix());
  }

  public Vec3f getTranslation() {
    return translation;
  }

  public void setTranslation(Vec3f translation) {
    this.translation = translation;
  }

  public void setTranslation(float x, float y, float z) {
    this.translation = new Vec3f(x, y, z);
  }

  public Vec3f getRotation() {
    return rotation;
  }

  public void setRotation(Vec3f rotation) {
    this.rotation = rotation;
  }

  public void setRotation(float x, float y, float z) {
    this.rotation = new Vec3f(x, y, z);
  }

  public Vec3f getScaling() {
    return scaling;
  }

  public void setScaling(Vec3f scaling) {
    this.scaling = scaling;
  }

  public void setScaling(float x, float y, float z) {
    this.scaling = new Vec3f(x, y, z);
  }

  public void setScaling(float s) {
    this.scaling = new Vec3f(s, s, s);
  }

  public Vec3f getLocalTranslation() {
    return localTranslation;
  }

  public void setLocalTranslation(Vec3f localTranslation) {
    this.localTranslation = localTranslation;
  }

  public void setLocalTranslation(float x, float y, float z) {
    this.localTranslation = new Vec3f(x, y, z);
  }

  public Vec3f getLocalRotation() {
    return localRotation;
  }

  public void setLocalRotation(Vec3f localRotation) {
    this.localRotation = localRotation;
  }

  public void setLocalRotation(float x, float y, float z) {
    this.localRotation = new Vec3f(x, y, z);
  }

  public Vec3f getLocalScaling() {
    return localScaling;
  }

  public void setLocalScaling(Vec3f localScaling) {
    this.localScaling = localScaling;
  }

  public void setLocalScaling(float x, float y, float z) {
    this.localScaling = new Vec3f(x, y, z);
  }

}
