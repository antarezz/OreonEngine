package org.oreon.examples.gl.oreonworlds.ocean;

import org.oreon.core.math.Vec4f;
import org.oreon.core.util.Constants;
import org.oreon.gl.components.water.Water;
import org.oreon.gl.components.water.WaterShader;
import org.oreon.gl.components.water.WaterWireframeShader;

public class Ocean extends Water {

  public Ocean() {

    super(128, WaterShader.getInstance(), WaterWireframeShader.getInstance());

    getWorldTransform().setScaling(Constants.ZFAR * 1.95f, 1, Constants.ZFAR * 1.95f);
    getWorldTransform().setTranslation(-Constants.ZFAR * 1.95f / 2, 0, -Constants.ZFAR * 1.95f / 2);

    setClip_offset(4);
    setClipplane(new Vec4f(0, -1, 0, getWorldTransform().getTranslation().getY() + 20));

    initShaderBuffer();
  }
}
