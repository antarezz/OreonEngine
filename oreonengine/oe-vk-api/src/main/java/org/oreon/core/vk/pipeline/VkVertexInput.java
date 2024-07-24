package org.oreon.core.vk.pipeline;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.oreon.core.model.Vertex.VertexLayout;

import lombok.Getter;

public class VkVertexInput {

  @Getter
  private VkVertexInputBindingDescription.Buffer bindingDescription;
  @Getter
  private VkVertexInputAttributeDescription.Buffer attributeDescriptions;

  private int binding;

  public VkVertexInput(VertexLayout layout) {

    switch (layout) {
      case POS2D:
        createBindingDescription(0, 1, Float.BYTES * 2);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32_SFLOAT, 0);
        break;
      case POS:
        createBindingDescription(0, 1, Float.BYTES * 3);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32B32_SFLOAT, 0);
        break;
      case POS_UV:
        createBindingDescription(0, 2, Float.BYTES * 5);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32B32_SFLOAT, 0);
        addVertexAttributeDescription(1, VK_FORMAT_R32G32_SFLOAT, Float.BYTES * 3);
        break;
      case POS2D_UV:
        createBindingDescription(0, 2, Float.BYTES * 4);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32_SFLOAT, 0);
        addVertexAttributeDescription(1, VK_FORMAT_R32G32_SFLOAT, Float.BYTES * 2);
        break;
      case POS_NORMAL:
        createBindingDescription(0, 2, Float.BYTES * 6);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32B32_SFLOAT, 0);
        addVertexAttributeDescription(1, VK_FORMAT_R32G32B32_SFLOAT, Float.BYTES * 3);
        break;
      case POS_NORMAL_UV:
        createBindingDescription(0, 3, Float.BYTES * 8);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32B32_SFLOAT, 0);
        addVertexAttributeDescription(1, VK_FORMAT_R32G32B32_SFLOAT, Float.BYTES * 3);
        addVertexAttributeDescription(2, VK_FORMAT_R32G32_SFLOAT, Float.BYTES * 6);
        break;
      case POS_NORMAL_UV_TAN_BITAN:
        createBindingDescription(0, 5, Float.BYTES * 14);
        addVertexAttributeDescription(0, VK_FORMAT_R32G32B32_SFLOAT, 0);
        addVertexAttributeDescription(1, VK_FORMAT_R32G32B32_SFLOAT, Float.BYTES * 3);
        addVertexAttributeDescription(2, VK_FORMAT_R32G32_SFLOAT, Float.BYTES * 6);
        addVertexAttributeDescription(3, VK_FORMAT_R32G32B32_SFLOAT, Float.BYTES * 8);
        addVertexAttributeDescription(4, VK_FORMAT_R32G32B32_SFLOAT, Float.BYTES * 11);
        break;
      default:
        break;
    }
  }

  private void createBindingDescription(int binding, int attributeCount, int stride) {

    this.binding = binding;

    bindingDescription = VkVertexInputBindingDescription.calloc(1)
        .binding(binding)
        .stride(stride)
        .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

    attributeDescriptions = VkVertexInputAttributeDescription.calloc(attributeCount);
  }

  private void addVertexAttributeDescription(int location, int format, int offset) {

    VkVertexInputAttributeDescription attributeDescription = VkVertexInputAttributeDescription.calloc()
        .binding(binding)
        .location(location)
        .format(format)
        .offset(offset);

    attributeDescriptions.put(location, attributeDescription);
  }

}
