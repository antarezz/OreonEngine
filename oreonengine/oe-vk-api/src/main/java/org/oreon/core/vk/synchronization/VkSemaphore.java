package org.oreon.core.vk.synchronization;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkSemaphore {

  @Getter
  private long handle;
  @Getter
  private LongBuffer handlePointer;

  private VkDevice device;

  public VkSemaphore(VkDevice device) {

    this.device = device;

    VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
        .pNext(0)
        .flags(0);

    handlePointer = memAllocLong(1);

    int err = vkCreateSemaphore(device, semaphoreCreateInfo, null, handlePointer);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create semaphore: " + VkUtil.translateVulkanResult(err));
    }

    handle = handlePointer.get(0);

    semaphoreCreateInfo.free();
  }

  public void destroy() {

    vkDestroySemaphore(device, handle, null);
  }

}
