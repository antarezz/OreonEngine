package org.oreon.core.math;

public class Vec2f {

  private float X;
  private float Y;

  public Vec2f() {
    this.setX(0);
    this.setY(0);
  }

  public Vec2f(float x, float y) {
    this.setX(x);
    this.setY(y);
  }

  public Vec2f(Vec2f v) {
    this.X = v.getX();
    this.Y = v.getY();
  }

  public float length() {
    return (float) Math.sqrt(X * X + Y * Y);
  }

  public float dot(Vec2f r) {
    return X * r.getX() + Y * r.getY();
  }

  public Vec2f normalize() {
    float length = length();

    X /= length;
    Y /= length;

    return this;
  }

  public Vec2f add(Vec2f r) {
    return new Vec2f(this.X + r.getX(), this.Y + r.getY());
  }

  public Vec2f add(float r) {
    return new Vec2f(this.X + r, this.Y + r);
  }

  public Vec2f sub(Vec2f r) {
    return new Vec2f(this.X - r.getX(), this.Y - r.getY());
  }

  public Vec2f sub(float r) {
    return new Vec2f(this.X - r, this.Y - r);
  }

  public Vec2f mul(Vec2f r) {
    return new Vec2f(this.X * r.getX(), this.Y * r.getY());
  }

  public Vec2f mul(float r) {
    return new Vec2f(this.X * r, this.Y * r);
  }

  public Vec2f div(Vec2f r) {
    return new Vec2f(this.X / r.getX(), this.Y / r.getY());
  }

  public Vec2f div(float r) {
    return new Vec2f(this.X / r, this.Y / r);
  }

  public String toString() {
    return "[" + this.X + "," + this.Y + "]";
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


}
