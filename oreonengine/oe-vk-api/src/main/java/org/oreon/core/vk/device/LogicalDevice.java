package org.oreon.core.vk.device;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.core.vk.command.CommandPool;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public class LogicalDevice {

  private VkDevice handle;
  private VkQueue graphicsQueue;
  private VkQueue computeQueue;
  private VkQueue transferQueue;

  private int graphicsQueueFamilyIndex;
  private int computeQueueFamilyIndex;
  private int transferQueueFamilyIndex;

  private HashMap<Long, DescriptorPool> descriptorPools;
  private HashMap<Long, CommandPool> graphicsCommandPools;
  private HashMap<Long, CommandPool> computeCommandPools;
  private HashMap<Long, CommandPool> transferCommandPools;

  public LogicalDevice(PhysicalDevice physicalDevice, float priority) {

    descriptorPools = new HashMap<Long, DescriptorPool>();
    graphicsCommandPools = new HashMap<Long, CommandPool>();
    computeCommandPools = new HashMap<Long, CommandPool>();
    transferCommandPools = new HashMap<Long, CommandPool>();

    FloatBuffer pQueuePriorities = memAllocFloat(1).put(priority);
    pQueuePriorities.flip();

    int createInfoCount = 3;
    try {
      graphicsQueueFamilyIndex = physicalDevice.getQueueFamilies().getGraphicsAndPresentationQueueFamily().getIndex();
    } catch (Exception e1) {
      throw new AssertionError(
          "no graphics and presentation queue available on device: " + physicalDevice.getProperties()
              .deviceNameString());
    }
    try {
      computeQueueFamilyIndex = physicalDevice.getQueueFamilies().getComputeExclusiveQueueFamily().getIndex();
    } catch (Exception e) {
      log.info("No compute exclusive queue available on device: " + physicalDevice.getProperties().deviceNameString());
      createInfoCount--;
      try {
        computeQueueFamilyIndex = physicalDevice.getQueueFamilies().getComputeQueueFamily().getIndex();
      } catch (Exception e1) {
        throw new AssertionError(
            "no compute queue available on device: " + physicalDevice.getProperties().deviceNameString());
      }
    }
    try {
      transferQueueFamilyIndex = physicalDevice.getQueueFamilies().getTransferExclusiveQueueFamily().getIndex();
    } catch (Exception e) {
      log.info("No transfer exclusive queue available on device: " + physicalDevice.getProperties().deviceNameString());
      createInfoCount--;
      try {
        transferQueueFamilyIndex = physicalDevice.getQueueFamilies().getTransferQueueFamily().getIndex();
      } catch (Exception e1) {
        throw new AssertionError(
            "no transfer queue available on device: " + physicalDevice.getProperties().deviceNameString());
      }
    }

    VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(createInfoCount);

    VkDeviceQueueCreateInfo.Buffer graphicsQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
        .queueFamilyIndex(graphicsQueueFamilyIndex)
        .pQueuePriorities(pQueuePriorities);

    VkDeviceQueueCreateInfo.Buffer computeQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1);
    VkDeviceQueueCreateInfo.Buffer transferQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1);

    queueCreateInfos.put(graphicsQueueCreateInfo);

    if (graphicsQueueFamilyIndex != computeQueueFamilyIndex) {

      computeQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
          .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
          .queueFamilyIndex(computeQueueFamilyIndex)
          .pQueuePriorities(pQueuePriorities);

      queueCreateInfos.put(computeQueueCreateInfo);
    }

    if (graphicsQueueFamilyIndex != transferQueueFamilyIndex) {

      transferQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
          .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
          .queueFamilyIndex(transferQueueFamilyIndex)
          .pQueuePriorities(pQueuePriorities);

      queueCreateInfos.put(transferQueueCreateInfo);
    }

    queueCreateInfos.flip();

    PointerBuffer extensions = memAllocPointer(1);
    ByteBuffer VK_KHR_SWAPCHAIN_EXTENSION = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
    extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
    extensions.flip();

    physicalDevice.checkDeviceExtensionsSupport(extensions);

    VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc()
        .tessellationShader(true)
        .geometryShader(true)
        .shaderClipDistance(true)
        .samplerAnisotropy(true)
        .shaderStorageImageExtendedFormats(true)
        .fillModeNonSolid(true)
        .shaderStorageImageMultisample(true);

    VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
        .pNext(VK_NULL_HANDLE)
        .pQueueCreateInfos(queueCreateInfos)
        .ppEnabledExtensionNames(extensions)
        .pEnabledFeatures(physicalDeviceFeatures);

    PointerBuffer pDevice = memAllocPointer(1);
    int err = vkCreateDevice(physicalDevice.getHandle(), deviceCreateInfo, null, pDevice);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create device: " + VkUtil.translateVulkanResult(err));
    }

    handle = new VkDevice(pDevice.get(0), physicalDevice.getHandle(), deviceCreateInfo);

    // create Queues and CommandPools
    graphicsQueue = getDeviceQueue(graphicsQueueFamilyIndex, 0);
    graphicsCommandPools.put(Thread.currentThread().getId(),
        new CommandPool(handle, graphicsQueueFamilyIndex));

    if (graphicsQueueFamilyIndex == computeQueueFamilyIndex) {
      computeQueue = graphicsQueue;
      computeCommandPools.put(Thread.currentThread().getId(),
          graphicsCommandPools.get(Thread.currentThread().getId()));
    } else {
      computeQueue = getDeviceQueue(computeQueueFamilyIndex, 0);
      computeCommandPools.put(Thread.currentThread().getId(),
          new CommandPool(handle, computeQueueFamilyIndex));
    }
    if (graphicsQueueFamilyIndex == transferQueueFamilyIndex) {
      transferQueue = graphicsQueue;
      transferCommandPools.put(Thread.currentThread().getId(),
          graphicsCommandPools.get(Thread.currentThread().getId()));
    } else {
      transferQueue = getDeviceQueue(transferQueueFamilyIndex, 0);
      transferCommandPools.put(Thread.currentThread().getId(),
          new CommandPool(handle, transferQueueFamilyIndex));
    }

    deviceCreateInfo.free();
    queueCreateInfos.free();
    graphicsQueueCreateInfo.free();
    computeQueueCreateInfo.free();
    transferQueueCreateInfo.free();
    memFree(pDevice);
    memFree(pQueuePriorities);
    memFree(VK_KHR_SWAPCHAIN_EXTENSION);
    memFree(extensions);
  }

  public VkQueue getDeviceQueue(int queueFamilyIndex, int queueIndex) {

    PointerBuffer pQueue = memAllocPointer(1);
    vkGetDeviceQueue(handle, queueFamilyIndex, queueIndex, pQueue);
    long queue = pQueue.get(0);
    memFree(pQueue);
    return new VkQueue(queue, handle);
  }

  public void destroy() {

    for (long key : descriptorPools.keySet()) {
      descriptorPools.get(key).destroy();
    }

    for (Entry<Long, CommandPool> entry : graphicsCommandPools.entrySet()) {
      entry.getValue().destroy();
    }
    if (graphicsQueueFamilyIndex != computeQueueFamilyIndex) {
      for (Entry<Long, CommandPool> entry : computeCommandPools.entrySet()) {
        entry.getValue().destroy();
      }
    }
    if (graphicsQueueFamilyIndex != transferQueueFamilyIndex) {
      for (Entry<Long, CommandPool> entry : transferCommandPools.entrySet()) {
        entry.getValue().destroy();
      }
    }
    vkDestroyDevice(handle, null);
  }

  public void addDescriptorPool(long threadId, DescriptorPool descriptorPool) {

    descriptorPools.put(threadId, descriptorPool);
  }

  public DescriptorPool getDescriptorPool(long threadId) {

    return descriptorPools.get(threadId);
  }

  public CommandPool getGraphicsCommandPool(long threadId) {

    return graphicsCommandPools.get(threadId);
  }

  public CommandPool getComputeCommandPool(long threadId) {

    return computeCommandPools.get(threadId);
  }

  public CommandPool getTransferCommandPool(long threadId) {

    return transferCommandPools.get(threadId);
  }

}
