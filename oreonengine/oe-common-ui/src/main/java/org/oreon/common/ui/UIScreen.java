package org.oreon.common.ui;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.oreon.core.scenegraph.RenderList;

@Getter
@Setter
public class UIScreen {

  private List<UIElement> elements = new ArrayList<UIElement>();

  public void render() {
    elements.forEach(element -> element.render());
  }

  public void update() {
    elements.forEach(element -> element.update());
  }

  public void record(RenderList renderList) {
    elements.forEach(element -> element.record(renderList));
  }

  public void shutdown() {
    elements.forEach(element -> element.shutdown());
  }
}
