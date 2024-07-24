package org.oreon.core.scenegraph;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.oreon.core.math.Transform;

public class Node {

  protected String id;
  private Node parent;
  private List<Node> children;
  private Transform worldTransform;
  private Transform localTransform;

  public Node() {
    id = UUID.randomUUID().toString();
    setWorldTransform(new Transform());
    setLocalTransform(new Transform());
    setChildren(new ArrayList<Node>());
  }

  public void addChild(Node child) {
    child.setParent(this);
    children.add(child);
  }

  public void update() {
    getWorldTransform().setRotation(
        getWorldTransform().getLocalRotation().add(getParentNode().getWorldTransform().getRotation()));
    getWorldTransform().setTranslation(
        getWorldTransform().getLocalTranslation().add(getParentNode().getWorldTransform().getTranslation()));
    getWorldTransform().setScaling(
        getWorldTransform().getLocalScaling().mul(getParentNode().getWorldTransform().getScaling()));

    for (Node child : children) {
      child.update();
    }
  }

  public void updateLights() {

    for (Node child : children) {
      child.updateLights();
    }
  }

  public void input() {
    children.forEach(child -> child.input());
  }

  public void render() {
    children.forEach(child -> child.render());
  }

  public void renderWireframe() {
    children.forEach(child -> child.renderWireframe());
  }

  public void renderShadows() {
    children.forEach(child -> child.renderShadows());
  }

  public void record(RenderList renderList) {
    children.forEach(child -> child.record(renderList));
  }

  public void shutdown() {
    children.forEach(child -> child.shutdown());
  }

  public Node getParentNode() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  public <T> T getParentObject() {

    return (T) parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public List<Node> getChildren() {
    return children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  public Transform getWorldTransform() {
    return worldTransform;
  }

  public void setWorldTransform(Transform transform) {
    this.worldTransform = transform;
  }

  public Transform getLocalTransform() {
    return localTransform;
  }

  public void setLocalTransform(Transform localTransform) {
    this.localTransform = localTransform;
  }

  public String getId() {
    return id;
  }

}
