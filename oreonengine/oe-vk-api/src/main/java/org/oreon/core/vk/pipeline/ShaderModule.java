package org.oreon.core.vk.pipeline;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.oreon.core.util.ResourceLoader;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class ShaderModule {

  @Getter
  private VkPipelineShaderStageCreateInfo shaderStageInfo;
  @Getter
  private long handle;

  private final VkDevice device;

  public ShaderModule(VkDevice device, String filePath, int stage) {

    this.device = device;
    createShaderModule(filePath);
    createShaderStage(stage);
  }

  private void createShaderModule(String filePath) {

    ByteBuffer shaderCode = null;
    try {
      shaderCode = ResourceLoader.ioResourceToByteBuffer(filePath, 1024);
    } catch (IOException e) {
      e.printStackTrace();
    }

    int err;
    VkShaderModuleCreateInfo moduleCreateInfo = VkShaderModuleCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
        .pNext(0)
        .pCode(shaderCode)
        .flags(0);
    LongBuffer pShaderModule = memAllocLong(1);
    err = vkCreateShaderModule(device, moduleCreateInfo, null, pShaderModule);
    handle = pShaderModule.get(0);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create shader module: " + VkUtil.translateVulkanResult(err));
    }

    memFree(pShaderModule);
    moduleCreateInfo.free();
  }

  private void createShaderStage(int stage) {

    shaderStageInfo = VkPipelineShaderStageCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
        .stage(stage)
        .module(handle)
        .pName(memUTF8("main"))
        .pSpecializationInfo(null);
  }

  public void destroy() {

    vkDestroyShaderModule(device, handle, null);
    handle = -1;
  }

}
