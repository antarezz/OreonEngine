package org.oreon.core.vk.wrapper.pipeline;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;

public class GraphicsPipeline extends VkPipeline {

  public GraphicsPipeline(VkDevice device, ShaderPipeline shaderPipeline,
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
    setColorBlendState();
    setDepthAndStencilTest(true);
    setDynamicState();
    setLayout(layout);
    createGraphicsPipeline(shaderPipeline, renderPass);
  }

  public GraphicsPipeline(VkDevice device, ShaderPipeline shaderPipeline,
      VkVertexInput vertexInput, int topology, LongBuffer layout, int width, int height,
      long renderPass, int colorAttachmentCount, int samples) {

    super(device);
    setVertexInput(vertexInput);
    setInputAssembly(topology);
    setViewportAndScissor(width, height);
    setRasterizer();
    setMultisamplingState(samples);
    for (int i = 0; i < colorAttachmentCount; i++) {
      addColorBlendAttachment();
    }
    setColorBlendState();
    setDepthAndStencilTest(true);
    setDynamicState();
    setLayout(layout);
    createGraphicsPipeline(shaderPipeline, renderPass);
  }

}
