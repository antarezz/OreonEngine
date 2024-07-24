package org.oreon.core.vk.wrapper.buffer;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.wrapper.command.BufferCopyCmdBuffer;

public class VkBufferHelper {

  public static VkBuffer createDeviceLocalBuffer(VkDevice device,
      VkPhysicalDeviceMemoryProperties memoryProperties,
      long commandPool, VkQueue queue, ByteBuffer dataBuffer, int usage) {

    StagingBuffer stagingBuffer = new StagingBuffer(device, memoryProperties, dataBuffer);
    DeviceLocalBuffer deviceLocalBuffer = new DeviceLocalBuffer(device, memoryProperties,
        dataBuffer.limit(), usage);

    BufferCopyCmdBuffer bufferCopyCommand = new BufferCopyCmdBuffer(device, commandPool);
    bufferCopyCommand.record(stagingBuffer.getHandle(),
        deviceLocalBuffer.getHandle(), 0, 0, dataBuffer.limit());
    Fence fence = new Fence(device);
    bufferCopyCommand.submit(queue, fence);

    bufferCopyCommand.destroy();
    stagingBuffer.destroy();

    return deviceLocalBuffer;
  }

}
