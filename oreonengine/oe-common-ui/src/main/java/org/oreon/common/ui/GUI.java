package org.oreon.common.ui;

import java.util.ArrayList;

import org.oreon.core.scenegraph.RenderList;

import lombok.Getter;

public abstract class GUI {

  @Getter
  private ArrayList<UIScreen> screens = new ArrayList<UIScreen>();


  public void update() {

    screens.forEach(screen -> screen.update());
  }

  public void render() {

    screens.forEach(screen -> screen.render());
  }

  public void record(RenderList renderList) {

    screens.forEach(screen -> screen.record(renderList));
  }

  public void shutdown() {

    screens.forEach(screen -> screen.shutdown());
  }

}
