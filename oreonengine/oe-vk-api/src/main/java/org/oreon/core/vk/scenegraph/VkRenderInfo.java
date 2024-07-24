package org.oreon.core.vk.scenegraph;

import java.util.List;

import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class VkRenderInfo extends NodeComponent {

  private VkPipeline pipeline;
  private CommandBuffer commandBuffer;
  private VkVertexInput vertexInput;
  private ShaderPipeline shaderPipeline;
  private List<DescriptorSetLayout> descriptorSetLayouts;
  private List<DescriptorSet> descriptorSets;

  public void shutdown() {

    if (pipeline != null) {
      pipeline.destroy();
    }
    if (commandBuffer != null) {
      commandBuffer.destroy();
    }
    if (shaderPipeline != null) {
      shaderPipeline.destroy();
    }
    if (descriptorSetLayouts != null) {
      for (DescriptorSetLayout layout : descriptorSetLayouts) {
        if (layout.getHandle() != -1) {
          layout.destroy();
        }
      }
    }
    if (descriptorSets != null) {
      for (DescriptorSet set : descriptorSets) {
        if (set.getHandle() != -1) {
          set.destroy();
        }
      }
    }
  }
}
