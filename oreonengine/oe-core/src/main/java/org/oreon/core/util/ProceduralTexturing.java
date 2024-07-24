package org.oreon.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Mesh;

public class ProceduralTexturing {

  public static void sphere(Mesh mesh) {
    for (int i = 0; i < mesh.getVertices().length; i++) {
      if (mesh.getVertices()[i].getUVCoord().getX() != 0.001f) {
        mesh.getVertices()[i].getUVCoord().setX((float) (0.5
            + (Math.atan2(mesh.getVertices()[i].getPosition().getZ(), mesh.getVertices()[i].getPosition().getX())) / (2
            * Math.PI)));
      }
      mesh.getVertices()[i].getUVCoord()
          .setY((float) (0.5 - (Math.asin(mesh.getVertices()[i].getPosition().getY()) / Math.PI)));
    }
  }

  public static void dome(Mesh mesh) {
    for (int i = 0; i < mesh.getVertices().length; i++) {
      mesh.getVertices()[i].setUVCoord(new Vec2f((mesh.getVertices()[i].getPosition().getX() + 1) * 0.5f,
          (mesh.getVertices()[i].getPosition().getZ() + 1) * 0.5f));
    }
  }

  public static void noiseTexture() {
    int resolution = 512;

    BufferedImage image = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_RGB);
    WritableRaster raster = image.getRaster();
    File output = new File("./" + "Noise512_3" + ".jpg");

    Random rnd = new Random();

    for (int i = 0; i < resolution; i++) {
      for (int j = 0; j < resolution; j++) {
        int noise = ((int) rnd.nextInt(Integer.MAX_VALUE));
        raster.setSample(j, i, 0, noise);
        raster.setSample(j, i, 1, noise);
        raster.setSample(j, i, 2, noise);
      }
    }
    try {
      ImageIO.write(image, "jpg", output);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
