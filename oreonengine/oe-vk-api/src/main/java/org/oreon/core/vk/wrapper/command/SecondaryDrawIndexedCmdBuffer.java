package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.vk.command.CommandBuffer;

public class SecondaryDrawIndexedCmdBuffer extends CommandBuffer {

  public SecondaryDrawIndexedCmdBuffer(VkDevice device, long commandPool) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
  }

  public SecondaryDrawIndexedCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets,
      long vertexBuffer, long indexBuffer, int indexCount) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_SECONDARY);

    record(pipeline, pipelineLayout, framebuffer,
        renderpass, subpass, descriptorSets,
        vertexBuffer, indexBuffer, indexCount,
        null, -1);
  }

  public SecondaryDrawIndexedCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets,
      long vertexBuffer, long indexBuffer, int indexCount,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_SECONDARY);

    record(pipeline, pipelineLayout, framebuffer,
        renderpass, subpass, descriptorSets,
        vertexBuffer, indexBuffer, indexCount,
        pushConstantsData, pushConstantsStageFlags);
  }

  public void record(long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets, long vertexBuffer,
      long indexBuffer, int indexCount,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    beginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
        framebuffer, renderpass, subpass);
    if (pushConstantsStageFlags != -1) {
      pushConstantsCmd(pipelineLayout, pushConstantsStageFlags, pushConstantsData);
    }
    bindGraphicsPipelineCmd(pipeline);
    bindVertexInputCmd(vertexBuffer, indexBuffer);
    if (descriptorSets != null) {
      bindGraphicsDescriptorSetsCmd(pipelineLayout, descriptorSets);
    }
    drawIndexedCmd(indexCount);
    finishRecord();
  }

}
