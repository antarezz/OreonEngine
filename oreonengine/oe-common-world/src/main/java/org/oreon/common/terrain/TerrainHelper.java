package org.oreon.common.terrain;

import org.oreon.common.quadtree.QuadtreeConfig;
import org.oreon.core.math.Vec2f;

public class TerrainHelper {

  public static float getTerrainHeight(QuadtreeConfig config,
      float x, float z) {

    float h = 0;

    Vec2f pos = new Vec2f();
    pos.setX(x);
    pos.setY(z);
    pos = pos.add(config.getHorizontalScaling() / 2f);
    pos = pos.div(config.getHorizontalScaling());
    Vec2f floor = new Vec2f((int) Math.floor(pos.getX()), (int) Math.floor(pos.getY()));
    pos = pos.sub(floor);
    pos = pos.mul(config.getHeightmap().getMetaData().getWidth());
    int x0 = (int) Math.floor(pos.getX());
    int x1 = x0 + 1;
    int z0 = (int) Math.floor(pos.getY());
    int z1 = z0 + 1;

    float h0 = config.getHeightmapDataBuffer().get(config.getHeightmap().getMetaData().getWidth() * z0 + x0);
    float h1 = config.getHeightmapDataBuffer().get(config.getHeightmap().getMetaData().getWidth() * z0 + x1);
    float h2 = config.getHeightmapDataBuffer().get(config.getHeightmap().getMetaData().getWidth() * z1 + x0);
    float h3 = config.getHeightmapDataBuffer().get(config.getHeightmap().getMetaData().getWidth() * z1 + x1);

    float percentU = pos.getX() - x0;
    float percentV = pos.getY() - z0;

    float dU, dV;
    if (percentU > percentV) {   // bottom triangle
      dU = h1 - h0;
      dV = h3 - h1;
    } else {   // top triangle
      dU = h3 - h2;
      dV = h2 - h0;
    }

    h = h0 + (dU * percentU) + (dV * percentV);
    h *= config.getVerticalScaling();

    return h;
  }
}
