package org.oreon.core.vk.pipeline;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_STENCIL_OP_KEEP;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateComputePipelines;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VkPipeline {

  @Getter
  private long handle;
  @Getter
  private long layoutHandle;

  private final VkDevice device;

  private VkPipelineVertexInputStateCreateInfo vertexInputState;
  private VkPushConstantRange.Buffer pushConstantRange;
  private VkPipelineInputAssemblyStateCreateInfo inputAssembly;
  private VkPipelineViewportStateCreateInfo viewportAndScissorState;
  private VkPipelineRasterizationStateCreateInfo rasterizer;
  private VkPipelineMultisampleStateCreateInfo multisamplingState;
  private VkPipelineColorBlendStateCreateInfo colorBlending;
  private VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachmentStates;
  private VkPipelineDepthStencilStateCreateInfo depthStencilState;
  private VkPipelineDynamicStateCreateInfo dynamicState;
  private VkPipelineTessellationStateCreateInfo tessellationState;
  private VkViewport.Buffer viewport;
  private VkRect2D.Buffer scissor;
  private IntBuffer pDynamicStates;

  public List<VkPipelineColorBlendAttachmentState> colorBlendAttachments = new ArrayList<>();

  public VkPipeline(VkDevice device) {

    this.device = device;
  }

  public void createGraphicsPipeline(ShaderPipeline shaderPipeline, long renderPass) {

    VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
        .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
        .pStages(shaderPipeline.getStages())
        .pVertexInputState(vertexInputState)
        .pInputAssemblyState(inputAssembly)
        .pViewportState(viewportAndScissorState)
        .pRasterizationState(rasterizer)
        .pMultisampleState(multisamplingState)
        .pDepthStencilState(depthStencilState)
        .pColorBlendState(colorBlending)
        .pDynamicState(null)
        .pTessellationState(tessellationState)
        .layout(layoutHandle)
        .renderPass(renderPass)
        .subpass(0)
        .basePipelineHandle(VK_NULL_HANDLE)
        .basePipelineIndex(-1);

    LongBuffer pPipelines = memAllocLong(1);
    int err = vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipelines);

    handle = pPipelines.get(0);

    vertexInputState.free();
    inputAssembly.free();
    viewportAndScissorState.free();
    rasterizer.free();
    multisamplingState.free();
    colorBlending.free();
    colorBlendAttachmentStates.free();
    depthStencilState.free();
    dynamicState.free();
    viewport.free();
    scissor.free();
    memFree(pDynamicStates);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create pipeline: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void createComputePipeline(ShaderModule shader) {

    VkComputePipelineCreateInfo.Buffer pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1)
        .sType(VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
        .stage(shader.getShaderStageInfo())
        .layout(layoutHandle);

    LongBuffer pPipelines = memAllocLong(1);
    VkUtil.vkCheckResult(vkCreateComputePipelines(device,
        VK_NULL_HANDLE, pipelineCreateInfo, null, pPipelines));

    handle = pPipelines.get(0);

    pipelineCreateInfo.free();
  }

  public void setLayout(LongBuffer pLayouts) {

    VkPipelineLayoutCreateInfo pipelineLayout = VkPipelineLayoutCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
        .pNext(0)
        .pSetLayouts(pLayouts)
        .pPushConstantRanges(pushConstantRange);

    LongBuffer pPipelineLayout = memAllocLong(1);
    int err = vkCreatePipelineLayout(device, pipelineLayout, null, pPipelineLayout);

    layoutHandle = pPipelineLayout.get(0);

    memFree(pPipelineLayout);
    pipelineLayout.free();
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create pipeline layout: " + VkUtil.translateVulkanResult(err));
    }
  }

  public void setVertexInput(VkVertexInput vertexInput) {

    vertexInputState = VkPipelineVertexInputStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
        .pNext(0)
        .pVertexBindingDescriptions(vertexInput.getBindingDescription())
        .pVertexAttributeDescriptions(vertexInput.getAttributeDescriptions());
  }

  public void setPushConstantsRange(int stageFlags, int size) {

    pushConstantRange = VkPushConstantRange.calloc(1)
        .stageFlags(stageFlags)
        .size(size)
        .offset(0);
  }

  public void setInputAssembly(int topology) {

    inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
        .topology(topology)
        .primitiveRestartEnable(false);
  }

  public void setViewportAndScissor(int width, int height) {

    viewport = VkViewport.calloc(1)
        .x(0)
        .y(0)
        .width(width)
        .height(height)
        .minDepth(0.0f)
        .maxDepth(1.0f);

    scissor = VkRect2D.calloc(1);
    scissor.extent().set(width, height);
    scissor.offset().set(0, 0);

    viewportAndScissorState = VkPipelineViewportStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
        .viewportCount(1)
        .pViewports(viewport)
        .scissorCount(1)
        .pScissors(scissor);
  }

  public void setRasterizer() {

    rasterizer = VkPipelineRasterizationStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
        .polygonMode(VK_POLYGON_MODE_FILL)
        .cullMode(VK_CULL_MODE_NONE)
        .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
        .rasterizerDiscardEnable(false)
        .lineWidth(1.0f)
        .depthClampEnable(false)
        .depthBiasEnable(false)
        .depthBiasConstantFactor(0)
        .depthBiasSlopeFactor(0)
        .depthBiasClamp(0);
  }

  public void setMultisamplingState(int samples) {

    multisamplingState = VkPipelineMultisampleStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
        .sampleShadingEnable(false)
        .rasterizationSamples(VkUtil.getSampleCountBit(samples))
        .pSampleMask(null)
        .minSampleShading(1)
        .alphaToCoverageEnable(false)
        .alphaToOneEnable(false);
  }

  public void addColorBlendAttachment() {

    VkPipelineColorBlendAttachmentState colorWriteMask = VkPipelineColorBlendAttachmentState.calloc()
        .blendEnable(false)
        .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT
            | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);

    colorBlendAttachments.add(colorWriteMask);
  }

  public void setColorBlendState() {

    colorBlendAttachmentStates =
        VkPipelineColorBlendAttachmentState.calloc(colorBlendAttachments.size());

    for (VkPipelineColorBlendAttachmentState colorBlendAttachment : colorBlendAttachments) {
      colorBlendAttachment.blendEnable(false);
      colorBlendAttachmentStates.put(colorBlendAttachment);
    }
    colorBlendAttachmentStates.flip();

    colorBlending = VkPipelineColorBlendStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
        .logicOpEnable(false)
        .pAttachments(colorBlendAttachmentStates);

    for (VkPipelineColorBlendAttachmentState colorBlendAttachment : colorBlendAttachments) {
      colorBlendAttachment.free();
    }
  }

  public void setColorBlendState(int srcColorBlendFactor, int dstColorBlendFactor,
      int srcAlphaBlendFactor, int dstAlphaBlendFactor, int colorBlendOp, int alphaBlendOp) {

    colorBlendAttachmentStates =
        VkPipelineColorBlendAttachmentState.calloc(colorBlendAttachments.size());

    for (VkPipelineColorBlendAttachmentState colorBlendAttachment : colorBlendAttachments) {
      colorBlendAttachment
          .blendEnable(true)
          .srcColorBlendFactor(srcColorBlendFactor)
          .dstColorBlendFactor(dstColorBlendFactor)
          .colorBlendOp(colorBlendOp)
          .srcAlphaBlendFactor(srcAlphaBlendFactor)
          .dstAlphaBlendFactor(dstAlphaBlendFactor)
          .alphaBlendOp(alphaBlendOp)
          .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT
              | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
      colorBlendAttachmentStates.put(colorBlendAttachment);
    }
    colorBlendAttachmentStates.flip();

    colorBlending = VkPipelineColorBlendStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
        .logicOpEnable(false)
        .pAttachments(colorBlendAttachmentStates);

    for (VkPipelineColorBlendAttachmentState colorBlendAttachment : colorBlendAttachments) {
      colorBlendAttachment.free();
    }
  }

  public void setDepthAndStencilTest(boolean depthTestEnable) {

    depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
        .depthTestEnable(depthTestEnable)
        .depthWriteEnable(true)
        .depthCompareOp(VK_COMPARE_OP_LESS)
        .depthBoundsTestEnable(false)
        .stencilTestEnable(false);
    depthStencilState.back()
        .failOp(VK_STENCIL_OP_KEEP)
        .passOp(VK_STENCIL_OP_KEEP)
        .compareOp(VK_COMPARE_OP_ALWAYS);
    depthStencilState.front(depthStencilState.back());
  }

  public void setDynamicState() {

    pDynamicStates = memAllocInt(2);
    pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT);
    pDynamicStates.put(VK_DYNAMIC_STATE_SCISSOR);
    pDynamicStates.flip();

    dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
        .pDynamicStates(pDynamicStates);
  }

  public void setTessellationState(int patchControlPoints) {

    tessellationState = VkPipelineTessellationStateCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO)
        .flags(0)
        .patchControlPoints(patchControlPoints);
  }

  public void destroy() {

    vkDestroyPipelineLayout(device, layoutHandle, null);
    vkDestroyPipeline(device, handle, null);
  }

}
