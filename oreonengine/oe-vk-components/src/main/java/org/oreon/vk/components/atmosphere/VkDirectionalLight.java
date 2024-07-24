package org.oreon.vk.components.atmosphere;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.light.DirectionalLight;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.context.VkResources.VkDescriptorName;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.wrapper.buffer.VkUniformBuffer;
import org.oreon.core.vk.wrapper.descriptor.VkDescriptor;

import lombok.Getter;

@Getter
public class VkDirectionalLight extends DirectionalLight{

	private VkUniformBuffer ubo_light;
	private DescriptorSet descriptorSet;
	private DescriptorSetLayout descriptorSetLayout;
	
	public VkDirectionalLight(){
		
		super();
		
		LogicalDevice device = VkOreonContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkOreonContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		ubo_light = new VkUniformBuffer(device.getHandle(), memoryProperties, BufferUtil.createByteBuffer(getFloatBufferLight()));
		
		descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 1);
	    descriptorSetLayout.addLayoutBinding(0,VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_COMPUTE_BIT);
	    descriptorSetLayout.create();
		
	    descriptorSet = new DescriptorSet(device.getHandle(),
	    		VkOreonContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE)
	    		.getDescriptorPool(Thread.currentThread().getId()).getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorBuffer(ubo_light.getHandle(), lightBufferSize, 0, 0,
	    		VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
	    
	    VkOreonContext.getResources().getDescriptors().put(VkDescriptorName.DIRECTIONAL_LIGHT, new VkDescriptor(descriptorSet, descriptorSetLayout));
	}

	public void updateLightUbo() {
		ubo_light.mapMemory(BufferUtil.createByteBuffer(getFloatBufferLight()));
	}

	public void updateMatricesUbo() {

	}
}
