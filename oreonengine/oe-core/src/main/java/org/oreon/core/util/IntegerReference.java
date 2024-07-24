package org.oreon.core.util;

public class IntegerReference {

  private int value;

  public IntegerReference() {
  }

  public IntegerReference(int value) {
    setValue(value);
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
