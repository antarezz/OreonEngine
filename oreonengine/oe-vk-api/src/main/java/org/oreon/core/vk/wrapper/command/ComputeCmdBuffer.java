package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.vk.command.CommandBuffer;

public class ComputeCmdBuffer extends CommandBuffer {

  public ComputeCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long[] descriptorSets,
      int groupCountX, int groupCountY, int groupCountZ) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    record(pipeline, pipelineLayout, descriptorSets,
        groupCountX, groupCountY, groupCountZ, null, 0);
  }

  public ComputeCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long[] descriptorSets,
      int groupCountX, int groupCountY, int groupCountZ,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    record(pipeline, pipelineLayout, descriptorSets,
        groupCountX, groupCountY, groupCountZ,
        pushConstantsData, pushConstantsStageFlags);
  }

  public ComputeCmdBuffer(VkDevice device, long commandPool,
      long pipeline, long pipelineLayout, long[] descriptorSets,
      int groupCountX, int groupCountY, int groupCountZ,
      long image, int imageLayout) {

    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);

    record(pipeline, pipelineLayout, descriptorSets,
        groupCountX, groupCountY, groupCountZ,
        image, imageLayout);
  }

  public void record(long pipeline, long pipelineLayout,
      long[] descriptorSets, int groupCountX, int groupCountY, int groupCountZ,
      ByteBuffer pushConstantsData, int pushConstantsStageFlags) {

    beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
    if (pushConstantsData != null) {
      pushConstantsCmd(pipelineLayout, pushConstantsStageFlags, pushConstantsData);
    }
    bindComputePipelineCmd(pipeline);
    bindComputeDescriptorSetsCmd(pipelineLayout, descriptorSets);
    dispatchCmd(groupCountX, groupCountY, groupCountZ);
    finishRecord();
  }

  public void record(long pipeline, long pipelineLayout,
      long[] descriptorSets, int groupCountX, int groupCountY, int groupCountZ,
      long image, int imageLayout) {

    beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
    clearColorImageCmd(image, imageLayout);
    bindComputePipelineCmd(pipeline);
    bindComputeDescriptorSetsCmd(pipelineLayout, descriptorSets);
    dispatchCmd(groupCountX, groupCountY, groupCountZ);
    finishRecord();
  }

}
