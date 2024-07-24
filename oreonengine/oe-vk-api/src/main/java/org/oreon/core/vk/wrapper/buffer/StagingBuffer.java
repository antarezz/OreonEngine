package org.oreon.core.vk.wrapper.buffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.vk.memory.VkBuffer;

public class StagingBuffer extends VkBuffer {

  public StagingBuffer(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties,
      ByteBuffer dataBuffer) {

    super(device, dataBuffer.limit(), VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
    allocate(memoryProperties,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    bindBufferMemory();
    mapMemory(dataBuffer);
  }

}
