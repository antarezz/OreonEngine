package org.oreon.core.scenegraph;

import org.oreon.core.math.Transform;

import lombok.Getter;

@Getter
public class Scenegraph extends Node {

  private Node root;
  private Node terrain;
  private Node water;
  private Node transparentObjects;

  private boolean hasTerrain = false;
  private boolean hasWater = false;

  public Scenegraph() {

    setWorldTransform(new Transform());

    root = new Node();
    terrain = new Node();
    water = new Node();
    transparentObjects = new Node();

    root.setParent(this);
    terrain.setParent(this);
    water.setParent(this);
    transparentObjects.setParent(this);
  }

  public void render() {
    root.render();
    terrain.render();
    water.render();
  }

  public void renderWireframe() {
    root.renderWireframe();
    terrain.renderWireframe();
    water.renderWireframe();
  }

  public void renderTransparentObejcts() {
    transparentObjects.render();
  }

  public void renderShadows() {
    root.renderShadows();
    terrain.renderShadows();
    water.renderShadows();
  }

  public void record(RenderList renderList) {
    root.record(renderList);
    terrain.record(renderList);
    water.record(renderList);
  }

  public void recordTransparentObjects(RenderList renderList) {
    transparentObjects.record(renderList);
  }

  public void update() {
    root.update();
    terrain.update();
    water.update();
    transparentObjects.update();
  }

  public void updateLights() {
    root.updateLights();
  }

  public void input() {
    root.input();
    terrain.input();
    water.input();
  }

  public void shutdown() {
    root.shutdown();
    terrain.shutdown();
    water.shutdown();
    transparentObjects.shutdown();
  }

  public void addObject(Node object) {
    root.addChild(object);
  }

  public void addTransparentObject(Node object) {
    transparentObjects.addChild(object);
  }

  public void setTerrain(Node vTerrain) {
    vTerrain.setParent(this);
    hasTerrain = true;
    terrain = vTerrain;
  }

  public void setWater(Node vWater) {
    vWater.setParent(this);
    hasWater = true;
    water = vWater;
  }

  public boolean hasTerrain() {
    return hasTerrain;
  }

  public boolean hasWater() {
    return hasWater;
  }

}
