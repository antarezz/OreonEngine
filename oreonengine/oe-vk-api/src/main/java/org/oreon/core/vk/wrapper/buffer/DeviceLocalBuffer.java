package org.oreon.core.vk.wrapper.buffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.vk.memory.VkBuffer;

public class DeviceLocalBuffer extends VkBuffer {

  public DeviceLocalBuffer(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      int size, int usage) {

    super(device, size, VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage);
    allocate(memoryProperties, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    bindBufferMemory();
  }

}
