package org.oreon.core.vk.synchronization;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class Fence {

  @Getter
  private long handle;
  private LongBuffer pHandle;

  private VkDevice device;

  public Fence(VkDevice device) {

    this.device = device;

    VkFenceCreateInfo createInfo = VkFenceCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
        .pNext(0)
        .flags(VK_FENCE_CREATE_SIGNALED_BIT);

    pHandle = memAllocLong(1);
    VkUtil.vkCheckResult(vkCreateFence(device, createInfo, null, pHandle));

    handle = pHandle.get(0);

    createInfo.free();
  }

  public void reset() {

    VkUtil.vkCheckResult(vkResetFences(device, handle));
  }

  public void waitForFence() {

    VkUtil.vkCheckResult(vkWaitForFences(device, pHandle, true, 1000000000l));
  }

  public void destroy() {

    vkDestroyFence(device, handle, null);
  }
}
