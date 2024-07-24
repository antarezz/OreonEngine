package org.oreon.core.scenegraph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class RenderList {

  @Getter
  private LinkedHashMap<String, Renderable> objectList;
  @Setter
  private boolean changed;

  public RenderList() {

    changed = false;
    objectList = new LinkedHashMap<String, Renderable>();
  }

  public boolean contains(String id) {

    return objectList.containsKey(id);
  }

  public Renderable get(String key) {

    return objectList.get(key);
  }

  public void add(Renderable object) {

    objectList.put(object.getId(), object);
  }

  public void remove(Renderable object) {

    objectList.remove(object.getId());
  }

  public void remove(String key) {

    objectList.remove(key);
  }

  public Set<String> getKeySet() {

    return objectList.keySet();
  }

  public Set<Map.Entry<String, Renderable>> getEntrySet() {

    return objectList.entrySet();
  }

  public Collection<Renderable> getValues() {

    return objectList.values();
  }

  public List<Renderable> sortFrontToBack() {

    // TODO
    return null;
  }

  public List<Renderable> sortBackToFront() {

    // TODO
    return null;
  }

  public boolean hasChanged() {
    return changed;
  }

  public boolean isEmpty() {
    return objectList.isEmpty();
  }
}
