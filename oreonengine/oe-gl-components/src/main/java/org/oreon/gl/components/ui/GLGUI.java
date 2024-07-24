package org.oreon.gl.components.ui;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import org.oreon.common.ui.GUI;
import org.oreon.common.ui.UIPanelLoader;
import org.oreon.common.ui.UIScreen;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.scenegraph.RenderList;

public class GLGUI extends GUI {

  protected GLTexture fontsTexture;
  protected GUIVAO panelMeshBuffer;

  public void init() {
    fontsTexture = new TextureImage2D("gui/tex/Fonts.png", SamplerFilter.Bilinear);
    panelMeshBuffer = new GUIVAO();
    panelMeshBuffer.addData(UIPanelLoader.load("gui/basicPanel.gui"));
  }

  @Override
  public void render() {
    glDisable(GL_DEPTH_TEST);
    super.render();
    glEnable(GL_DEPTH_TEST);
  }

}
