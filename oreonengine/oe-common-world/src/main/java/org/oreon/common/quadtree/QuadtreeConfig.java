package org.oreon.common.quadtree;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.oreon.core.image.Image;
import org.oreon.core.model.Material;
import org.oreon.core.scenegraph.NodeComponent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuadtreeConfig extends NodeComponent {

  protected float verticalScaling;
  protected float horizontalScaling;
  protected int bezier;
  protected int reflectionOffset;
  protected float uvScaling;
  protected int tessellationFactor;
  protected float tessellationSlope;
  protected float tessellationShift;
  protected int highDetailRange;
  protected float normalStrength;
  protected float heightStrength;
  protected Image heightmap;
  protected Image normalmap;
  protected Image ambientmap;
  protected Image splatmap;
  protected FloatBuffer heightmapDataBuffer;
  protected int heightmapResolution;
  protected boolean edgeElevation;
  protected List<Material> materials = new ArrayList<>();
  protected int rootChunkCount;
  protected int lodCount;
  protected int[] lod_range = new int[8];
  protected int[] lod_morphing_area = new int[8];
  protected boolean diamond_square;

  public QuadtreeConfig() {

    Properties properties = new Properties();
    try {
      InputStream stream = QuadtreeConfig.class.getClassLoader()
          .getResourceAsStream("terrain-config.properties");
      properties.load(stream);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    verticalScaling = Float.valueOf(properties.getProperty("scaling.y"));
    horizontalScaling = Float.valueOf(properties.getProperty("scaling.xz"));
    rootChunkCount = Integer.valueOf(properties.getProperty("rootchunks.count"));
    tessellationFactor = Integer.valueOf(properties.getProperty("tessellationFactor"));
    tessellationSlope = Float.valueOf(properties.getProperty("tessellationSlope"));
    tessellationShift = Float.valueOf(properties.getProperty("tessellationShift"));
    uvScaling = Float.valueOf(properties.getProperty("scaling.uv"));
    highDetailRange = Integer.valueOf(properties.getProperty("highDetail.range"));
    diamond_square = Integer.valueOf(properties.getProperty("diamond_square")) == 1 ? true : false;
    heightmapResolution = Integer.valueOf(properties.getProperty("heightmap.resolution"));
    edgeElevation = Integer.valueOf(properties.getProperty("edge.elevation")) == 1 ? true : false;
    lodCount = Integer.valueOf(properties.getProperty("lod.count"));

    for (int i = 0; i < lodCount; i++) {

      if (Integer.valueOf(properties.getProperty("lodRanges.lod" + i)) == 0) {
        lod_range[i] = 0;
        lod_morphing_area[i] = 0;
      } else {
        setLodRange(i, Integer.valueOf(properties.getProperty("lodRanges.lod" + i)));
      }
    }
  }

  public void setLodRange(int index, int lod_range) {
    this.lod_range[index] = lod_range;
    lod_morphing_area[index] = lod_range - getMorphingArea4Lod(index + 1);
  }

  private int getMorphingArea4Lod(int lod) {
    return (int) ((horizontalScaling / rootChunkCount) / (Math.pow(2, lod)));
  }

}
