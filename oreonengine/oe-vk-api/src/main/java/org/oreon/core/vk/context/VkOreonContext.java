package org.oreon.core.vk.context;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.device.PhysicalDevice;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.platform.VkWindow;
import org.oreon.core.vk.scenegraph.VkCamera;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkOreonContext extends BaseOreonContext {
	
	@Getter
	private static ByteBuffer[] enabledLayers;
	@Getter
	private static VulkanInstance vkInstance;
	@Getter
	private static VkResources resources;
	@Getter
	private static DeviceManager deviceManager;
	@Getter
	private static long surface;
	
	public static void create(){
		
		init();
		
		window = new VkWindow();
		resources = new VkResources();
		camera = new VkCamera();
		deviceManager = new DeviceManager();
		
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");
		
		if (!glfwVulkanSupported()) {
            throw new AssertionError("GLFW failed to find the Vulkan loader");
        }
		
		ByteBuffer[] layers = {
            	memUTF8("VK_LAYER_LUNARG_standard_validation")
//            	memUTF8("VK_LAYER_LUNARG_assistant_layer")
		};
		
		enabledLayers = layers;
		
	    vkInstance = new VulkanInstance(
	    		VkUtil.getValidationLayerNames(
	    				config.isVkValidation(),
	    				layers));

	    getWindow().create();
	    
	    LongBuffer pSurface = memAllocLong(1);
	    int err = glfwCreateWindowSurface(vkInstance.getHandle(), BaseOreonContext.getWindow().getId(), null, pSurface);
	    
	    surface = pSurface.get(0);
	    if (err != VK_SUCCESS) {
	        throw new AssertionError("Failed to create surface: " + VkUtil.translateVulkanResult(err));
	    }
	    
        PhysicalDevice physicalDevice = new PhysicalDevice(vkInstance.getHandle(), surface);
	    LogicalDevice logicalDevice = new LogicalDevice(physicalDevice, 0);
	    VkDeviceBundle majorDevice = new VkDeviceBundle(physicalDevice, logicalDevice);
	    VkOreonContext.getDeviceManager().addDevice(DeviceType.MAJOR_GRAPHICS_DEVICE, majorDevice);
	    
	    DescriptorPool descriptorPool = new DescriptorPool(
	    		majorDevice.getLogicalDevice().getHandle(), 4);
	    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 33);
	    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, 61);
	    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 2);
	    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 12);
	    descriptorPool.create();
	    majorDevice.getLogicalDevice().addDescriptorPool(Thread.currentThread().getId(), descriptorPool);
	}
	
	public static VkCamera getCamera(){
		
		return (VkCamera) camera;
	}
	
	public static VkWindow getWindow(){
		
		return (VkWindow) window;
	}
	
}
