package org.oreon.core.light;

import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.query.OcclusionQuery;
import org.oreon.core.scenegraph.Node;

public class Light extends Node {

  protected Vec3f color;
  protected float intensity;
  private OcclusionQuery occlusionQuery;

  public Light(Vec3f position, Vec3f color, float intensity) {
    this.color = color;
    this.intensity = intensity;
    getLocalTransform().setTranslation(position);
  }

  public Light() {
  }

  public Vec2f getScreenSpacePosition() {

    Vec4f clipSpacePos = getLocalTransform().getModelViewProjectionMatrix().mul(new Vec4f(0, 0, 0, 1));
    Vec3f ndcSpacePos = new Vec3f(clipSpacePos.getX() / clipSpacePos.getW(), clipSpacePos.getY() / clipSpacePos.getW(),
        clipSpacePos.getZ() / clipSpacePos.getW());

    if (ndcSpacePos.getX() < -1 || ndcSpacePos.getX() > 1 || ndcSpacePos.getY() < -1 || ndcSpacePos.getY() > 1) {
      return null;
    }

    Vec2f windowSpacePos = (new Vec2f(ndcSpacePos.getX(), ndcSpacePos.getY()).add(1.0f)).div(2.0f).mul(
        new Vec2f(
            ContextHolder.getContext().getConfig().getFrameWidth(),
            ContextHolder.getContext().getConfig().getFrameHeight()
        )
    );

    return windowSpacePos;
  }

  public void occlusionQuery() {
    occlusionQuery.doQuery(this);
  }

  public Vec3f getColor() {
    return color;
  }

  public void setColor(Vec3f color) {
    this.color = color;
  }

  public float getIntensity() {
    return intensity;
  }

  public void setIntensity(float intensity) {
    this.intensity = intensity;
  }

  public OcclusionQuery getOcclusionQuery() {
    return occlusionQuery;
  }

  public void setOcclusionQuery(OcclusionQuery occlusionQuery) {
    this.occlusionQuery = occlusionQuery;
  }
}