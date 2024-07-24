package org.oreon.vk.engine;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.LinkedHashMap;

import org.oreon.core.RenderEngine;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.RenderList;
import org.oreon.core.target.FrameBufferObject.Attachment;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.swapchain.SwapChain;
import org.oreon.core.vk.synchronization.VkSemaphore;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.command.PrimaryCmdBuffer;
import org.oreon.vk.components.atmosphere.VkDirectionalLight;
import org.oreon.vk.components.filter.Bloom;
import org.oreon.vk.components.planet.Planet;
import org.oreon.vk.components.ui.VkGUI;

import lombok.Setter;

public class VkDeferredEngine extends RenderEngine {
	
	private SwapChain swapChain;
	private VkDeviceBundle graphicsDevice;
	
	private VkFrameBufferObject offScreenFbo;
	private VkFrameBufferObject transparencyFbo;
	
	private VkSemaphore offScreenSemaphore;
	private VkSemaphore deferredStageSemaphore;
	private VkSemaphore transparencySemaphore;
	private VkSemaphore postProcessingSemaphore;
	
	private CommandBuffer deferredStageCmdBuffer;
	private SubmitInfo deferredStageSubmitInfo;
	
	private CommandBuffer postProcessingCmdBuffer;
	private SubmitInfo postProcessingSubmitInfo;
	
	private PrimaryCmdBuffer offScreenPrimaryCmdBuffer;
	private LinkedHashMap<String, CommandBuffer> offScreenSecondaryCmdBuffers;
	private RenderList offScreenRenderList;
	private SubmitInfo offScreenSubmitInfo;
	
	private PrimaryCmdBuffer transparencyPrimaryCmdBuffer;
	private LinkedHashMap<String, CommandBuffer> transparencySecondaryCmdBuffers;
	private RenderList transparencyRenderList;
	private SubmitInfo transparencySubmitInfo;
	
	// uniform buffers
	// private VkUniformBuffer renderStateUbo;

	private SampleCoverage sampleCoverage;
	private DeferredLighting deferredLighting;
	private FXAA fxaa;
	private OpaqueTransparencyBlending opaqueTransparencyBlending;
	
	// post processing filter
	private Bloom bloom;
	
	// gui
	@Setter
	private VkGUI gui;
	
	@Override
	public void init() {
		
		super.init();

		sceneGraph.addObject(new VkDirectionalLight());
		
		offScreenRenderList = new RenderList();
		transparencyRenderList = new RenderList();
		offScreenSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		transparencySecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		
		graphicsDevice = VkOreonContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
	    
	    offScreenFbo = new OffScreenFbo(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getPhysicalDevice().getMemoryProperties());
	    transparencyFbo = new TransparencyFbo(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getPhysicalDevice().getMemoryProperties());
	    
	    VkOreonContext.getResources().setOffScreenFbo(offScreenFbo);
	    VkOreonContext.getResources().setTransparencyFbo(transparencyFbo);
	    
	    // Semaphore creations
	    offScreenSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    deferredStageSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    transparencySemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    postProcessingSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    
	    // offscreen opaque primary command buffer creation
	    offScreenPrimaryCmdBuffer =  new PrimaryCmdBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());
	    offScreenSubmitInfo = new SubmitInfo();
	    offScreenSubmitInfo.setCommandBuffers(offScreenPrimaryCmdBuffer.getHandlePointer());
	    offScreenSubmitInfo.setSignalSemaphores(offScreenSemaphore.getHandlePointer());
	    
	    // offscreen transparency primary command buffer creation
	    transparencyPrimaryCmdBuffer =  new PrimaryCmdBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());
	    transparencySubmitInfo = new SubmitInfo();
	    transparencySubmitInfo.setCommandBuffers(transparencyPrimaryCmdBuffer.getHandlePointer());
	    transparencySubmitInfo.setSignalSemaphores(transparencySemaphore.getHandlePointer());
	    
	    sampleCoverage = new SampleCoverage(graphicsDevice,
	    		BaseOreonContext.getConfig().getFrameWidth(),
	    		BaseOreonContext.getConfig().getFrameHeight(),
	    		offScreenFbo.getAttachmentImageView(Attachment.POSITION),
	    		offScreenFbo.getAttachmentImageView(Attachment.LIGHT_SCATTERING),
	    		offScreenFbo.getAttachmentImageView(Attachment.SPECULAR_EMISSION_DIFFUSE_SSAO_BLOOM));
	    
	    deferredLighting = new DeferredLighting(graphicsDevice,
	    		BaseOreonContext.getConfig().getFrameWidth(),
	    		BaseOreonContext.getConfig().getFrameHeight(),
	    		offScreenFbo.getAttachmentImageView(Attachment.COLOR),
	    		offScreenFbo.getAttachmentImageView(Attachment.POSITION),
	    		offScreenFbo.getAttachmentImageView(Attachment.NORMAL),
	    		offScreenFbo.getAttachmentImageView(Attachment.SPECULAR_EMISSION_DIFFUSE_SSAO_BLOOM),
	    		sampleCoverage.getSampleCoverageImageView());
	    
	    LongBuffer opaqueTransparencyBlendWaitSemaphores = memAllocLong(2);
	    opaqueTransparencyBlendWaitSemaphores.put(0, deferredStageSemaphore.getHandle());
	    opaqueTransparencyBlendWaitSemaphores.put(1, transparencySemaphore.getHandle());
	    opaqueTransparencyBlending = new OpaqueTransparencyBlending(graphicsDevice,
	    		BaseOreonContext.getConfig().getFrameWidth(),
	    		BaseOreonContext.getConfig().getFrameHeight(),
	    		deferredLighting.getDeferredLightingSceneImageView(),
	    		offScreenFbo.getAttachmentImageView(Attachment.DEPTH),
	    		sampleCoverage.getLightScatteringImageView(),
	    		transparencyFbo.getAttachmentImageView(Attachment.COLOR),
	    		transparencyFbo.getAttachmentImageView(Attachment.DEPTH),
	    		transparencyFbo.getAttachmentImageView(Attachment.ALPHA),
	    		transparencyFbo.getAttachmentImageView(Attachment.LIGHT_SCATTERING),
	    		opaqueTransparencyBlendWaitSemaphores);
	    
	    VkImageView displayImageView = deferredLighting.getDeferredLightingSceneImageView();

	    if (BaseOreonContext.getConfig().isFxaaEnabled()){
		    fxaa = new FXAA(graphicsDevice,
		    		BaseOreonContext.getConfig().getFrameWidth(),
		    		BaseOreonContext.getConfig().getFrameHeight(),
		    		displayImageView);
		    
		    displayImageView = fxaa.getFxaaImageView();
	    }
	    
	    if (BaseOreonContext.getConfig().isBloomEnabled()){
		    bloom = new Bloom(graphicsDevice,
		    		BaseOreonContext.getConfig().getFrameWidth(),
		    		BaseOreonContext.getConfig().getFrameHeight(),
		    		displayImageView,
		    		sampleCoverage.getSpecularEmissionDiffuseSsaoBloomImageView());
		    
		    displayImageView = bloom.getBloomSceneImageView();
	    }
	    
	    if (gui != null){
	    	// all post procssing effects and FXAA disabled
    		gui.init(displayImageView, postProcessingSemaphore.getHandlePointer());
			displayImageView = gui.getImageView();
		}
	    
	    swapChain = new SwapChain(graphicsDevice.getLogicalDevice(), graphicsDevice.getPhysicalDevice(),
	    		VkOreonContext.getSurface(), displayImageView.getHandle());
	    
	    // record sample coverage + deferred lighting command buffer
	    deferredStageCmdBuffer = new CommandBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
	    		VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    deferredStageCmdBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
	    sampleCoverage.record(deferredStageCmdBuffer);
	    deferredStageCmdBuffer.pipelineMemoryBarrierCmd(
	    		VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    deferredLighting.record(deferredStageCmdBuffer);
	    deferredStageCmdBuffer.finishRecord();
	    
	    IntBuffer pComputeShaderWaitDstStageMask = memAllocInt(1);
        pComputeShaderWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    deferredStageSubmitInfo = new SubmitInfo(deferredStageCmdBuffer.getHandlePointer());
	    deferredStageSubmitInfo.setWaitSemaphores(offScreenSemaphore.getHandlePointer());
	    deferredStageSubmitInfo.setWaitDstStageMask(pComputeShaderWaitDstStageMask);
	    deferredStageSubmitInfo.setSignalSemaphores(deferredStageSemaphore.getHandlePointer());
	    
	    // record post processing command buffer
	    postProcessingCmdBuffer = new CommandBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
	    		VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    postProcessingCmdBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
	    if (BaseOreonContext.getConfig().isFxaaEnabled()){
	    	fxaa.record(postProcessingCmdBuffer);
	    }
	    postProcessingCmdBuffer.pipelineMemoryBarrierCmd(
	    		VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    if (BaseOreonContext.getConfig().isBloomEnabled()){
	    	bloom.record(postProcessingCmdBuffer);
	    }
	    postProcessingCmdBuffer.finishRecord();
	
	    postProcessingSubmitInfo = new SubmitInfo(postProcessingCmdBuffer.getHandlePointer());
	    postProcessingSubmitInfo.setWaitSemaphores(opaqueTransparencyBlending.getSignalSemaphore().getHandlePointer());
	    postProcessingSubmitInfo.setWaitSemaphores(deferredStageSemaphore.getHandlePointer());
	    postProcessingSubmitInfo.setWaitDstStageMask(pComputeShaderWaitDstStageMask);
	    postProcessingSubmitInfo.setSignalSemaphores(postProcessingSemaphore.getHandlePointer());
	}
    
	@Override
	public void render() {
		
		sceneGraph.render();
		offScreenRenderList.setChanged(false);
		sceneGraph.record(offScreenRenderList);
		
		// update Terrain/Planet Quadtree
		if (sceneGraph.hasTerrain()){
			if (camera.isCameraMoved()){
				// start waiting updateQuadtree thread
				((Planet) sceneGraph.getTerrain()).getQuadtree().signal();
			}
		}

		// record new primary Command Buffer if renderList has changed
		if (offScreenRenderList.hasChanged()){
			
			offScreenSecondaryCmdBuffers.clear();
			
			offScreenRenderList.getKeySet().forEach(key ->
			{
				if(!offScreenSecondaryCmdBuffers.containsKey(key)){
					VkRenderInfo mainRenderInfo = offScreenRenderList.get(key)
							.getComponent(NodeComponentType.MAIN_RENDERINFO);
					
					if (mainRenderInfo != null)
						offScreenSecondaryCmdBuffers.put(key, mainRenderInfo.getCommandBuffer());
				}
			});

			// Offscreen primary render command buffer
			offScreenPrimaryCmdBuffer.reset();
			offScreenPrimaryCmdBuffer.record(offScreenFbo.getRenderPass().getHandle(),
					offScreenFbo.getFrameBuffer().getHandle(),
					offScreenFbo.getWidth(),
					offScreenFbo.getHeight(),
					offScreenFbo.getColorAttachmentCount(),
					offScreenFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(offScreenSecondaryCmdBuffers.values()));
		}
		
		if (!offScreenRenderList.isEmpty()){
			offScreenSubmitInfo.submit(graphicsDevice.getLogicalDevice().getGraphicsQueue());
		}
		
		deferredStageSubmitInfo.submit(graphicsDevice.getLogicalDevice().getComputeQueue());
		
		transparencyRenderList.setChanged(false);
		sceneGraph.recordTransparentObjects(transparencyRenderList);
		
		if (transparencyRenderList.hasChanged() && !transparencyRenderList.isEmpty()){
			
			transparencySecondaryCmdBuffers.clear();
			
			transparencyRenderList.getKeySet().forEach(key ->
			{
				if(!transparencySecondaryCmdBuffers.containsKey(key)){
					VkRenderInfo mainRenderInfo = transparencyRenderList.get(key)
							.getComponent(NodeComponentType.MAIN_RENDERINFO);
					transparencySecondaryCmdBuffers.put(key, mainRenderInfo.getCommandBuffer());
				}
			});
		
			// Tranparency primary render command buffer
			transparencyPrimaryCmdBuffer.reset();
			transparencyPrimaryCmdBuffer.record(transparencyFbo.getRenderPass().getHandle(),
					transparencyFbo.getFrameBuffer().getHandle(),
					transparencyFbo.getWidth(),
					transparencyFbo.getHeight(),
					transparencyFbo.getColorAttachmentCount(),
					transparencyFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(transparencySecondaryCmdBuffers.values()));
		}
		
		if(!transparencyRenderList.isEmpty()){
			transparencySubmitInfo.submit(graphicsDevice.getLogicalDevice().getGraphicsQueue());
			opaqueTransparencyBlending.render();
		}
		
		postProcessingSubmitInfo.submit(graphicsDevice.getLogicalDevice().getComputeQueue());
		
		if (gui != null){
			gui.render();
		}
		
		swapChain.draw(graphicsDevice.getLogicalDevice().getGraphicsQueue(),
				gui != null ? gui.getSignalSemaphore(): postProcessingSemaphore);
		swapChain.getDrawFence().waitForFence();
	}

	@Override
	public void update() {

		super.update();
		if (gui != null){
			gui.update();
		}
	}

	@Override
	public void shutdown() {
		
		// wait for queues to be finished before destroy vulkan objects
		vkDeviceWaitIdle(graphicsDevice.getLogicalDevice().getHandle());
		
		super.shutdown();
		
		offScreenFbo.destroy();
		transparencyFbo.destroy();
		offScreenSemaphore.destroy();
		deferredStageSemaphore.destroy();
		transparencySemaphore.destroy();
		postProcessingSemaphore.destroy();
		offScreenPrimaryCmdBuffer.destroy();
		deferredStageCmdBuffer.destroy();
		postProcessingCmdBuffer.destroy();
		transparencyPrimaryCmdBuffer.destroy();
		sampleCoverage.shutdown();
		deferredLighting.shutdown();
		if (fxaa != null){
			fxaa.shutdown();
		}
		opaqueTransparencyBlending.shutdown();
		if (bloom != null){
			bloom.shutdown();
		}
		if (gui != null){
			gui.shutdown();
		}
		swapChain.destroy();
		BaseOreonContext.getCamera().shutdown();
		graphicsDevice.getLogicalDevice().destroy();
		VkOreonContext.getVkInstance().destroy();
	}
	
}
