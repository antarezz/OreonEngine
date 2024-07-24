package org.oreon.core.math;


public class Vec3f {

  private float X;
  private float Y;
  private float Z;

  public Vec3f() {
    this.setX(0);
    this.setY(0);
    this.setZ(0);
  }

  public Vec3f(float x, float y, float z) {
    this.setX(x);
    this.setY(y);
    this.setZ(z);
  }

  public Vec3f(float x) {
    this.setX(x);
    this.setY(x);
    this.setZ(x);
  }

  public Vec3f(Vec3f v) {
    this.X = v.getX();
    this.Y = v.getY();
    this.Z = v.getZ();
  }

  public float length() {
    return (float) Math.sqrt(X * X + Y * Y + Z * Z);
  }

  public float dot(Vec3f r) {
    return X * r.getX() + Y * r.getY() + Z * r.getZ();
  }

  public Vec3f cross(Vec3f r) {
    float x = Y * r.getZ() - Z * r.getY();
    float y = Z * r.getX() - X * r.getZ();
    float z = X * r.getY() - Y * r.getX();

    return new Vec3f(x, y, z);
  }

  public Vec3f normalize() {
    float length = this.length();

    X /= length;
    Y /= length;
    Z /= length;

    return this;
  }

  public Vec3f rotate(float angle, Vec3f axis) {
    float sinHalfAngle = (float) Math.sin(Math.toRadians(angle / 2));
    float cosHalfAngle = (float) Math.cos(Math.toRadians(angle / 2));

    float rX = axis.getX() * sinHalfAngle;
    float rY = axis.getY() * sinHalfAngle;
    float rZ = axis.getZ() * sinHalfAngle;
    float rW = cosHalfAngle;

    Vec4f rotation = new Vec4f(rX, rY, rZ, rW);
    Vec4f conjugate = rotation.conjugate();

    Vec4f w = rotation.mul(this).mul(conjugate);

    X = w.getX();
    Y = w.getY();
    Z = w.getZ();

    return this;
  }

  public Vec3f add(Vec3f r) {
    return new Vec3f(this.X + r.getX(), this.Y + r.getY(), this.Z + r.getZ());
  }

  public Vec3f add(float r) {
    return new Vec3f(this.X + r, this.Y + r, this.Z + r);
  }

  public Vec3f sub(Vec3f r) {
    return new Vec3f(this.X - r.getX(), this.Y - r.getY(), this.Z - r.getZ());
  }

  public Vec3f sub(float r) {
    return new Vec3f(this.X - r, this.Y - r, this.Z - r);
  }

  public Vec3f mul(Vec3f r) {
    return new Vec3f(this.X * r.getX(), this.Y * r.getY(), this.Z * r.getZ());
  }

  public Vec3f mul(float x, float y, float z) {
    return new Vec3f(this.X * x, this.Y * y, this.Z * z);
  }

  public Vec3f mul(float r) {
    return new Vec3f(this.X * r, this.Y * r, this.Z * r);
  }

  public Vec3f div(Vec3f r) {
    return new Vec3f(this.X / r.getX(), this.Y / r.getY(), this.getZ() / r.getZ());
  }

  public Vec3f div(float r) {
    return new Vec3f(this.X / r, this.Y / r, this.Z / r);
  }

  public Vec3f abs() {
    return new Vec3f(Math.abs(X), Math.abs(Y), Math.abs(Z));
  }

  public boolean equals(Vec3f v) {
    if (X == v.getX() && Y == v.getY() && Z == v.getZ()) {
      return true;
    } else {
      return false;
    }
  }

  public String toString() {
    return "[" + this.X + "," + this.Y + "," + this.Z + "]";
  }

  public float getX() {
    return X;
  }

  public void setX(float x) {
    X = x;
  }

  public float getY() {
    return Y;
  }

  public void setY(float y) {
    Y = y;
  }

  public float getZ() {
    return Z;
  }

  public void setZ(float z) {
    Z = z;
  }


}
