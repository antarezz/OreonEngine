package org.oreon.examples.gl.oreonworlds.plants;

import java.util.ArrayList;
import java.util.List;

import org.oreon.common.terrain.TerrainHelper;
import org.oreon.core.gl.instanced.GLInstancedObject;
import org.oreon.core.gl.memory.GLMeshVBO;
import org.oreon.core.gl.scenegraph.GLRenderInfo;
import org.oreon.core.gl.util.GLAssimpModelLoader;
import org.oreon.core.gl.wrapper.parameter.CullFaceDisable;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.model.Model;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.IntegerReference;
import org.oreon.examples.gl.oreonworlds.shaders.plants.GrassShader;
import org.oreon.gl.components.terrain.GLTerrain;

public class Grass01ClusterGroup extends GLInstancedObject {

  public Grass01ClusterGroup() {

    setInstanceCount(1);
    Vec3f[] positions = {new Vec3f(243.69344f, 220.08157f, -2171.1907f)};
    setPositions(positions);
    setHighPolyRange(-1);

    List<Model> models = GLAssimpModelLoader.loadModel("oreonworlds/assets/plants/Grass_01", "grassmodel.obj");

    List<Renderable> objects = new ArrayList<>();

    setHighPolyInstanceCount(new IntegerReference(0));
    setLowPolyInstanceCount(new IntegerReference(getInstanceCount()));

    for (Model model : models) {

      GLMeshVBO meshBuffer = new GLMeshVBO();
      model.getMesh().setTangentSpace(false);

      meshBuffer.addData(model.getMesh());
      meshBuffer.setDrawInstanced(true);
      meshBuffer.setInstances(getLowPolyInstanceCount());

      GLRenderInfo renderInfo = new GLRenderInfo(GrassShader.getInstance(), new CullFaceDisable(), meshBuffer);

      Renderable object = new Renderable();
      object.addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
      object.addComponent(NodeComponentType.MATERIAL0, model.getMaterial());
      objects.add(object);
      addChild(object);
      getLowPolyObjects().add(object);
    }

    for (int i = 0; i < getInstanceCount(); i++) {

      float s = (float) (Math.random() * 6 + 20);
      Vec3f translation = getPositions()[i];
      Vec3f scaling = new Vec3f(s, s, s);
      Vec3f rotation = new Vec3f(0, (float) Math.random() * 360f, 0);

      float terrainHeight = TerrainHelper.getTerrainHeight(GLTerrain.getConfig(), translation.getX(),
          translation.getZ());
      terrainHeight -= 1;
      translation.setY(terrainHeight);

      Matrix4f translationMatrix = new Matrix4f().Translation(translation);
      Matrix4f rotationMatrix = new Matrix4f().Rotation(rotation);
      Matrix4f scalingMatrix = new Matrix4f().Scaling(scaling);

      getWorldMatrices().add(translationMatrix.mul(scalingMatrix.mul(rotationMatrix)));
      getModelMatrices().add(rotationMatrix);
      getLowPolyIndices().add(i);
    }

    initMatricesBuffers();
  }
}
