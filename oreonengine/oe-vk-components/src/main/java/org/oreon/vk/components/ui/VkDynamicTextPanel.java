package org.oreon.vk.components.ui;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.common.ui.UITextPanel;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;
import org.oreon.core.vk.wrapper.buffer.VkUniformBuffer;
import org.oreon.core.vk.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import org.oreon.core.vk.wrapper.pipeline.GraphicsPipelineAlphaBlend;

public class VkDynamicTextPanel extends UITextPanel{
	
	private VkPipeline graphicsPipeline;
	private CommandBuffer cmdBuffer;
	private SubmitInfo submitInfo;
	private VkUniformBuffer buffer;
	
	public VkDynamicTextPanel(String text, int xPos, int yPos, int xScaling, int yScaling,
			VkImageView fontsImageView, VkSampler fontsSampler, VkFrameBufferObject fbo) {
		
		super(text, xPos, yPos, xScaling, yScaling);
		
		// flip y-axxis for vulkan coordinate system
		getOrthographicMatrix().set(1, 1, -getOrthographicMatrix().get(1, 1));
		
		VkDeviceBundle deviceBundle = VkOreonContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
		LogicalDevice device = deviceBundle.getLogicalDevice();
		DescriptorPool descriptorPool = device.getDescriptorPool(Thread.currentThread().getId());
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkOreonContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("shaders/ui/dynamicTextPanel.vert.spv");
	    shaderPipeline.createFragmentShader("shaders/ui/textPanel.frag.spv");
	    shaderPipeline.createShaderPipeline();

	    int pushConstantRange = Float.BYTES * 16;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getOrthographicMatrix()));
		pushConstants.flip();
		
		ByteBuffer ubo = memAlloc(Float.BYTES * panel.getVertices().length * 4);
		for (int i=0; i<panel.getVertices().length; i++){
			ubo.putFloat(panel.getVertices()[i].getUVCoord().getX());
			ubo.putFloat(panel.getVertices()[i].getUVCoord().getY());
			ubo.putFloat(0);
			ubo.putFloat(0);
		}
		ubo.flip();
		
		buffer = new VkUniformBuffer(device.getHandle(), memoryProperties, ubo);
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 2);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
	    		VK_SHADER_STAGE_VERTEX_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(fontsImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, fontsSampler.getHandle(),
	    		0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorBuffer(buffer.getHandle(),
	    		Float.BYTES * panel.getVertices().length * 2, 0, 1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
	    
	    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(descriptorSetLayout);
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(panel.getVertices(), VertexLayout.POS2D);
		ByteBuffer indexBuffer = BufferUtil.createByteBuffer(panel.getIndices());
		
		VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

        VkBuffer indexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        		device.getHandle(), memoryProperties,
        		device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        		device.getTransferQueue(),
        		indexBuffer, VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
        
        graphicsPipeline = new GraphicsPipelineAlphaBlend(device.getHandle(),
				shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
				VkUtil.createLongBuffer(descriptorSetLayouts),
				fbo.getWidth(), fbo.getHeight(),
				fbo.getRenderPass().getHandle(),
				fbo.getColorAttachmentCount(),
				1, pushConstantRange,
				VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
        
		cmdBuffer = new SecondaryDrawIndexedCmdBuffer(
				device.getHandle(),
				deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
				fbo.getFrameBuffer().getHandle(),
				fbo.getRenderPass().getHandle(),
				0,
				VkUtil.createLongArray(descriptorSets),
				vertexBufferObject.getHandle(), indexBufferObject.getHandle(),
				panel.getIndices().length,
				pushConstants,
				VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
		
		submitInfo = new SubmitInfo();
		submitInfo.setCommandBuffers(cmdBuffer.getHandlePointer());
		
		VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(cmdBuffer).build();
		addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	}
	
	public void update(String newText){
		
		if (outputText.equals(newText)){
			return;
		}
		
		super.update(newText);
		
		ByteBuffer ubo = memAlloc(Float.BYTES * panel.getVertices().length * 4);
		for (int i=0; i<panel.getVertices().length; i++){
			ubo.putFloat(panel.getVertices()[i].getUVCoord().getX());
			ubo.putFloat(panel.getVertices()[i].getUVCoord().getY());
			ubo.putFloat(0);
			ubo.putFloat(0);
		}
		ubo.flip();
		
		buffer.mapMemory(ubo);
	}
}
