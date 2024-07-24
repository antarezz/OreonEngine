package org.oreon.core.vk.image;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkSampler {

  @Getter
  private long handle;

  private VkDevice device;

  public VkSampler(VkDevice device, int filterMode,
      boolean anisotropic, float maxAnisotropy, int mipmapMode, float maxLod,
      int addressMode) {

    this.device = device;

    VkSamplerCreateInfo createInfo = VkSamplerCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
        .magFilter(filterMode)
        .minFilter(filterMode)
        .addressModeU(addressMode)
        .addressModeV(addressMode)
        .addressModeW(addressMode)
        .anisotropyEnable(anisotropic)
        .maxAnisotropy(maxAnisotropy)
        .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
        .unnormalizedCoordinates(false)
        .compareEnable(false)
        .compareOp(VK_COMPARE_OP_ALWAYS)
        .mipmapMode(mipmapMode)
        .mipLodBias(0)
        .minLod(0)
        .maxLod(maxLod);

    LongBuffer pBuffer = memAllocLong(1);
    int err = vkCreateSampler(device, createInfo, null, pBuffer);
    handle = pBuffer.get(0);

    memFree(pBuffer);
    createInfo.free();

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create sampler: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void destroy() {

    vkDestroySampler(device, handle, null);
  }

}
