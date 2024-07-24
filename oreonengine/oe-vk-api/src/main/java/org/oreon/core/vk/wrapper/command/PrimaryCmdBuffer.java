package org.oreon.core.vk.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.math.Vec3f;
import org.oreon.core.vk.command.CommandBuffer;

public class PrimaryCmdBuffer extends CommandBuffer {

  public PrimaryCmdBuffer(VkDevice device, long commandPool) {
    super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
  }

  public void record(long renderPass, long frameBuffer,
      int width, int height, int colorAttachmentCount, int depthAttachment,
      PointerBuffer secondaryCmdBuffers) {

    beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
    beginRenderPassCmd(renderPass, frameBuffer, width, height,
        colorAttachmentCount, depthAttachment,
        VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

    if (secondaryCmdBuffers != null) {
      recordSecondaryCmdBuffers(secondaryCmdBuffers);
    }

    endRenderPassCmd();
    finishRecord();
  }

  public void record(long renderPass, long frameBuffer,
      int width, int height, int colorAttachmentCount, int depthAttachment,
      Vec3f clearColor, PointerBuffer secondaryCmdBuffers) {

    beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
    beginRenderPassCmd(renderPass, frameBuffer, width, height,
        colorAttachmentCount, depthAttachment,
        VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS,
        clearColor);
    if (secondaryCmdBuffers != null) {
      recordSecondaryCmdBuffers(secondaryCmdBuffers);
    }

    endRenderPassCmd();
    finishRecord();
  }
}
