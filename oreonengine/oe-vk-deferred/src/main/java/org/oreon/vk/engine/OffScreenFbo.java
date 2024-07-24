package org.oreon.vk.engine;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

import java.nio.LongBuffer;
import lombok.Getter;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.vk.framebuffer.FrameBufferColorAttachment;
import org.oreon.core.vk.framebuffer.FrameBufferDepthAttachment;
import org.oreon.core.vk.framebuffer.VkFrameBuffer;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.pipeline.RenderPass;
import org.oreon.core.vk.wrapper.image.VkImageBundle;

@Getter
public class OffScreenFbo extends VkFrameBufferObject {

  public OffScreenFbo(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties) {

    width = ContextHolder.getContext().getConfig().getFrameWidth();
    height = ContextHolder.getContext().getConfig().getFrameHeight();
    int samples = ContextHolder.getContext().getConfig().getMultisampling_sampleCount();

    VkImageBundle albedoAttachment = new FrameBufferColorAttachment(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, samples);

    VkImageBundle worldPositionAttachment = new FrameBufferColorAttachment(device, memoryProperties,
        width, height, VK_FORMAT_R32G32B32A32_SFLOAT, samples);

    VkImageBundle normalAttachment = new FrameBufferColorAttachment(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, samples);

    VkImageBundle lightScatteringMaskAttachment = new FrameBufferColorAttachment(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, samples);

    VkImageBundle specularEmissionAttachment = new FrameBufferColorAttachment(device, memoryProperties,
        width, height, VK_FORMAT_R16G16B16A16_SFLOAT, samples);

    VkImageBundle depthBuffer = new FrameBufferDepthAttachment(device, memoryProperties,
        width, height, VK_FORMAT_D32_SFLOAT, samples);

    attachments.put(Attachment.COLOR, albedoAttachment);
    attachments.put(Attachment.POSITION, worldPositionAttachment);
    attachments.put(Attachment.NORMAL, normalAttachment);
    attachments.put(Attachment.LIGHT_SCATTERING, lightScatteringMaskAttachment);
    attachments.put(Attachment.SPECULAR_EMISSION_DIFFUSE_SSAO_BLOOM, specularEmissionAttachment);
    attachments.put(Attachment.DEPTH, depthBuffer);

    renderPass = new RenderPass(device);
    renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
        VK_FORMAT_R16G16B16A16_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);
    renderPass.addColorAttachment(1, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
        VK_FORMAT_R32G32B32A32_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);
    renderPass.addColorAttachment(2, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
        VK_FORMAT_R16G16B16A16_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);
    renderPass.addColorAttachment(3, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
        VK_FORMAT_R16G16B16A16_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);
    renderPass.addColorAttachment(4, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
        VK_FORMAT_R16G16B16A16_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);
    renderPass.addDepthAttachment(5, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
        VK_FORMAT_D32_SFLOAT, samples, VK_IMAGE_LAYOUT_UNDEFINED,
        VK_IMAGE_LAYOUT_GENERAL);

    renderPass.addSubpassDependency(VK_SUBPASS_EXTERNAL, 0,
        VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
        VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
        VK_ACCESS_MEMORY_READ_BIT,
        VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT |
            VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
        VK_DEPENDENCY_BY_REGION_BIT);
    renderPass.addSubpassDependency(0, VK_SUBPASS_EXTERNAL,
        VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
        VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT |
            VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
        VK_ACCESS_SHADER_READ_BIT,
        VK_DEPENDENCY_BY_REGION_BIT);
    renderPass.createSubpass();
    renderPass.createRenderPass();

    depthAttachmentCount = 1;
    colorAttachmentCount = renderPass.getAttachmentCount() - depthAttachmentCount;

    LongBuffer pImageViews = memAllocLong(renderPass.getAttachmentCount());
    pImageViews.put(0, attachments.get(Attachment.COLOR).getImageView().getHandle());
    pImageViews.put(1, attachments.get(Attachment.POSITION).getImageView().getHandle());
    pImageViews.put(2, attachments.get(Attachment.NORMAL).getImageView().getHandle());
    pImageViews.put(3, attachments.get(Attachment.SPECULAR_EMISSION_DIFFUSE_SSAO_BLOOM).getImageView().getHandle());
    pImageViews.put(4, attachments.get(Attachment.LIGHT_SCATTERING).getImageView().getHandle());
    pImageViews.put(5, attachments.get(Attachment.DEPTH).getImageView().getHandle());

    frameBuffer = new VkFrameBuffer(device, width, height, 1, pImageViews, renderPass.getHandle());
  }

}
