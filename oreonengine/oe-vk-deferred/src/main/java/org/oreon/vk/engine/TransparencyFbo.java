package org.oreon.vk.engine;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.vk.framebuffer.FrameBufferColorAttachment;
import org.oreon.core.vk.framebuffer.FrameBufferDepthAttachment;
import org.oreon.core.vk.framebuffer.VkFrameBuffer;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.pipeline.RenderPass;
import org.oreon.core.vk.wrapper.image.VkImageBundle;

public class TransparencyFbo extends VkFrameBufferObject{
	
	public TransparencyFbo(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties) {
		
		width = BaseOreonContext.getConfig().getFrameWidth();
		height = BaseOreonContext.getConfig().getFrameHeight();
		
		VkImageBundle albedoAttachment = new FrameBufferColorAttachment(device, memoryProperties,
				width, height, VK_FORMAT_R16G16B16A16_SFLOAT, 1);
		
		VkImageBundle alphaAttachment = new FrameBufferColorAttachment(device, memoryProperties,
				width, height, VK_FORMAT_R16G16B16A16_SFLOAT, 1);

		VkImageBundle lightScatteringAttachment = new FrameBufferColorAttachment(device, memoryProperties,
				width, height, VK_FORMAT_R16G16B16A16_SFLOAT, 1);
		
		VkImageBundle depthBuffer = new FrameBufferDepthAttachment(device, memoryProperties,
				width, height, VK_FORMAT_D32_SFLOAT, 1);
		
		attachments.put(Attachment.COLOR, albedoAttachment);
		attachments.put(Attachment.ALPHA, alphaAttachment);
		attachments.put(Attachment.LIGHT_SCATTERING, lightScatteringAttachment);
		attachments.put(Attachment.DEPTH, depthBuffer);
		
		renderPass = new RenderPass(device);
		renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
				VK_FORMAT_R16G16B16A16_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		renderPass.addColorAttachment(1, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
				VK_FORMAT_R16G16B16A16_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		renderPass.addColorAttachment(2, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
				VK_FORMAT_R16G16B16A16_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		renderPass.addDepthAttachment(3, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
				VK_FORMAT_D32_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		
		renderPass.addSubpassDependency(VK_SUBPASS_EXTERNAL, 0,
				VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
				VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
				VK_ACCESS_MEMORY_READ_BIT,
				VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
				VK_DEPENDENCY_BY_REGION_BIT);
		renderPass.addSubpassDependency(0, VK_SUBPASS_EXTERNAL,
				VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
				VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
				VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
				VK_ACCESS_MEMORY_READ_BIT,
				VK_DEPENDENCY_BY_REGION_BIT);
		renderPass.createSubpass();
		renderPass.createRenderPass();

		depthAttachmentCount = 1;
		colorAttachmentCount = renderPass.getAttachmentCount()-depthAttachmentCount;

		LongBuffer pImageViews = memAllocLong(renderPass.getAttachmentCount());
		pImageViews.put(0, attachments.get(Attachment.COLOR).getImageView().getHandle());
		pImageViews.put(1, attachments.get(Attachment.ALPHA).getImageView().getHandle());
		pImageViews.put(2, attachments.get(Attachment.LIGHT_SCATTERING).getImageView().getHandle());
		pImageViews.put(3, attachments.get(Attachment.DEPTH).getImageView().getHandle());
		
		frameBuffer = new VkFrameBuffer(device, width, height, 1, pImageViews, renderPass.getHandle());
	}

}
