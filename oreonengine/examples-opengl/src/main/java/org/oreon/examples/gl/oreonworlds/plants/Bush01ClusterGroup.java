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
import org.oreon.examples.gl.oreonworlds.shaders.plants.BushShader;
import org.oreon.examples.gl.oreonworlds.shaders.plants.BushShadowShader;

public class Bush01ClusterGroup extends InstancedObject {

  public Bush01ClusterGroup() {

    List<Renderable> objects = new ArrayList<>();

    List<Model> models = GLAssimpModelLoader.loadModel("oreonworlds/assets/plants/Bush_01", "Bush_01.obj");

    for (Model model : models) {

      GLMeshVBO meshBuffer = new GLMeshVBO();
      model.getMesh().setTangentSpace(false);
//			model.getMesh().setInstanced(true);
      meshBuffer.addData(model.getMesh());

      GLRenderInfo renderInfo = new GLRenderInfo(BushShader.getInstance(), new CullFaceDisable(), meshBuffer);
      GLRenderInfo shadowRenderInfo = new GLRenderInfo(BushShadowShader.getInstance(), new CullFaceDisable(),
          meshBuffer);

      Renderable object = new Renderable();
      object.addComponent(NodeComponentType.MAIN_RENDERINFO, renderInfo);
      object.addComponent(NodeComponentType.SHADOW_RENDERINFO, shadowRenderInfo);
      object.addComponent(NodeComponentType.MATERIAL0, model.getMaterial());
      objects.add(object);
    }

//		addCluster(new Bush01Cluster(20,new Vec3f(-2082,0,2881),objects));
//		addCluster(new Bush01Cluster(40,new Vec3f(-2125,0,2891),objects));
//		addCluster(new Bush01Cluster(20,new Vec3f(-2188,0,2894),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1288,0,1627),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1319,0,1679),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1255,0,1581),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-2173,0,2752),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-2104,0,2722),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1434,0,2638),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1518,0,2686),getChildren()));
//		addCluster(new Bush01Cluster(6,new Vec3f(-1529,0,2093),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1262,0,1674),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1325,0,1647),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1245,0,1281),getChildren()));
//		addCluster(new Bush01Cluster(10,new Vec3f(-1233,0,1318),getChildren()));
//		addCluster(new Bush01Cluster(6,new Vec3f(-1214,0,1395),getChildren()));
//		addCluster(new Bush01Cluster(6,new Vec3f(-1166,0,1511),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1228,0,1349),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1238,0,1384),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1222,0,1417),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-837,0,1501),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-853,0,1559),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-889,0,1621),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1958,0,2705),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1944,0,2632),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-1863,0,2630),getChildren()));
//		addCluster(new Bush01Cluster(6,new Vec3f(-1437,0,1823),getChildren()));
//		addCluster(new Bush01Cluster(8,new Vec3f(-250,0,-96),getChildren()));

//		setThread(new Thread(this));
//		getThread().start();
  }

//	@Override
//	public void run() {
//		while(isRunning()){
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
//					if (cluster.getCenter().sub(BaseContext.getCamera().getPosition()).length() < 600){
//						addChild(cluster);
//					}
//				}
//			}
//		}
//	}
}
