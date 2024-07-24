package org.oreon.core.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.util.Constants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {

  // screen settings
  private int frameWidth;
  private int frameHeight;

  // window settings
  private String displayTitle;
  private int windowWidth;
  private int windowHeight;

  // glfw opengl vsync
  private boolean glfwGLVSync;

  // anitaliasing
  private final int multisampling_sampleCount;
  private boolean fxaaEnabled;

  // shadows settings
  private boolean shadowsEnable;
  private int shadowMapResolution;
  private int shadowsQuality;

  // post processing effects
  private boolean ssaoEnabled;
  private boolean bloomEnabled;
  private boolean depthOfFieldBlurEnabled;
  private boolean motionBlurEnabled;
  private boolean lightScatteringEnabled;
  private boolean lensFlareEnabled;

  // dynamic render settings
  private boolean renderWireframe;
  private boolean renderUnderwater;
  private boolean renderReflection;
  private boolean renderRefraction;
  private Vec4f clipplane;

  // Vulkan Validation
  private boolean vkValidation;

  // Atmosphere parameters
  private float sunRadius;
  private Vec3f sunPosition;
  private Vec3f sunColor;
  private float sunIntensity;
  private float ambient;
  private boolean AtmosphericScatteringEnable;
  private float atmosphereBloomFactor;
  private Vec3f fogColor;
  private float horizonVerticalShift;
  private float horizonReflectionVerticalShift;
  private float sightRange;

  // postprocessing parameters
  private int lightscatteringSampleCount;
  private float lightscatteringDecay;
  private float motionblurSampleCount;
  private int motionblurBlurfactor;
  private int bloomKernels;
  private int bloomSigma;

  public Config() {

    Properties properties = new Properties();
    try {
      InputStream vInputStream = Config.class.getClassLoader().getResourceAsStream("oe-config.properties");
      properties.load(vInputStream);
      vInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    windowWidth = Integer.valueOf(properties.getProperty("window.width"));
    windowHeight = Integer.valueOf(properties.getProperty("window.height"));
    displayTitle = properties.getProperty("display.title");
    frameWidth = Integer.valueOf(properties.getProperty("frame.width"));
    frameHeight = Integer.valueOf(properties.getProperty("frame.height"));
    multisampling_sampleCount = Integer.valueOf(properties.getProperty("multisampling.sample.count"));
    fxaaEnabled = Integer.valueOf(properties.getProperty("fxaa.enable")) == 1 ? true : false;
    shadowsEnable = Integer.valueOf(properties.getProperty("shadows.enable")) == 1 ? true : false;
    shadowMapResolution = Integer.valueOf(properties.getProperty("shadows.map.resolution"));
    shadowsQuality = Integer.valueOf(properties.getProperty("shadows.quality"));
    bloomEnabled = Integer.valueOf(properties.getProperty("bloom.enable")) == 1 ? true : false;
    ssaoEnabled = Integer.valueOf(properties.getProperty("ssao.enable")) == 1 ? true : false;
    motionBlurEnabled = Integer.valueOf(properties.getProperty("motionBlur.enable")) == 1 ? true : false;
    lightScatteringEnabled = Integer.valueOf(properties.getProperty("lightScattering.enable")) == 1 ? true : false;
    depthOfFieldBlurEnabled = Integer.valueOf(properties.getProperty("depthOfFieldBlur.enable")) == 1 ? true : false;
    lensFlareEnabled = Integer.valueOf(properties.getProperty("lensFlare.enable")) == 1 ? true : false;

    if (properties.getProperty("validation.enable") != null) {
      vkValidation = Integer.valueOf(properties.getProperty("validation.enable")) == 1 ? true : false;
    }

    if (properties.getProperty("glfw.vsync") != null) {
      glfwGLVSync = Integer.valueOf(properties.getProperty("glfw.vsync")) == 1 ? true : false;
    }

    renderWireframe = false;
    renderUnderwater = false;
    renderReflection = false;
    renderRefraction = false;
    clipplane = Constants.ZEROPLANE;

    try {
      InputStream vInputStream = Config.class.getClassLoader().getResourceAsStream("atmosphere-config.properties");
      if (vInputStream != null) {
        properties.load(vInputStream);
        vInputStream.close();

        sunRadius = Float.valueOf(properties.getProperty("sun.radius"));
        sunPosition = new Vec3f(
            Float.valueOf(properties.getProperty("sun.position.x")),
            Float.valueOf(properties.getProperty("sun.position.y")),
            Float.valueOf(properties.getProperty("sun.position.z"))).normalize();
        sunColor = new Vec3f(
            Float.valueOf(properties.getProperty("sun.color.r")),
            Float.valueOf(properties.getProperty("sun.color.g")),
            Float.valueOf(properties.getProperty("sun.color.b")));
        sunIntensity = Float.valueOf(properties.getProperty("sun.intensity"));
        ambient = Float.valueOf(properties.getProperty("ambient"));
        AtmosphericScatteringEnable =
            Integer.valueOf(properties.getProperty("atmosphere.scattering.enable")) == 1 ? true : false;
        horizonVerticalShift = Float.valueOf(properties.getProperty("horizon.verticalShift"));
        horizonReflectionVerticalShift = Float.valueOf(properties.getProperty("horizon.reflection.verticalShift"));
        atmosphereBloomFactor = Float.valueOf(properties.getProperty("atmosphere.bloom.factor"));
        sightRange = Float.valueOf(properties.getProperty("sightRange"));
        fogColor = new Vec3f(Float.valueOf(properties.getProperty("fog.color.r")),
            Float.valueOf(properties.getProperty("fog.color.g")),
            Float.valueOf(properties.getProperty("fog.color.b")));
        float fogBrightness = Float.valueOf(properties.getProperty("fog.brightness"));

        fogColor = fogColor.mul(fogBrightness);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      InputStream vInputStream = Config.class.getClassLoader().getResourceAsStream("postprocessing-config.properties");
      if (vInputStream != null) {
        properties.load(vInputStream);
        vInputStream.close();

        lightscatteringSampleCount = Integer.valueOf(properties.getProperty("lightscattering.samples.count"));
        lightscatteringDecay = Float.valueOf(properties.getProperty("lightscattering.decay"));
        motionblurBlurfactor = Integer.valueOf(properties.getProperty("motionblur.blurfactor"));
        motionblurSampleCount = Integer.valueOf(properties.getProperty("motionblur.samples.count"));
        bloomKernels = Integer.valueOf(properties.getProperty("bloom.kernels"));
        bloomSigma = Integer.valueOf(properties.getProperty("bloom.sigma"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getFrameWidth() {
    return frameWidth;
  }

  public int getFrameHeight() {
    return frameHeight;
  }
}
