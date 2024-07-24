package org.oreon.core.util;

import org.oreon.core.math.Vec4f;

public class Constants {

  public static final long NANOSECOND = 1000000000l;
  public static final float ZFAR = 10000.0f;
  public static final float ZNEAR = 0.1f;
  public static Vec4f ZEROPLANE = new Vec4f(0, 0, 0, 0);
  public static int CLIPOFFSET;
  public static final int PSSM_SPLITS = 5;
  public static final float[] PSSM_SPLIT_SHEME = {-0.02f, 0.02f,
      -0.04f, 0.04f,
      -0.1f, 0.1f,
      -0.5f, 0.5f,
      -1f, 1f};

  // Global Uniform Block Bindings
  public static final int CameraUniformBlockBinding = 51;
  public static final int DirectionalLightUniformBlockBinding = 52;
  public static final int LightMatricesUniformBlockBinding = 53;

  //example spotlight
	/*PointLight light = new PointLight(getTransform().getTranslation(), new Vec3f(1,1,1), new Vec3f(1.0f,1.0f,0.8f), 1f);
	light.getLocalTransform().getTranslation().setY(-10);
	if (i == 1)
		light.setEnabled(1);
	light.setSpot(1);
	light.setConstantAttenuation(0.01f);
	light.setLinearAttenuation(0.005f);
	light.setQuadraticAttenuation(0.00005f);
	light.setConeDirection(new Vec3f(0,-1,0));
	light.setSpotCosCutoff(0.8f);
	light.setSpotExponent(20);
	drone.addComponent("Light", light);*/
}
