package org.oreon.core.vk.scenegraph;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.NodeComponent;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.command.CommandPool;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class VkMeshData extends NodeComponent {

  private VkBuffer vertexBufferObject;
  private ByteBuffer vertexBuffer;
  private int vertexCount;

  private VkBuffer indexBufferObject;
  private ByteBuffer indexBuffer;
  private int indexCount;

  public VkMeshData(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
      CommandPool commandPool, VkQueue queue, Mesh mesh, VertexLayout vertexLayout) {

    ByteBuffer vertexByteBuffer = BufferUtil.createByteBuffer(mesh.getVertices(), vertexLayout);
    ByteBuffer indexByteBuffer = BufferUtil.createByteBuffer(mesh.getIndices());
    vertexCount = mesh.getVertices().length;
    indexCount = mesh.getIndices().length;

    vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        device, memoryProperties,
        commandPool.getHandle(), queue,
        vertexByteBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

    indexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        device, memoryProperties,
        commandPool.getHandle(), queue,
        indexByteBuffer, VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
  }

  public void shutdown() {

    if (vertexBufferObject != null) {
      vertexBufferObject.destroy();
    }
    if (indexBufferObject != null) {
      indexBufferObject.destroy();
    }
  }

}
