package org.oreon.core.light;

import java.util.ArrayList;
import java.util.List;

public class LightHandler {

  private static List<Light> lights = new ArrayList<Light>();

  public static List<Light> getLights() {
    return lights;
  }

  public static void setLights(List<Light> lights) {
    LightHandler.lights = lights;
  }

  public static void doOcclusionQueries() {
    for (Light light : lights) {
      light.occlusionQuery();
    }
  }
}
