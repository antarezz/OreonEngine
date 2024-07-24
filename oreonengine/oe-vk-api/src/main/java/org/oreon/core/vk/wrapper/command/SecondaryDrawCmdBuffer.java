package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.vk.command.CommandBuffer;

public class SecondaryDrawCmdBuffer extends CommandBuffer {

  public SecondaryDrawCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets,
      long vertexBuffer, int vertexCount) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_SECONDARY);

    record(pipeline, pipelineLayout, framebuffer,
        renderpass, subpass, descriptorSets,
        vertexBuffer, vertexCount,
        null, -1);
  }

  public SecondaryDrawCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets,
      long vertexBuffer, int vertexCount,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_SECONDARY);

    record(pipeline, pipelineLayout, framebuffer,
        renderpass, subpass, descriptorSets,
        vertexBuffer, vertexCount,
        pushConstantsData, pushConstantsStageFlags);
  }

  private void record(long pipeline, long pipelineLayout, long framebuffer,
      long renderpass, int subpass, long[] descriptorSets, long vertexBuffer, int vertexCount,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    beginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
        framebuffer, renderpass, subpass);

    if (pushConstantsStageFlags != -1) {
      pushConstantsCmd(pipelineLayout, pushConstantsStageFlags, pushConstantsData);
    }
    bindGraphicsPipelineCmd(pipeline);
    bindVertexInputCmd(vertexBuffer);
    bindGraphicsDescriptorSetsCmd(pipelineLayout, descriptorSets);
    drawCmd(vertexCount);
    finishRecord();
  }

}
