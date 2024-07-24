package org.oreon.examples.gl.oreonworlds.plants;

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
import org.oreon.core.model.Vertex;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.IntegerReference;
import org.oreon.core.util.Util;
import org.oreon.examples.gl.oreonworlds.shaders.InstancedBillboardShader;
import org.oreon.examples.gl.oreonworlds.shaders.InstancedBillboardShadowShader;
import org.oreon.examples.gl.oreonworlds.shaders.InstancedWireframeShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.TreeLeavesShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.TreeShadowShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.TreeTrunkShader;
import org.oreon.gl.components.terrain.GLTerrain;

public class Tree01ClusterGroup extends GLInstancedObject {

  public Tree01ClusterGroup() {

    setInstanceCount(6);
    Vec3f[] positions = {new Vec3f(-1061.5507f, 152.36606f, 1029.8318f),
        new Vec3f(-1102.2029f, 162.44598f, 1242.017f), new Vec3f(-1149.664f, 143.76784f, 1060.4601f),
        new Vec3f(-1109.2875f, 172.1296f, 1304.6906f), new Vec3f(-1031.7795f, 118.724434f, 1067.6212f),
        new Vec3f(-1112.1095f, 141.70348f, 1041.5681f)};
    setPositions(positions);
    setHighPolyRange(800);

    List<Model> models = GLAssimpModelLoader.loadModel("oreonworlds/assets/plants/Tree_01", "tree01.obj");
    List<Model> billboards = GLAssimpModelLoader.loadModel("oreonworlds/assets/plants/Tree_01", "billboardmodel.obj");

    setHighPolyInstanceCount(new IntegerReference(0));
    setLowPolyInstanceCount(new IntegerReference(getInstanceCount()));

    for (Model model : models) {

      GLMeshVBO meshBuffer = new GLMeshVBO();

      if (model.equals(models.get(0))) {
        model.getMesh().setTangentSpace(true);
        Util.generateTangentsBitangents(model.getMesh());
      } else {
        model.getMesh().setTangentSpace(false);
      }

      for (Vertex vertex : model.getMesh().getVertices()) {
        vertex.getPosition().setX(vertex.getPosition().getX());
        vertex.getPosition().setZ(vertex.getPosition().getZ());
      }

      meshBuffer.addData(model.getMesh());
      meshBuffer.setDrawInstanced(true);
      meshBuffer.setInstances(getHighPolyInstanceCount());

      GLRenderInfo renderInfo;
      GLRenderInfo shadowRenderInfo;
      GLRenderInfo wireframeRenderInfo = new GLRenderInfo(InstancedWireframeShader.getInstance(), new CullFaceDisable(),
          meshBuffer);

      if (model.equals(models.get(0))) {
        renderInfo = new GLRenderInfo(TreeTrunkShader.getInstance(), new CullFaceDisable(), meshBuffer);
        shadowRenderInfo = new GLRenderInfo(TreeShadowShader.getInstance(), new CullFaceDisable(), meshBuffer);
      } else {
        renderInfo = new GLRenderInfo(TreeLeavesShader.getInstance(), new CullFaceDisable(), meshBuffer);
        shadowRenderInfo = new GLRenderInfo(TreeShadowShader.getInstance(), new CullFaceDisable(), meshBuffer);
      }

      Renderable object = new Renderable();
      object.addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
      object.addComponent(NodeComponentType.SHADOW_RENDERINFO, shadowRenderInfo);
      object.addComponent(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);
      object.addComponent(NodeComponentType.MATERIAL0, model.getMaterial());
      addChild(object);
      getHighPolyObjects().add(object);
    }

    for (Model billboard : billboards) {

      GLMeshVBO meshBuffer = new GLMeshVBO();

      billboard.getMesh().setTangentSpace(false);

      for (Vertex vertex : billboard.getMesh().getVertices()) {
        vertex.setPosition(vertex.getPosition().mul(8));
      }

      meshBuffer.addData(billboard.getMesh());
      meshBuffer.setDrawInstanced(true);
      meshBuffer.setInstances(getLowPolyInstanceCount());

      GLRenderInfo renderInfo = new GLRenderInfo(InstancedBillboardShader.getInstance(), new CullFaceDisable(),
          meshBuffer);
      GLRenderInfo shadowRenderInfo = new GLRenderInfo(InstancedBillboardShadowShader.getInstance(),
          new CullFaceDisable(), meshBuffer);
      GLRenderInfo wireframeRenderInfo = new GLRenderInfo(InstancedWireframeShader.getInstance(), new CullFaceDisable(),
          meshBuffer);

      Renderable object = new Renderable();
      object.addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
      object.addComponent(NodeComponentType.SHADOW_RENDERINFO, shadowRenderInfo);
      object.addComponent(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);
      object.addComponent(NodeComponentType.MATERIAL0, billboard.getMaterial());
      addChild(object);
      getLowPolyObjects().add(object);
    }

    for (int i = 0; i < getInstanceCount(); i++) {

      float s = (float) (Math.random() * 4 + 12);
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
