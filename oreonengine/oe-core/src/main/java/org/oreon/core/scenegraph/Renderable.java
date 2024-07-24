package org.oreon.core.scenegraph;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

public class Renderable extends Node {

  private HashMap<NodeComponentType, NodeComponent> components;
  @Setter
  protected boolean render;

  public Renderable() {
    super();

    render = true;
    components = new HashMap<NodeComponentType, NodeComponent>();
  }

  public void addComponent(NodeComponentType type, NodeComponent component) {
    component.setParent(this);
    components.put(type, component);
  }

  public void update() {
    for (Map.Entry<NodeComponentType, NodeComponent> entry : components.entrySet()) {
      if (entry.getKey() != NodeComponentType.LIGHT) {
        entry.getValue().update();
      }
    }

    super.update();
  }

  public void updateLights() {
    for (Map.Entry<NodeComponentType, NodeComponent> entry : components.entrySet()) {
      if (entry.getKey() == NodeComponentType.LIGHT) {
        entry.getValue().update();
      }
    }

    super.update();
  }

  public void input() {
    components.values().forEach(component -> component.input());

    super.input();
  }

  public void render() {

    if (components.containsKey(NodeComponentType.MAIN_RENDERINFO)) {
      components.get(NodeComponentType.MAIN_RENDERINFO).render();
    }

    super.render();
  }

  public void renderWireframe() {

    if (components.containsKey(NodeComponentType.WIREFRAME_RENDERINFO)) {
      components.get(NodeComponentType.WIREFRAME_RENDERINFO).render();
    }

    super.renderWireframe();
  }

  public void renderShadows() {
    if (components.containsKey(NodeComponentType.SHADOW_RENDERINFO)) {
      components.get(NodeComponentType.SHADOW_RENDERINFO).render();
    }

    super.renderShadows();
  }

  public void record(RenderList renderList) {

    if (render) {
      if (!renderList.contains(id)) {
        renderList.add(this);
        renderList.setChanged(true);
      }
    } else {
      if (renderList.contains(id)) {
        renderList.remove(this);
        renderList.setChanged(true);
      }
    }

    super.record(renderList);
  }

  public void shutdown() {

    components.values().forEach(component -> component.shutdown());

    super.shutdown();
  }

  public Map<NodeComponentType, NodeComponent> getComponents() {
    return components;
  }

  @SuppressWarnings("unchecked")
  public <T> T getComponent(NodeComponentType type) {
    return (T) this.components.get(type);
  }
}
