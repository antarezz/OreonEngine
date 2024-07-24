package org.oreon.core.vk.pipeline;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import lombok.Getter;

public class ShaderPipeline {

  @Getter
  private VkPipelineShaderStageCreateInfo.Buffer stages;
  private List<ShaderModule> shaderStages = new ArrayList<ShaderModule>();

  private final VkDevice device;

  public ShaderPipeline(VkDevice device) {

    this.device = device;
  }

  public void createShaderPipeline() {

    stages = VkPipelineShaderStageCreateInfo.calloc(shaderStages.size());

    for (ShaderModule shaderStage : shaderStages) {
      stages.put(shaderStage.getShaderStageInfo());
    }

    stages.flip();
  }

  public void createVertexShader(String filePath) {

    shaderStages.add(new ShaderModule(device, filePath, VK_SHADER_STAGE_VERTEX_BIT));
  }

  public void createTessellationControlShader(String filePath) {

    shaderStages.add(new ShaderModule(device, filePath, VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT));
  }

  public void createTessellationEvaluationShader(String filePath) {

    shaderStages.add(new ShaderModule(device, filePath, VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT));
  }

  public void createGeometryShader(String filePath) {

    shaderStages.add(new ShaderModule(device, filePath, VK_SHADER_STAGE_GEOMETRY_BIT));
  }

  public void createFragmentShader(String filePath) {

    shaderStages.add(new ShaderModule(device, filePath, VK_SHADER_STAGE_FRAGMENT_BIT));
  }

  public void addShaderModule(ShaderModule shaderModule) {

    shaderStages.add(shaderModule);
  }

  public void destroy() {

    stages.free();

    for (ShaderModule shaderModule : shaderStages) {
      if (shaderModule.getHandle() != -1) {
        shaderModule.destroy();
      }
    }
  }

}
