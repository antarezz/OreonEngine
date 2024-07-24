package org.oreon.gl.components.terrain;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_GREEN;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.oreon.common.quadtree.QuadtreeConfig;
import org.oreon.core.gl.pipeline.GLShaderProgram;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.gl.wrapper.texture.TextureStorage2D;
import org.oreon.core.image.Image.ImageFormat;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Material;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.ResourceLoader;
import org.oreon.gl.components.fft.FFT;
import org.oreon.gl.components.util.NormalRenderer;

import lombok.Getter;

public class GLTerrainConfig extends QuadtreeConfig {

  public GLTerrainConfig() {

    super();

    Properties properties = new Properties();
    try {
      InputStream stream = GLTerrainConfig.class.getClassLoader().getResourceAsStream("terrain-config.properties");
      properties.load(stream);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (!properties.getProperty("heightmap").equals("0")) {
      GLTexture vHeightmap = new GLTexture(properties.getProperty("heightmap"));
      vHeightmap.bind();
      vHeightmap.bilinearFilter();
      setHeightmap(vHeightmap);

      heightStrength = Float.valueOf(properties.getProperty("heightmap.strength"));

      NormalRenderer normalRenderer = new NormalRenderer(getHeightmap().getMetaData().getWidth());
      normalRenderer.setStrength(normalStrength);
      normalRenderer.render(vHeightmap);
      setNormalmap(normalRenderer.getNormalmap());
      createHeightmapDataBuffer();
    }

    for (int i = 0; i < Integer.valueOf(properties.getProperty("materials.count")); i++) {

      getMaterials().add(new Material());

      GLTexture diffusemap = new TextureImage2D(properties.getProperty("materials.material" + i + "_DIF"),
          SamplerFilter.Trilinear);
      getMaterials().get(getMaterials().size() - 1).setDiffusemap(diffusemap);

      GLTexture normalmap = new TextureImage2D(properties.getProperty("materials.material" + i + "_NRM"),
          SamplerFilter.Trilinear);
      getMaterials().get(getMaterials().size() - 1).setNormalmap(normalmap);

      GLTexture heightmap = new TextureImage2D(properties.getProperty("materials.material" + i + "_DISP"),
          SamplerFilter.Trilinear);
      getMaterials().get(getMaterials().size() - 1).setHeightmap(heightmap);

      getMaterials().get(getMaterials().size() - 1)
          .setHeightScaling(Float.valueOf(properties.getProperty("materials.material" + i + "_heightScaling")));
      getMaterials().get(getMaterials().size() - 1)
          .setHorizontalScaling(Float.valueOf(properties.getProperty("materials.material" + i + "_horizontalScaling")));
    }

    int fractalCount = Integer.valueOf(properties.getProperty("fractals.count"));
    int fractalMapResolution = Integer.valueOf(properties.getProperty("fractals.resolution"));

    List<FractalMap> fractals = new ArrayList<FractalMap>();

    for (int i = 0; i < fractalCount; i++) {

      int L = Integer.valueOf(properties.getProperty("fractal" + i + ".L"));
      float amplitude = Float.valueOf(properties.getProperty("fractal" + i + ".amplitude"));
      float capillar = Float.valueOf(properties.getProperty("fractal" + i + ".capillar"));
      ;
      int scaling = Integer.valueOf(properties.getProperty("fractal" + i + ".scaling"));
      float heightStrength = Float.valueOf(properties.getProperty("fractal" + i + ".heightStrength"));
      float horizontalStrength = Float.valueOf(properties.getProperty("fractal" + i + ".horizontalStrength"));
      float normalStrength = Float.valueOf(properties.getProperty("fractal" + i + ".normalStrength"));
      int random = Integer.valueOf(properties.getProperty("fractal" + i + ".random"));
      Vec2f direction = new Vec2f(Float.valueOf(properties.getProperty("fractal" + i + ".direction.x")),
          Float.valueOf(properties.getProperty("fractal" + i + ".direction.y")));
      float intensity = Float.valueOf(properties.getProperty("fractal" + i + ".intensity"));
      float alignment = Float.valueOf(properties.getProperty("fractal" + i + ".alignment"));
      boolean choppy = Integer.valueOf(properties.getProperty("fractal" + i + ".choppy")) == 1 ? true : false;

      FractalMap fractal = new FractalMap(fractalMapResolution, L, amplitude,
          direction, intensity, capillar, alignment, choppy,
          scaling, heightStrength, horizontalStrength, normalStrength, random);
      fractal.render();

      fractals.add(fractal);
    }

    renderFractalMap(fractals);
    createHeightmapDataBuffer();
  }

  public void createHeightmapDataBuffer() {

    heightmapDataBuffer = BufferUtil.createFloatBuffer(
        getHeightmap().getMetaData().getWidth() * getHeightmap().getMetaData().getHeight());
    heightmap.bind();
    // GL_GREEN since y-space (height) stored in green channel
    glGetTexImage(GL_TEXTURE_2D, 0, GL_GREEN, GL_FLOAT, heightmapDataBuffer);
  }

  public void renderFractalMap(List<FractalMap> fractals) {

    FractalMapGenerator fractalMapGenerator = new FractalMapGenerator(heightmapResolution, edgeElevation);
    fractalMapGenerator.render(fractals);
    GLTexture vHeightmap = fractalMapGenerator.getHeightmap();
    GLTexture vNormalmap = fractalMapGenerator.getNormalmap();
    setHeightmap(vHeightmap);
    setNormalmap(vNormalmap);

    SplatMapGenerator splatMapGenerator = new SplatMapGenerator(heightmapResolution);
    splatmap = splatMapGenerator.getSplatmap();
    splatMapGenerator.render(vNormalmap, vHeightmap);
  }

  //--------------------------------------------------//
  //               Heightmap Generation               //
  //--------------------------------------------------//

  public class FractalMapGenerator {

    @Getter
    private GLTexture heightmap;

    @Getter
    private GLTexture normalmap;

    private FractalMapShader shader;
    private int N;
    private boolean edgeElevation;

    public FractalMapGenerator(int N, boolean edgeElevation) {

      this.N = N;
      this.edgeElevation = edgeElevation;
      shader = new FractalMapShader();

      heightmap = new TextureStorage2D(N, N, (int) (Math.log(N) / Math.log(2)), ImageFormat.RGBA32FLOAT);
      heightmap.bind();
      heightmap.bilinearFilter();
      heightmap.unbind();
      heightmap.getMetaData().setWidth(N);
      heightmap.getMetaData().setHeight(N);

      normalmap = new TextureStorage2D(N, N, (int) (Math.log(N) / Math.log(2)), ImageFormat.RGBA32FLOAT);
      normalmap.bind();
      normalmap.bilinearFilter();
      normalmap.unbind();
      normalmap.getMetaData().setWidth(N);
      normalmap.getMetaData().setHeight(N);
    }

    public void render(List<FractalMap> fractals) {

      shader.bind();
      shader.updateUniforms(fractals, N, edgeElevation);
      glBindImageTexture(0, heightmap.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
      glBindImageTexture(1, normalmap.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();
      heightmap.bind();
      heightmap.bilinearFilter();
    }

  }

  @Getter
  public class FractalMap {

    private GLTexture heightmap;
    private GLTexture dxDisplacement;
    private GLTexture dzDisplacement;
    private GLTexture normalmap;
    private int scaling;
    private float heightStrength;
    private float horizontalStrength;
    private float normalStrength;

    // FFT parameter
    private int N;
    private int L;
    private float amplitude;
    private Vec2f direction;
    private float intensity;
    private float capillar;
    private float alignment;
    private boolean choppy;

    private FFT fft;


    public FractalMap(int N, int L, float amplitude, Vec2f direction,
        float intensity, float capillar, float alignment, boolean choppy,
        int scaling, float heightstrength, float horizontalStrength, float normalStrength, int random) {

      this.N = N;
      this.scaling = scaling;
      this.heightStrength = heightstrength;
      this.normalStrength = normalStrength;
      this.horizontalStrength = horizontalStrength;
      this.scaling = scaling;
      this.choppy = choppy;
      fft = new FFT(N, L, amplitude, direction, alignment, intensity, capillar);
      fft.setChoppy(choppy);
      fft.setT(random);
    }

    public void render() {

      fft.init();
      fft.render();
      heightmap = fft.getDy();
      heightmap.bind();
      heightmap.bilinearFilter();
      dxDisplacement = fft.getDx();
      dxDisplacement.bind();
      dxDisplacement.bilinearFilter();
      dzDisplacement = fft.getDz();
      dzDisplacement.bind();
      dzDisplacement.bilinearFilter();
      NormalRenderer normalRenderer = new NormalRenderer(N);
      normalRenderer.setStrength(normalStrength);
      normalRenderer.render(heightmap);
      normalmap = normalRenderer.getNormalmap();
      normalmap.bind();
      normalmap.bilinearFilter();
    }

  }

  public class FractalMapShader extends GLShaderProgram {

    public FractalMapShader() {

      super();

      addComputeShader(ResourceLoader.loadShader("shaders/terrain/HeightMap.comp"));
      compileShader();

      addUniform("N");
      addUniform("edgeElevation");

      for (int i = 0; i < 8; i++) {
        addUniform("fractals[" + i + "].dy");
        addUniform("fractals[" + i + "].dx");
        addUniform("fractals[" + i + "].dz");
        addUniform("fractals[" + i + "].normalmap");
        addUniform("fractals[" + i + "].scaling");
        addUniform("fractals[" + i + "].verticalStrength");
        addUniform("fractals[" + i + "].horizontalStrength");
        addUniform("fractals[" + i + "].choppy");
      }
    }

    public void updateUniforms(List<FractalMap> fractals, int N, boolean edgeElevation) {

      setUniformi("N", N);
      setUniformi("edgeElevation", edgeElevation ? 1 : 0);

      for (int i = 0; i < 8; i++) {
        glActiveTexture(GL_TEXTURE0 + i * 4);
        fractals.get(i).getHeightmap().bind();
        glActiveTexture(GL_TEXTURE1 + i * 4);
        fractals.get(i).getDxDisplacement().bind();
        glActiveTexture(GL_TEXTURE2 + i * 4);
        fractals.get(i).getDzDisplacement().bind();

        glActiveTexture(GL_TEXTURE3 + i * 4);
        fractals.get(i).getNormalmap().bind();

        setUniformi("fractals[" + i + "].dy", 0 + i * 4);
        setUniformi("fractals[" + i + "].dx", 1 + i * 4);
        setUniformi("fractals[" + i + "].dz", 2 + i * 4);
        setUniformi("fractals[" + i + "].normalmap", 3 + i * 4);
        setUniformi("fractals[" + i + "].scaling", fractals.get(i).getScaling());
        setUniformf("fractals[" + i + "].verticalStrength", fractals.get(i).getHeightStrength());
        setUniformf("fractals[" + i + "].horizontalStrength", fractals.get(i).getHorizontalStrength());
        setUniformi("fractals[" + i + "].choppy", fractals.get(i).isChoppy() ? 1 : 0);
      }
    }

  }

  //--------------------------------------------------//
  //               Splatmap Generation                //
  //--------------------------------------------------//

  public class SplatMapGenerator {

    @Getter
    private GLTexture splatmap;
    private SplatMapShader shader;
    private int N;

    public SplatMapGenerator(int N) {

      this.N = N;
      shader = new SplatMapShader();
      splatmap = new TextureStorage2D(N, N, (int) (Math.log(N) / Math.log(2)), ImageFormat.RGBA16FLOAT);
      splatmap.bind();
      splatmap.bilinearFilter();
      splatmap.unbind();
    }

    public void render(GLTexture normalmap, GLTexture heightmap) {

      shader.bind();
      glBindImageTexture(0, splatmap.getHandle(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA16F);
      glBindImageTexture(1, heightmap.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
      glBindImageTexture(2, normalmap.getHandle(), 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
      glDispatchCompute(N / 16, N / 16, 1);
      glFinish();
      splatmap.bind();
      splatmap.bilinearFilter();
    }

  }

  public class SplatMapShader extends GLShaderProgram {

    public SplatMapShader() {

      super();

      addComputeShader(ResourceLoader.loadShader("shaders/terrain/SplatMap.comp"));
      compileShader();
    }
  }

}
