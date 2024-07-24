package org.oreon.core.vk.context;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.*;

@Log4j2
@Getter
public class VkOreonContext extends BaseOreonContext<VkCamera, VkWindow, VkResources> {

  private final ByteBuffer[] enabledLayers;
  private final VulkanInstance vkInstance;
  private final DeviceManager deviceManager;
  private final long surface;

  public VkOreonContext() {
    super(new VkCamera(), new VkWindow(), new VkResources(), null);
    deviceManager = new DeviceManager();

		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

    if (!glfwVulkanSupported()) {
      throw new AssertionError("GLFW failed to find the Vulkan loader");
    }

    enabledLayers = new ByteBuffer[]{
        memUTF8("VK_LAYER_LUNARG_standard_validation")
        //, memUTF8("VK_LAYER_LUNARG_assistant_layer")
    };
    vkInstance = new VulkanInstance(
        VkUtil.getValidationLayerNames(getConfig().isVkValidation(), enabledLayers)
    );

    getWindow().create();

    LongBuffer pSurface = memAllocLong(1);
    int err = glfwCreateWindowSurface(vkInstance.getHandle(), getWindow().getId(), null, pSurface);
    surface = pSurface.get(0);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create surface: " + VkUtil.translateVulkanResult(err));
    }
    PhysicalDevice physicalDevice = new PhysicalDevice(vkInstance.getHandle(), surface);
    LogicalDevice logicalDevice = new LogicalDevice(physicalDevice, 0);
    VkDeviceBundle majorDevice = new VkDeviceBundle(physicalDevice, logicalDevice);
    deviceManager.addDevice(DeviceType.MAJOR_GRAPHICS_DEVICE, majorDevice);

    DescriptorPool descriptorPool = new DescriptorPool(
        majorDevice.getLogicalDevice().getHandle(), 4);
    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 33);
    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, 61);
    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 2);
    descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 12);
    descriptorPool.create();
    majorDevice.getLogicalDevice().addDescriptorPool(Thread.currentThread().threadId(), descriptorPool);
  }
}
