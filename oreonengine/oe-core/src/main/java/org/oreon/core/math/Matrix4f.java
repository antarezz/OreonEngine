package org.oreon.core.math;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.context.ContextHolder;

public class Matrix4f {

  private float[][] m;

  public Matrix4f() {
    setM(new float[4][4]);
  }

  public Matrix4f Zero() {
    m[0][0] = 0;
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = 0;
    m[1][0] = 0;
    m[1][1] = 0;
    m[1][2] = 0;
    m[1][3] = 0;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 0;
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 0;

    return this;
  }

  public Matrix4f Identity() {
    m[0][0] = 1;
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = 0;
    m[1][0] = 0;
    m[1][1] = 1;
    m[1][2] = 0;
    m[1][3] = 0;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 1;
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f Orthographic2D(int width, int height) {
    m[0][0] = 2f / (float) width;
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = -1;
    m[1][0] = 0;
    m[1][1] = 2f / (float) height;
    m[1][2] = 0;
    m[1][3] = -1;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 1;
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f Orthographic2D() {
    //Z-Value 1: depth of orthographic OOB between 0 and -1

    m[0][0] = 2f / (float) ContextHolder.getContext().getWindow().getWidth();
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = -1;
    m[1][0] = 0;
    m[1][1] = 2f / (float) ContextHolder.getContext().getWindow().getHeight();
    m[1][2] = 0;
    m[1][3] = -1;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 1;
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f Translation(Vec3f translation) {
    m[0][0] = 1;
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = translation.getX();
    m[1][0] = 0;
    m[1][1] = 1;
    m[1][2] = 0;
    m[1][3] = translation.getY();
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 1;
    m[2][3] = translation.getZ();
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f Rotation(Vec3f rotation) {
    float x = (float) Math.toRadians(rotation.getX());
    float y = (float) Math.toRadians(rotation.getY());
    float z = (float) Math.toRadians(rotation.getZ());

    float sinX = (float) Math.sin(x);
    float sinY = (float) Math.sin(y);
    float sinZ = (float) Math.sin(z);

    float cosX = (float) Math.cos(x);
    float cosY = (float) Math.cos(y);
    float cosZ = (float) Math.cos(z);

    final float sinXsinY = sinX * sinY;
    final float cosXsinY = cosX * sinY;

    m[0][0] = cosY * cosZ;
    m[0][1] = cosY * sinZ;
    m[0][2] = -sinY;
    m[0][3] = 0f;

    m[1][0] = sinXsinY * cosZ - cosX * sinZ;
    m[1][1] = sinXsinY * sinZ + cosX * cosZ;
    m[1][2] = sinX * cosY;
    m[1][3] = 0f;

    m[2][0] = cosXsinY * cosZ + sinX * sinZ;
    m[2][1] = cosXsinY * sinZ - sinX * cosZ;
    m[2][2] = cosX * cosY;
    m[2][3] = 0f;

    m[3][0] = 0f;
    m[3][1] = 0f;
    m[3][2] = 0f;
    m[3][3] = 1f;

    return this;
  }

  public Matrix4f Scaling(Vec3f scaling) {
    m[0][0] = scaling.getX();
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = 0;
    m[1][0] = 0;
    m[1][1] = scaling.getY();
    m[1][2] = 0;
    m[1][3] = 0;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = scaling.getZ();
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f MakeTransform(Vec3f translation, Vec3f rotation, Vec3f scaling) {
    // Convert euler angles to a quaternion
    float cr = (float) Math.cos(rotation.getX() * 0.5);
    float sr = (float) Math.sin(rotation.getX() * 0.5);
    float cp = (float) Math.cos(rotation.getY() * 0.5);
    float sp = (float) Math.sin(rotation.getY() * 0.5);
    float cy = (float) Math.cos(rotation.getZ() * 0.5);
    float sy = (float) Math.sin(rotation.getZ() * 0.5);

    float w = cy * cr * cp + sy * sr * sp;
    float x = cy * sr * cp - sy * cr * sp;
    float y = cy * cr * sp + sy * sr * cp;
    float z = sy * cr * cp - cy * sr * sp;

    // Cache some data for further computations
    float x2 = x + x;
    float y2 = y + y;
    float z2 = z + z;

    float xx = x * x2, xy = x * y2, xz = x * z2;
    float yy = y * y2, yz = y * z2, zz = z * z2;
    float wx = w * x2, wy = w * y2, wz = w * z2;

    float scalingX = scaling.getX();
    float scalingY = scaling.getY();
    float scalingZ = scaling.getZ();

    // Apply rotation and scale simultaneously, simply adding the translation.
    m[0][0] = (1f - (yy + zz)) * scalingX;
    m[0][1] = (xy + wz) * scalingX;
    m[0][2] = (xz - wy) * scalingX;
    m[0][3] = translation.getX();

    m[1][0] = (xy - wz) * scalingY;
    m[1][1] = (1f - (xx + zz)) * scalingY;
    m[1][2] = (yz + wx) * scalingY;
    m[1][3] = translation.getY();

    m[2][0] = (xz + wy) * scalingZ;
    m[2][1] = (yz - wx) * scalingZ;
    m[2][2] = (1f - (xx + yy)) * scalingZ;
    m[2][3] = translation.getZ();

    m[3][0] = 0f;
    m[3][1] = 0f;
    m[3][2] = 0f;
    m[3][3] = 1f;

    return this;
  }

  public Matrix4f OrthographicProjection(float l, float r, float b, float t, float n, float f) {

    m[0][0] = 2.0f / (r - l);
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = -(r + l) / (r - l);
    m[1][0] = 0;
    m[1][1] = 2.0f / (t - b);
    m[1][2] = 0;
    m[1][3] = -(t + b) / (t - b);
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = 2.0f / (f - n);
    m[2][3] = -(f + n) / (f - n);
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f PerspectiveProjection(float fovY, float width, float height, float zNear, float zFar) {
    float tanFOV = (float) Math.tan(Math.toRadians(fovY / 2));
    float aspectRatio = width / height;

    m[0][0] = 1 / (tanFOV * aspectRatio);
    m[0][1] = 0;
    m[0][2] = 0;
    m[0][3] = 0;
    m[1][0] = 0;
    m[1][1] = 1 / tanFOV;
    m[1][2] = 0;
    m[1][3] = 0;
    m[2][0] = 0;
    m[2][1] = 0;
    m[2][2] = zFar / (zFar - zNear);
    m[2][3] = zFar * zNear / (zFar - zNear);
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 1;
    m[3][3] = 1;

    return this;
  }

  public Matrix4f View(Vec3f forward, Vec3f up) {
    Vec3f f = forward;
    Vec3f u = up;
    Vec3f r = u.cross(f);

    m[0][0] = r.getX();
    m[0][1] = r.getY();
    m[0][2] = r.getZ();
    m[0][3] = 0;
    m[1][0] = u.getX();
    m[1][1] = u.getY();
    m[1][2] = u.getZ();
    m[1][3] = 0;
    m[2][0] = f.getX();
    m[2][1] = f.getY();
    m[2][2] = f.getZ();
    m[2][3] = 0;
    m[3][0] = 0;
    m[3][1] = 0;
    m[3][2] = 0;
    m[3][3] = 1;

    return this;
  }


  public Matrix4f mul(Matrix4f r) {

    Matrix4f res = new Matrix4f();

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        res.set(i, j, m[i][0] * r.get(0, j) +
            m[i][1] * r.get(1, j) +
            m[i][2] * r.get(2, j) +
            m[i][3] * r.get(3, j));
      }
    }

    return res;
  }

  public Vec4f mul(Vec4f v) {
    Vec4f res = new Vec4f(0, 0, 0, 0);

    res.setX(m[0][0] * v.getX() + m[0][1] * v.getY() + m[0][2] * v.getZ() + m[0][3] * v.getW());
    res.setY(m[1][0] * v.getX() + m[1][1] * v.getY() + m[1][2] * v.getZ() + m[1][3] * v.getW());
    res.setZ(m[2][0] * v.getX() + m[2][1] * v.getY() + m[2][2] * v.getZ() + m[2][3] * v.getW());
    res.setW(m[3][0] * v.getX() + m[3][1] * v.getY() + m[3][2] * v.getZ() + m[3][3] * v.getW());

    return res;
  }

  public Matrix4f transpose() {
    Matrix4f result = new Matrix4f();

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        result.set(i, j, get(j, i));
      }
    }
    return result;
  }

  public Matrix4f invert() {
    float s0 = get(0, 0) * get(1, 1) - get(1, 0) * get(0, 1);
    float s1 = get(0, 0) * get(1, 2) - get(1, 0) * get(0, 2);
    float s2 = get(0, 0) * get(1, 3) - get(1, 0) * get(0, 3);
    float s3 = get(0, 1) * get(1, 2) - get(1, 1) * get(0, 2);
    float s4 = get(0, 1) * get(1, 3) - get(1, 1) * get(0, 3);
    float s5 = get(0, 2) * get(1, 3) - get(1, 2) * get(0, 3);

    float c5 = get(2, 2) * get(3, 3) - get(3, 2) * get(2, 3);
    float c4 = get(2, 1) * get(3, 3) - get(3, 1) * get(2, 3);
    float c3 = get(2, 1) * get(3, 2) - get(3, 1) * get(2, 2);
    float c2 = get(2, 0) * get(3, 3) - get(3, 0) * get(2, 3);
    float c1 = get(2, 0) * get(3, 2) - get(3, 0) * get(2, 2);
    float c0 = get(2, 0) * get(3, 1) - get(3, 0) * get(2, 1);

    float div = (s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0);
		if (div == 0) {
			System.err.println("not invertible");
		}

    float invdet = 1.0f / div;

    Matrix4f invM = new Matrix4f();

    invM.set(0, 0, (get(1, 1) * c5 - get(1, 2) * c4 + get(1, 3) * c3) * invdet);
    invM.set(0, 1, (-get(0, 1) * c5 + get(0, 2) * c4 - get(0, 3) * c3) * invdet);
    invM.set(0, 2, (get(3, 1) * s5 - get(3, 2) * s4 + get(3, 3) * s3) * invdet);
    invM.set(0, 3, (-get(2, 1) * s5 + get(2, 2) * s4 - get(2, 3) * s3) * invdet);

    invM.set(1, 0, (-get(1, 0) * c5 + get(1, 2) * c2 - get(1, 3) * c1) * invdet);
    invM.set(1, 1, (get(0, 0) * c5 - get(0, 2) * c2 + get(0, 3) * c1) * invdet);
    invM.set(1, 2, (-get(3, 0) * s5 + get(3, 2) * s2 - get(3, 3) * s1) * invdet);
    invM.set(1, 3, (get(2, 0) * s5 - get(2, 2) * s2 + get(2, 3) * s1) * invdet);

    invM.set(2, 0, (get(1, 0) * c4 - get(1, 1) * c2 + get(1, 3) * c0) * invdet);
    invM.set(2, 1, (-get(0, 0) * c4 + get(0, 1) * c2 - get(0, 3) * c0) * invdet);
    invM.set(2, 2, (get(3, 0) * s4 - get(3, 1) * s2 + get(3, 3) * s0) * invdet);
    invM.set(2, 3, (-get(2, 0) * s4 + get(2, 1) * s2 - get(2, 3) * s0) * invdet);

    invM.set(3, 0, (-get(1, 0) * c3 + get(1, 1) * c1 - get(1, 2) * c0) * invdet);
    invM.set(3, 1, (get(0, 0) * c3 - get(0, 1) * c1 + get(0, 2) * c0) * invdet);
    invM.set(3, 2, (-get(3, 0) * s3 + get(3, 1) * s1 - get(3, 2) * s0) * invdet);
    invM.set(3, 3, (get(2, 0) * s3 - get(2, 1) * s1 + get(2, 2) * s0) * invdet);

    return invM;
  }

  public Vec3f getTranslation() {
    return new Vec3f(m[0][3], m[1][3], m[2][3]);
  }

  public boolean equals(Matrix4f m) {
		if (this.m[0][0] == m.getM()[0][0] && this.m[0][1] == m.getM()[0][1] &&
				this.m[0][2] == m.getM()[0][2] && this.m[0][3] == m.getM()[0][3] &&
				this.m[1][0] == m.getM()[1][0] && this.m[1][1] == m.getM()[1][1] &&
				this.m[1][2] == m.getM()[1][2] && this.m[1][3] == m.getM()[1][3] &&
				this.m[2][0] == m.getM()[2][0] && this.m[2][1] == m.getM()[2][1] &&
				this.m[2][2] == m.getM()[2][2] && this.m[2][3] == m.getM()[2][3] &&
				this.m[3][0] == m.getM()[3][0] && this.m[3][1] == m.getM()[3][1] &&
				this.m[3][2] == m.getM()[3][2] && this.m[3][3] == m.getM()[3][3]) {
			return true;
		} else {
			return false;
		}
  }

  public void set(int x, int y, float value) {
    this.m[x][y] = value;
  }

  public float get(int x, int y) {
    return this.m[x][y];
  }

  public float[][] getM() {
    return m;
  }

  public void setM(float[][] m) {
    this.m = m;
  }

  public String toString() {

    return "|" + m[0][0] + " " + m[0][1] + " " + m[0][2] + " " + m[0][3] + "|\n" +
        "|" + m[1][0] + " " + m[1][1] + " " + m[1][2] + " " + m[1][3] + "|\n" +
        "|" + m[2][0] + " " + m[2][1] + " " + m[2][2] + " " + m[2][3] + "|\n" +
        "|" + m[3][0] + " " + m[3][1] + " " + m[3][2] + " " + m[3][3] + "|";
  }
}
