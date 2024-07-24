package org.oreon.core.shadow;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;

public class PssmCamera {

  private Vec3f[] frustumCorners;
  private float zNear;
  private float zFar;

  private Matrix4f m_orthographicViewProjection;

  public PssmCamera(float near, float far) {
    this.zNear = near;
    this.zFar = far;
    frustumCorners = new Vec3f[8];
  }

  public void update(Matrix4f m_View, Vec3f v_Up, Vec3f v_Right) {
    updateFrustumCorners();
    updateOrthoMatrix(m_View, v_Up, v_Right);
  }

  private void updateOrthoMatrix(Matrix4f m_View, Vec3f v_Up, Vec3f v_Right) {

    frustumCorners[0] = m_View.mul(new Vec4f(frustumCorners[0], 1)).xyz();
    frustumCorners[1] = m_View.mul(new Vec4f(frustumCorners[1], 1)).xyz();
    frustumCorners[2] = m_View.mul(new Vec4f(frustumCorners[2], 1)).xyz();
    frustumCorners[3] = m_View.mul(new Vec4f(frustumCorners[3], 1)).xyz();
    frustumCorners[4] = m_View.mul(new Vec4f(frustumCorners[4], 1)).xyz();
    frustumCorners[5] = m_View.mul(new Vec4f(frustumCorners[5], 1)).xyz();
    frustumCorners[6] = m_View.mul(new Vec4f(frustumCorners[6], 1)).xyz();
    frustumCorners[7] = m_View.mul(new Vec4f(frustumCorners[7], 1)).xyz();

    float[] boundaries = getBoundaries(frustumCorners);

    float left = boundaries[0];
    float right = boundaries[1];
    float bottom = boundaries[2];
    float top = boundaries[3];
    float near = boundaries[4];
    float far = boundaries[5];

    this.m_orthographicViewProjection = new Matrix4f().OrthographicProjection(left, right, bottom, top, near, far)
        .mul(m_View);
  }

  public float[] getBoundaries(Vec3f[] frustumCorners) {

    float xMin = 1000000f;
    float xMax = -1000000f;
    float yMin = 1000000f;
    float yMax = -1000000f;
    float zMin = 1000000f;
    float zMax = -1000000f;

    for (Vec3f corner : frustumCorners) {
      if (corner.getX() < xMin) {
        xMin = corner.getX();
      }
      if (corner.getX() > xMax) {
        xMax = corner.getX();
      }
      if (corner.getY() < yMin) {
        yMin = corner.getY();
      }
      if (corner.getY() > yMax) {
        yMax = corner.getY();
      }
      if (corner.getZ() < zMin) {
        zMin = corner.getZ();
      }
      if (corner.getZ() > zMax) {
        zMax = corner.getZ();
      }
    }

    float[] boundaries = {xMin, xMax, yMin, yMax, zMin, zMax};
    return boundaries;
  }

  private void updateFrustumCorners() {

    Vec3f right = ContextHolder.getContext().getCamera().getUp()
        .cross(ContextHolder.getContext().getCamera().getForward());

    float tanFOV = (float) Math.tan(Math.toRadians(ContextHolder.getContext().getCamera().getFovY() / 2));
    float aspectRatio =
        ContextHolder.getContext().getCamera().getWidth() / ContextHolder.getContext().getCamera().getHeight();

    //width and height of near plane
    float heightNear = 2 * tanFOV * zNear;
    float widthNear = heightNear * aspectRatio;

    //width and height of far plane
    float heightFar = 2 * tanFOV * zFar;
    float widthFar = heightFar * aspectRatio;

    //center of planes
    Vec3f centerNear = ContextHolder.getContext().getCamera().getPosition()
        .add(ContextHolder.getContext().getCamera().getForward().mul(zNear));
    Vec3f centerFar = ContextHolder.getContext().getCamera().getPosition()
        .add(ContextHolder.getContext().getCamera().getForward().mul(zFar));

    Vec3f NearTopLeft = centerNear.add(
        (ContextHolder.getContext().getCamera().getUp().mul(heightNear / 2f)).sub((right.mul(widthNear / 2f))));
    Vec3f NearTopRight = centerNear.add(
        (ContextHolder.getContext().getCamera().getUp().mul(heightNear / 2f)).add((right.mul(widthNear / 2f))));
    Vec3f NearBottomLeft = centerNear.sub(
        (ContextHolder.getContext().getCamera().getUp().mul(heightNear / 2f)).sub((right.mul(widthNear / 2f))));
    Vec3f NearBottomRight = centerNear.sub(
        (ContextHolder.getContext().getCamera().getUp().mul(heightNear / 2f)).add((right.mul(widthNear / 2f))));

    Vec3f FarTopLeft = centerFar.add(
        (ContextHolder.getContext().getCamera().getUp().mul(heightFar / 2f)).sub((right.mul(widthFar / 2f))));
    Vec3f FarTopRight = centerFar.add(
        (ContextHolder.getContext().getCamera().getUp().mul(heightFar / 2f)).add((right.mul(widthFar / 2f))));
    Vec3f FarBottomLeft = centerFar.sub(
        (ContextHolder.getContext().getCamera().getUp().mul(heightFar / 2f)).sub((right.mul(widthFar / 2f))));
    Vec3f FarBottomRight = centerFar.sub(
        (ContextHolder.getContext().getCamera().getUp().mul(heightFar / 2f)).add((right.mul(widthFar / 2f))));

    frustumCorners[0] = NearTopLeft;
    frustumCorners[1] = NearTopRight;
    frustumCorners[2] = NearBottomLeft;
    frustumCorners[3] = NearBottomRight;

    frustumCorners[4] = FarTopLeft;
    frustumCorners[5] = FarTopRight;
    frustumCorners[6] = FarBottomLeft;
    frustumCorners[7] = FarBottomRight;
  }

  public Matrix4f getM_orthographicViewProjection() {
    return m_orthographicViewProjection;
  }
}
