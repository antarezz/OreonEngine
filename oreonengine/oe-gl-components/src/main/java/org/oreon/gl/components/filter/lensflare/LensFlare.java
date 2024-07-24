package org.oreon.gl.components.filter.lensflare;

import java.util.ArrayList;
import java.util.List;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.light.Light;
import org.oreon.core.light.LightHandler;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec2f;

public class LensFlare {

  private List<LensFlareTexturePanel> lensFlareTexturePanels = new ArrayList<LensFlareTexturePanel>();
  private Vec2f windowMidPos = new Vec2f(
      ContextHolder.getContext().getConfig().getFrameWidth() / 2f,
      ContextHolder.getContext().getConfig().getFrameHeight() / 2f
  );

//	private float occlusionThreshold = 80000f;

  public LensFlare() {

    LensFlareTexturePanel texturePanel0 = new LensFlareTexturePanel();
    texturePanel0.setTexture(new TextureImage2D("textures/lens_flare/tex4.png", SamplerFilter.Bilinear));
    texturePanel0.getOrthoTransform().setScaling(200, 200, 0);
    texturePanel0.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel0.getOrthoTransform().getWorldMatrix()));
    texturePanel0.setSpacing(0.06f);
    texturePanel0.setBrightness(0.2f);
    lensFlareTexturePanels.add(texturePanel0);

    LensFlareTexturePanel texturePanel1 = new LensFlareTexturePanel();
    texturePanel1.setTexture(new TextureImage2D("textures/lens_flare/tex01.png", SamplerFilter.Bilinear));
    texturePanel1.getOrthoTransform().setScaling(400, 400, 0);
    texturePanel1.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel1.getOrthoTransform().getWorldMatrix()));
    texturePanel1.setSpacing(0.0f);
    texturePanel1.setBrightness(0.4f);
    lensFlareTexturePanels.add(texturePanel1);

    LensFlareTexturePanel texturePanel2 = new LensFlareTexturePanel();
    texturePanel2.setTexture(new TextureImage2D("textures/lens_flare/tex2.png", SamplerFilter.Bilinear));
    texturePanel2.getOrthoTransform().setScaling(250, 250, 0);
    texturePanel2.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel2.getOrthoTransform().getWorldMatrix()));
    texturePanel2.setSpacing(0.2f);
    texturePanel2.setBrightness(0.25f);
    lensFlareTexturePanels.add(texturePanel2);

    LensFlareTexturePanel texturePanel3 = new LensFlareTexturePanel();
    texturePanel3.setTexture(new TextureImage2D("textures/lens_flare/tex7.png", SamplerFilter.Bilinear));
    texturePanel3.getOrthoTransform().setScaling(200, 200, 0);
    texturePanel3.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel3.getOrthoTransform().getWorldMatrix()));
    texturePanel3.setSpacing(0.4f);
    texturePanel3.setBrightness(0.2f);
    lensFlareTexturePanels.add(texturePanel3);

    LensFlareTexturePanel texturePanel4 = new LensFlareTexturePanel();
    texturePanel4.setTexture(new TextureImage2D("textures/lens_flare/tex5.png", SamplerFilter.Bilinear));
    texturePanel4.getOrthoTransform().setScaling(100, 100, 0);
    texturePanel4.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel4.getOrthoTransform().getWorldMatrix()));
    texturePanel4.setSpacing(0.6f);
    texturePanel4.setBrightness(0.2f);
    lensFlareTexturePanels.add(texturePanel4);

    LensFlareTexturePanel texturePanel5 = new LensFlareTexturePanel();
    texturePanel5.setTexture(new TextureImage2D("textures/lens_flare/tex3.png", SamplerFilter.Bilinear));
    texturePanel5.getOrthoTransform().setScaling(100, 100, 0);
    texturePanel5.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel5.getOrthoTransform().getWorldMatrix()));
    texturePanel5.setSpacing(0.8f);
    texturePanel5.setBrightness(0.1f);
    lensFlareTexturePanels.add(texturePanel5);

    LensFlareTexturePanel texturePanel6 = new LensFlareTexturePanel();
    texturePanel6.setTexture(new TextureImage2D("textures/lens_flare/tex9.png", SamplerFilter.Bilinear));
    texturePanel6.getOrthoTransform().setScaling(100, 100, 0);
    texturePanel6.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel6.getOrthoTransform().getWorldMatrix()));
    texturePanel6.setSpacing(1.1f);
    texturePanel6.setBrightness(0.1f);
    lensFlareTexturePanels.add(texturePanel6);

    LensFlareTexturePanel texturePanel7 = new LensFlareTexturePanel();
    texturePanel7.setTexture(new TextureImage2D("textures/lens_flare/tex1.png", SamplerFilter.Bilinear));
    texturePanel7.getOrthoTransform().setScaling(100, 100, 0);
    texturePanel7.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel7.getOrthoTransform().getWorldMatrix()));
    texturePanel7.setSpacing(1.3f);
    texturePanel7.setBrightness(0.1f);
    lensFlareTexturePanels.add(texturePanel7);

    LensFlareTexturePanel texturePanel8 = new LensFlareTexturePanel();
    texturePanel8.setTexture(new TextureImage2D("textures/lens_flare/tex4.png", SamplerFilter.Bilinear));
    texturePanel8.getOrthoTransform().setScaling(300, 300, 0);
    texturePanel8.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel8.getOrthoTransform().getWorldMatrix()));
    texturePanel8.setSpacing(1.7f);
    texturePanel8.setBrightness(0.05f);
    lensFlareTexturePanels.add(texturePanel8);

    LensFlareTexturePanel texturePanel9 = new LensFlareTexturePanel();
    texturePanel9.setTexture(new TextureImage2D("textures/lens_flare/tex8.png", SamplerFilter.Bilinear));
    texturePanel9.getOrthoTransform().setScaling(400, 400, 0);
    texturePanel9.setOrthographicMatrix(
        new Matrix4f().Orthographic2D().mul(texturePanel9.getOrthoTransform().getWorldMatrix()));
    texturePanel9.setSpacing(2.0f);
    texturePanel9.setBrightness(0.05f);
    lensFlareTexturePanels.add(texturePanel9);
  }

  public void render() {

    if (ContextHolder.getContext().getConfig().isRenderWireframe()) {
      return;
    }

    for (Light light : LightHandler.getLights()) {

      Vec2f lightScreenSpacePos = light.getScreenSpacePosition();

      if (lightScreenSpacePos == null) {
        return;
      }

      Vec2f sunToWindowCenter = windowMidPos.sub(lightScreenSpacePos);

//			float brightness = 1 - sunToWindowCenter.div(new Vec2f(BaseContext.getConfig().getFrameWidth(), BaseContext.getConfig().getFrameHeight())).length();

      for (LensFlareTexturePanel lensFlareTexture : lensFlareTexturePanels) {

        lensFlareTexture.getOrthoTransform().getTranslation().setX(
            light.getScreenSpacePosition().getX() + (sunToWindowCenter.getX() * lensFlareTexture.getSpacing())
                - lensFlareTexture.getOrthoTransform().getScaling().getX() / 2f);
        lensFlareTexture.getOrthoTransform().getTranslation().setY(
            light.getScreenSpacePosition().getY() + (sunToWindowCenter.getY() * lensFlareTexture.getSpacing())
                - lensFlareTexture.getOrthoTransform().getScaling().getY() / 2f);
        lensFlareTexture.setOrthographicMatrix(
            new Matrix4f().Orthographic2D().mul(lensFlareTexture.getOrthoTransform().getWorldMatrix()));

        // TODO occlusion needs a rework due to atmospheric scattering of the sun
//				float transparency = (light.getOcclusionQuery().getOcclusionFactor()/occlusionThreshold) * brightness;
        lensFlareTexture.setTransparency(0.75f);
        lensFlareTexture.render();
      }
    }
  }
}
