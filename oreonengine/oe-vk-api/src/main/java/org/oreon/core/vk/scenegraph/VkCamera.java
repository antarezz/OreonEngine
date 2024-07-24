package org.oreon.core.vk.scenegraph;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import lombok.Getter;
import org.lwjgl.vulkan.VkDevice;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec3f;
import org.oreon.core.scenegraph.Camera;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.context.VkResources.VkDescriptorName;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.wrapper.buffer.VkUniformBuffer;
import org.oreon.core.vk.wrapper.descriptor.VkDescriptor;

@Getter
public class VkCamera extends Camera {

  private VkUniformBuffer uniformBuffer;
  private DescriptorSet descriptorSet;
  private DescriptorSetLayout descriptorSetLayout;

  public VkCamera() {

    super(new Vec3f(-179.94112f, 63.197327f, -105.08341f), new Vec3f(0.48035842f, -0.39218548f, 0.7845039f),
        new Vec3f(0.20479666f, 0.9198862f, 0.33446646f));

    // flip y-axxis for vulkan coordinate system
    getProjectionMatrix().set(1, 1, -getProjectionMatrix().get(1, 1));
  }

  @Override
  public void init() {

    final VkOreonContext context = (VkOreonContext) ContextHolder.getContext();
    VkDevice device = context.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getHandle();

    uniformBuffer = new VkUniformBuffer(
        device, context.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE)
        .getMemoryProperties(), BufferUtil.createByteBuffer(floatBuffer));

    descriptorSetLayout = new DescriptorSetLayout(device, 1);
    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
        VK_SHADER_STAGE_ALL_GRAPHICS | VK_SHADER_STAGE_COMPUTE_BIT);
    descriptorSetLayout.create();

    descriptorSet = new DescriptorSet(device,
        context.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE)
            .getDescriptorPool(Thread.currentThread().getId()).getHandle(),
        descriptorSetLayout.getHandlePointer());
    descriptorSet.updateDescriptorBuffer(uniformBuffer.getHandle(), bufferSize, 0, 0,
        VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);

    context.getResources().getDescriptors()
        .put(VkDescriptorName.CAMERA, new VkDescriptor(descriptorSet, descriptorSetLayout));
  }

  @Override
  public void update() {

    super.update();

    uniformBuffer.updateData(BufferUtil.createByteBuffer(floatBuffer));
  }

  public void shutdown() {

    uniformBuffer.destroy();
  }

}
