package org.oreon.core.vk.wrapper.pipeline;

import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;

public class GraphicsPipelineAlphaBlend extends VkPipeline {

  public GraphicsPipelineAlphaBlend(VkDevice device, ShaderPipeline shaderPipeline,
      VkVertexInput vertexInput, int topology, LongBuffer layout, int width, int height,
      long renderPass, int colorAttachmentCount, int samples,
      int srcAlphaBlendFactor, int dstAlphaBlendFactor, int alphaBlendOp) {

    super(device);
    setVertexInput(vertexInput);
    setInputAssembly(topology);
    setViewportAndScissor(width, height);
    setRasterizer();
    setMultisamplingState(samples);
    for (int i = 0; i < colorAttachmentCount; i++) {
      addColorBlendAttachment();
    }
    setColorBlendState(VK_BLEND_FACTOR_SRC_ALPHA, VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
        VK_BLEND_OP_ADD, VK_BLEND_FACTOR_ONE, VK_BLEND_FACTOR_ZERO, VK_BLEND_OP_ADD);
    setDepthAndStencilTest(true);
    setDynamicState();
    setLayout(layout);
    createGraphicsPipeline(shaderPipeline, renderPass);
  }

  public GraphicsPipelineAlphaBlend(VkDevice device, ShaderPipeline shaderPipeline,
      VkVertexInput vertexInput, int topology, LongBuffer layout, int width, int height,
      long renderPass, int colorAttachmentCount, int samples,
      int pushConstantRange, int pushConstantStageFlags) {

    super(device);
    setVertexInput(vertexInput);
    setInputAssembly(topology);
    setPushConstantsRange(pushConstantStageFlags, pushConstantRange);
    setViewportAndScissor(width, height);
    setRasterizer();
    setMultisamplingState(samples);
    for (int i = 0; i < colorAttachmentCount; i++) {
      addColorBlendAttachment();
    }
    setColorBlendState(VK_BLEND_FACTOR_SRC_ALPHA, VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
        VK_BLEND_FACTOR_ONE, VK_BLEND_FACTOR_ZERO, VK_BLEND_OP_ADD, VK_BLEND_OP_ADD);
    setDepthAndStencilTest(true);
    setDynamicState();
    setLayout(layout);
    createGraphicsPipeline(shaderPipeline, renderPass);
  }

}
