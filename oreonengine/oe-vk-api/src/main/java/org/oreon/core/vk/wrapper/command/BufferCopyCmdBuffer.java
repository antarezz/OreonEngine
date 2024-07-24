package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.synchronization.Fence;

public class BufferCopyCmdBuffer extends CommandBuffer {

  public BufferCopyCmdBuffer(VkDevice device, long commandPool) {
    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
  }

  public void record(long srcBuffer, long dstBuffer, long srcOffset, long dstOffset, long size) {

    beginRecord(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
    copyBufferCmd(srcBuffer, dstBuffer, srcOffset, dstOffset, size);
    finishRecord();
  }

  public void submit(VkQueue queue) {

    SubmitInfo submitInfo = new SubmitInfo(getHandlePointer());
    submitInfo.submit(queue);
  }

  public void submit(VkQueue queue, Fence fence) {

    SubmitInfo submitInfo = new SubmitInfo(getHandlePointer());
    submitInfo.setFence(fence);
    submitInfo.submit(queue);
    fence.waitForFence();
  }

}
