package org.oreon.common.ui;

import java.util.ArrayList;
import java.util.List;

import org.oreon.core.math.Vec2f;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex;
import org.oreon.core.util.Util;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class UITextPanel extends UIElement {

  protected Mesh panel;
  protected String outputText;
  private int numFonts;

  public UITextPanel(String text, int xPos, int yPos, int xScaling, int yScaling) {
    super(xPos, yPos, xScaling, yScaling);
    this.outputText = text;
    numFonts = text.length();
    panel = generatePanelVertices();
    generateFontMapUvCoords(text);
  }

  public Mesh generatePanelVertices() {

    List<Vertex> vertexList = new ArrayList<Vertex>();
    List<Integer> indexList = new ArrayList<Integer>();

    for (int i = 0; i < numFonts; i++) {

      Mesh mesh = UIPanelLoader.load("gui/basicPanel.gui");

      for (Vertex v : mesh.getVertices()) {
        v.getPosition().setX(v.getPosition().getX() + i * 0.65f);
        vertexList.add(v);
      }
      for (int index : mesh.getIndices()) {
        indexList.add(index + 4 * i);
      }
    }

    Vertex[] vertices = new Vertex[vertexList.size()];
    vertexList.toArray(vertices);

    Integer[] objectArray = new Integer[indexList.size()];
    indexList.toArray(objectArray);
    int[] indices = Util.toIntArray(objectArray);

    Mesh mesh = new Mesh(vertices, indices);

    return mesh;
  }

  public void generateFontMapUvCoords(String text) {

    List<Vec2f> uvList = new ArrayList<Vec2f>();

    for (int i = 0; i < numFonts; i++) {
      Vec2f[] fontMapUv = new Vec2f[4];
      fontMapUv = Util.texCoordsFromFontMap(text.charAt(i));
      uvList.add(fontMapUv[0]);
      uvList.add(fontMapUv[1]);
      uvList.add(fontMapUv[2]);
      uvList.add(fontMapUv[3]);
    }

    if (uvList.size() != panel.getVertices().length) {
      log.error("uv count not equal vertex count");
    }

    int i = 0;
    for (Vertex v : panel.getVertices()) {
      v.setUVCoord(uvList.get(i));
      i++;
    }
  }

  public void update(String newText) {

    outputText = newText;

    String textToDisplay = new String();

    if (newText.length() > numFonts) {
      log.error("Text to update too long");
    }
    if (newText.length() < numFonts) {
      int offset = numFonts - newText.length();
      for (int i = 0; i < offset; i++) {
        textToDisplay += " ";
      }
    }
    textToDisplay += newText;

    generateFontMapUvCoords(textToDisplay);
  }

}
