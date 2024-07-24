package org.oreon.examples.gl.oreonworlds.plants;

import java.util.ArrayList;
import java.util.List;

import org.oreon.core.gl.memory.GLMeshVBO;
import org.oreon.core.gl.scenegraph.GLRenderInfo;
import org.oreon.core.gl.util.GLAssimpModelLoader;
import org.oreon.core.gl.wrapper.parameter.CullFaceDisable;
import org.oreon.core.instanced.InstancedObject;
import org.oreon.core.model.Model;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.examples.gl.oreonworlds.shaders.InstancedWireframeShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.GrassShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.GrassShadowShader;

public class Plant01ClusterGroup extends InstancedObject {

  public Plant01ClusterGroup() {

    List<Model> models = GLAssimpModelLoader.loadModel("oreonworlds/assets/plants/Plant_01", "billboardmodel.obj");

    List<Renderable> objects = new ArrayList<>();

    for (Model model : models) {

      GLMeshVBO meshBuffer = new GLMeshVBO();
      model.getMesh().setTangentSpace(false);
//			model.getMesh().setInstanced(true);

      meshBuffer.addData(model.getMesh());

      GLRenderInfo renderInfo = new GLRenderInfo(GrassShader.getInstance(), new CullFaceDisable(), meshBuffer);
      GLRenderInfo shadowRenderInfo = new GLRenderInfo(GrassShadowShader.getInstance(), new CullFaceDisable(),
          meshBuffer);
      GLRenderInfo wireframeRenderInfo = new GLRenderInfo(InstancedWireframeShader.getInstance(), new CullFaceDisable(),
          meshBuffer);

      Renderable object = new Renderable();
      object.addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
      object.addComponent(NodeComponentType.SHADOW_RENDERINFO, shadowRenderInfo);
      object.addComponent(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);
      object.addComponent(NodeComponentType.MATERIAL0, model.getMaterial());
      objects.add(object);
    }

//		addCluster(new Plant01Cluster(40,new Vec3f(-2171,0,2776),objects));
//		addCluster(new Plant01Cluster(20,new Vec3f(-1125,0,1448),objects));
//		addCluster(new Plant01Cluster(20,new Vec3f(-1174,0,1612),objects));
//		
//		setThread(new Thread(this));
//		getThread().start();
  }

//	public void run() {
//			while(isRunning()){
//			
//			InstancedHandler.getInstance().getLock().lock();
//			try {
//				InstancedHandler.getInstance().getCondition().await();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			finally{
//				InstancedHandler.getInstance().getLock().unlock();
//			}
//			
//			synchronized (getChildren()) {
//				
//				getChildren().clear();
//		
//				for (InstancedCluster cluster : getClusters()){
//					if (cluster.getCenter().sub(BaseContext.getCamera().getPosition()).length() < 6000){
//						addChild(cluster);
//					}
//				}
//			}
//		}
//	}
}
