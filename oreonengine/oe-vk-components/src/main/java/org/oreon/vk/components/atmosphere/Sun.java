package org.oreon.vk.components.atmosphere;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.math.Vec3f;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.scenegraph.VkMeshData;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;
import org.oreon.core.vk.wrapper.command.SecondaryDrawCmdBuffer;
import org.oreon.core.vk.wrapper.image.VkImageBundle;
import org.oreon.core.vk.wrapper.image.VkImageHelper;
import org.oreon.core.vk.wrapper.pipeline.GraphicsPipeline;

public class Sun extends Renderable{
	
	private VkImageBundle sunImageBundle;
	private VkImageBundle sunImageBundle_lightScattering;

	public Sun() {
		
		LogicalDevice device = VkOreonContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkOreonContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		getWorldTransform().setTranslation(BaseOreonContext.getConfig().getSunPosition().normalize().mul(-2600));
		Vec3f origin = new Vec3f(0,0,0);
		Vec3f[] array = new Vec3f[1];
		array[0] = origin;
		
		VkImage sunImage = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				"textures/sun/sun.png",
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_GRAPHICS_BIT);
		
		VkImageView sunImageView = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, sunImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		VkSampler sunImageSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR,
				false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		sunImageBundle = new VkImageBundle(sunImage, sunImageView, sunImageSampler);
		
		VkImage sunImage_lightScattering = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				"textures/sun/sun_small.png",
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_GRAPHICS_BIT);
		
		VkImageView sunImageView_lightScattering = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, sunImage_lightScattering.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		VkSampler sunImageSampler_lightScattering = new VkSampler(device.getHandle(), VK_FILTER_LINEAR,
				false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		sunImageBundle_lightScattering = new VkImageBundle(sunImage_lightScattering,
				sunImageView_lightScattering, sunImageSampler_lightScattering);
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS);
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(array);
		
		VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		
		List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 2);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		device.getDescriptorPool(Thread.currentThread().getId()).getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(
	    		sunImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		sunImageSampler.getHandle(), 0,
				VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		sunImageView_lightScattering.getHandle(),
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				sunImageSampler_lightScattering.getHandle(), 1,
				VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    
	    descriptorSets.add(VkOreonContext.getCamera().getDescriptorSet());
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(VkOreonContext.getCamera().getDescriptorSetLayout());
		descriptorSetLayouts.add(descriptorSetLayout);
		
		int pushConstantRange = Float.BYTES * 16;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrix()));
		pushConstants.flip();
	    
	    ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("shaders/sun/sun.vert.spv");
	    shaderPipeline.createFragmentShader("shaders/sun/sun.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    VkPipeline graphicsPipeline = new GraphicsPipeline(device.getHandle(),
	    		shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_POINT_LIST,
	    		VkUtil.createLongBuffer(descriptorSetLayouts),
				BaseOreonContext.getConfig().getFrameWidth(),
				BaseOreonContext.getConfig().getFrameHeight(),
				VkOreonContext.getResources().getTransparencyFbo().getRenderPass().getHandle(),
				VkOreonContext.getResources().getTransparencyFbo().getColorAttachmentCount(),
				1,
				pushConstantRange, VK_SHADER_STAGE_VERTEX_BIT);
	    
	    CommandBuffer mainCommandBuffer = new SecondaryDrawCmdBuffer(
	    		device.getHandle(),
	    		device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(), 
	    		graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
	    		VkOreonContext.getResources().getTransparencyFbo().getFrameBuffer().getHandle(),
	    		VkOreonContext.getResources().getTransparencyFbo().getRenderPass().getHandle(),
	    		0,
	    		VkUtil.createLongArray(descriptorSets),
	    		vertexBufferObject.getHandle(), 1,
	    		pushConstants, VK_SHADER_STAGE_VERTEX_BIT);
	    
	    VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
	    		.vertexBuffer(vertexBuffer).build();
	    VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(mainCommandBuffer)
	    		.pipeline(graphicsPipeline).descriptorSets(descriptorSets)
	    		.descriptorSetLayouts(descriptorSetLayouts).build();
	    
	    addComponent(NodeComponentType.MESH_DATA, meshData);
	    addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	    
	    shaderPipeline.destroy();
	}
	
	public void shutdown(){
		
		super.shutdown();
		sunImageBundle.destroy();
		sunImageBundle_lightScattering.destroy();
	}
	
}
