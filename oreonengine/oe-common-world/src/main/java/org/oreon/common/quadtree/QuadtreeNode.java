package org.oreon.common.quadtree;

import java.util.Map;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.math.Transform;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.Node;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.RenderList;
import org.oreon.core.scenegraph.Renderable;

import lombok.Getter;

public abstract class QuadtreeNode extends Renderable{
	
	protected Vec3f worldPos;
	protected boolean isleaf;
	protected QuadtreeCache quadtreeCache;
	protected QuadtreeConfig quadtreeConfig;
	@Getter
	protected ChunkConfig chunkConfig;
	
	public QuadtreeNode(Map<NodeComponentType, NodeComponent> components,
			QuadtreeCache quadtreeCache, Transform worldTransform, 
			Vec2f location, int lod, Vec2f index){
		
		try {
			addComponent(NodeComponentType.MAIN_RENDERINFO, components.get(NodeComponentType.MAIN_RENDERINFO).clone());
			addComponent(NodeComponentType.WIREFRAME_RENDERINFO, components.get(NodeComponentType.WIREFRAME_RENDERINFO).clone());
			if (components.containsKey(NodeComponentType.SHADOW_RENDERINFO)){
				addComponent(NodeComponentType.SHADOW_RENDERINFO, components.get(NodeComponentType.SHADOW_RENDERINFO).clone());
			}
			addComponent(NodeComponentType.CONFIGURATION, components.get(NodeComponentType.CONFIGURATION));
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		
		quadtreeConfig = getComponent(NodeComponentType.CONFIGURATION);
		chunkConfig = new ChunkConfig(lod, location, index,
				1f/(quadtreeConfig.getRootChunkCount() * (float)(Math.pow(2, lod))));
		
		this.quadtreeCache = quadtreeCache; 
		this.isleaf = true;
		
		Vec3f localScaling = new Vec3f(chunkConfig.getGap(),0,chunkConfig.getGap());
		Vec3f localTranslation = new Vec3f(location.getX(),0,location.getY());
		
		getLocalTransform().setScaling(localScaling);
		getLocalTransform().setTranslation(localTranslation);
		
		setWorldTransform(worldTransform);
		getWorldTransform().getScaling().setY(quadtreeConfig.getVerticalScaling());
		
		computeWorldPos();
		updateQuadtree();
	}
	
	public void update()
	{
		for(Node child: getChildren())
			child.update();		
	}
	
	public void render()
	{
		boolean renderChunk = false;
		if (BaseOreonContext.getConfig().isRenderReflection() || BaseOreonContext.getConfig().isRenderRefraction()){
			// render only first two lod's for reflection/refraction
			renderChunk = (isleaf && chunkConfig.getLod() == 0) || (!isleaf && chunkConfig.getLod() == 0);// || (!isleaf && lod == 1);
		}
		else{
			renderChunk = isleaf;
		}
		
		if (renderChunk)
		{	
			getComponents().get(NodeComponentType.MAIN_RENDERINFO).render();
		}
		else
		{
			for(Node child: getChildren())
				child.render();
		}
	}
	
	public void renderWireframe()
	{
		if (isleaf){
			if (getComponents().containsKey(NodeComponentType.WIREFRAME_RENDERINFO)){
				getComponents().get(NodeComponentType.WIREFRAME_RENDERINFO).render();
			}
		}
		else{
			for(Node child: getChildren())
				child.renderWireframe();
		}
	}
	
	public void renderShadows()
	{
		if (isleaf){
			if (getComponents().containsKey(NodeComponentType.SHADOW_RENDERINFO)){
				getComponents().get(NodeComponentType.SHADOW_RENDERINFO).render();
			}
		}
		for(Node child: getChildren())
			child.renderShadows();
	}
	
	public void record(RenderList renderList){

		if (!renderList.contains(id)){
			renderList.add(this);
			renderList.setChanged(true);
		}
	}
	
	public void updateQuadtree(){
		
		updateChildNodes();
		
		for (Node node : getChildren()){
			((QuadtreeNode) node).updateQuadtree();
		}
	}
	
	private void updateChildNodes(){
		
		float distance = (BaseOreonContext.getCamera().getPosition().sub(worldPos)).length();
		
		if (distance < quadtreeConfig.getLod_range()[chunkConfig.getLod()]){
			add4ChildNodes(chunkConfig.getLod()+1);
		}
		else if(distance >= quadtreeConfig.getLod_range()[chunkConfig.getLod()]){
			removeChildNodes();
		}
	}
	
	private void add4ChildNodes(int lod){
	
		if (isleaf){
			isleaf = false;
		}
		if(getChildren().size() == 0){
			for (int i=0; i<2; i++){
				for (int j=0; j<2; j++){
					addChild(createChildChunk(getComponents(), quadtreeCache, getWorldTransform(),
							chunkConfig.getLocation().add(new Vec2f(i*chunkConfig.getGap()/2f,j*chunkConfig.getGap()/2f)), lod, new Vec2f(i,j)));
				}
			}
		}	
	}

	private void removeChildNodes(){
		
		if (!isleaf){
			isleaf = true;
		}
		if(getChildren().size() != 0){
			getChildren().clear();
		}
	}
	
	public String getQuadtreeCacheKey(){
		
		// LOD|LOC_X|LOC_Y|INDEX_X|INDEX_Y|WORLDPOS
		return String.valueOf(chunkConfig.getLod()) + String.valueOf(chunkConfig.getLocation().getX()) + String.valueOf(chunkConfig.getLocation().getY())
			+ String.valueOf(chunkConfig.getIndex().getX()) + String.valueOf(chunkConfig.getIndex().getY()) + worldPos.toString();
	}
	
	public String getQuadtreeCacheKey(int lod, Vec2f location, Vec2f index){
		
		// LOD|LOC_X|LOC_Y|INDEX_X|INDEX_Y
		return String.valueOf(lod) + String.valueOf(location.getX()) + String.valueOf(location.getY())
			+ String.valueOf(index.getX()) + String.valueOf(index.getY()) + worldPos.toString();
	}
	
	public void cacheChildrenTree(){
		
		// traverse children tree until max LOD
		if (chunkConfig.getLod() < quadtreeConfig.getLodCount()){
			if(getChildren().size() != 0){
				for (Node child : getChildren()){
					quadtreeCache.addChunk(child);
					((QuadtreeNode) child).cacheChildrenTree();
				}
				getChildren().clear();
			}
		}
	}
	
	public abstract QuadtreeNode createChildChunk(Map<NodeComponentType, NodeComponent> components,
		QuadtreeCache quadtreeCache, Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index);
	
	protected void computeWorldPos() {
			
		// TODO here with matrix multiplication
		Vec2f loc = chunkConfig.getLocation().add(chunkConfig.getGap()/2f).mul(quadtreeConfig.getHorizontalScaling()).sub(quadtreeConfig.getHorizontalScaling()/2f);
		float height = getTerrainHeight(loc.getX(), loc.getY());
		this.worldPos = new Vec3f(loc.getX(),height,loc.getY());
	}
	
	private float getTerrainHeight(float x, float z){
		
		float h = 0;
		
		Vec2f pos = new Vec2f();
		pos.setX(x);
		pos.setY(z);
		pos = pos.add(quadtreeConfig.getHorizontalScaling()/2f);
		pos = pos.div(quadtreeConfig.getHorizontalScaling());
		Vec2f floor = new Vec2f((int) Math.floor(pos.getX()), (int) Math.floor(pos.getY()));
		pos = pos.sub(floor);
		pos = pos.mul(quadtreeConfig.getHeightmap().getMetaData().getWidth());
		int x0 = (int) Math.floor(pos.getX());
		int x1 = x0 + 1;
		int z0 = (int) Math.floor(pos.getY());
		int z1 = z0 + 1;
		
		float h0 =  quadtreeConfig.getHeightmapDataBuffer().get(quadtreeConfig.getHeightmap().getMetaData().getWidth() * z0 + x0);
		float h1 =  quadtreeConfig.getHeightmapDataBuffer().get(quadtreeConfig.getHeightmap().getMetaData().getWidth() * z0 + x1);
		float h2 =  quadtreeConfig.getHeightmapDataBuffer().get(quadtreeConfig.getHeightmap().getMetaData().getWidth() * z1 + x0);
		float h3 =  quadtreeConfig.getHeightmapDataBuffer().get(quadtreeConfig.getHeightmap().getMetaData().getWidth() * z1 + x1);
		
		float percentU = pos.getX() - x0;
	    float percentV = pos.getY() - z0;
	    
	    float dU, dV;
	    if (percentU > percentV)
	    {   // bottom triangle
	        dU = h1 - h0;
	        dV = h3 - h1;
	    }
	    else
	    {   // top triangle
	        dU = h3 - h2;
	        dV = h2 - h0;
	    }
	    
	    h = h0 + (dU * percentU) + (dV * percentV );
	    h *= quadtreeConfig.getVerticalScaling();
		
		return h;
	}
	
	public QuadtreeNode getQuadtreeParent() {
		return (QuadtreeNode) getParentNode();
	}
}
