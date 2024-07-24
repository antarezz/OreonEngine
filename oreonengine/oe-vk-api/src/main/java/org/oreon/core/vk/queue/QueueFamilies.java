package org.oreon.core.vk.queue;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_SPARSE_BINDING_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.oreon.core.vk.util.VkUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class QueueFamilies {

  private List<QueueFamily> queueFamilies;

  public QueueFamilies(VkPhysicalDevice physicalDevice, long surface) {

    queueFamilies = new ArrayList<>();

    IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
    int queueCount = pQueueFamilyPropertyCount.get(0);

    VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps);

    log.info("Available Queues: " + queueCount);

    IntBuffer supportsPresent = memAllocInt(queueCount);

    for (int i = 0; i < queueCount; i++) {

      supportsPresent.position(i);
      supportsPresent.put(i, 0);

      int flags = queueProps.get(i).queueFlags();
      int count = queueProps.get(i).queueCount();

      // check if surface exists
      if (surface != -1) {
        int err = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice,
            i,
            surface,
            supportsPresent);
        if (err != VK_SUCCESS) {
          throw new AssertionError("Failed to physical device surface support: " + VkUtil.translateVulkanResult(err));
        }
      }

      log.info("Index:" + i + " flags:" + flags + " count:" + count + " presentation:" + supportsPresent.get(i));

      QueueFamily queueFamily = new QueueFamily();
      queueFamily.setIndex(i);
      queueFamily.setFlags(flags);
      queueFamily.setCount(count);
      queueFamily.setPresentFlag(supportsPresent.get(i));

      queueFamilies.add(queueFamily);
    }

    memFree(pQueueFamilyPropertyCount);
    queueProps.free();
  }

  public QueueFamily getGraphicsQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if ((queueFamily.getFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with graphics support found");
  }

  public QueueFamily getComputeQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if ((queueFamily.getFlags() & VK_QUEUE_COMPUTE_BIT) != 0) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with compute support found");
  }

  public QueueFamily getTransferQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if ((queueFamily.getFlags() & VK_QUEUE_TRANSFER_BIT) != 0) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with transfer support found");
  }

  public QueueFamily getSparseBindingQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if ((queueFamily.getFlags() & VK_QUEUE_SPARSE_BINDING_BIT) != 0) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with sparse binding support found");
  }

  public QueueFamily getPresentationQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if (queueFamily.getPresentFlag() == VK_TRUE) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with presentation support found");
  }

  public QueueFamily getGraphicsAndPresentationQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {

      if ((queueFamily.getFlags() & VK_QUEUE_GRAPHICS_BIT) != 0
          && queueFamily.getPresentFlag() == VK_TRUE) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with both graphics and presentation support found");
  }

  public QueueFamily getComputeExclusiveQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if (queueFamily.getFlags() == VK_QUEUE_COMPUTE_BIT) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue with compute exclusive support found");
  }

  public QueueFamily getTransferExclusiveQueueFamily() throws Exception {

    for (QueueFamily queueFamily : queueFamilies) {
      if (queueFamily.getFlags() == VK_QUEUE_TRANSFER_BIT) {
        return queueFamily;
      }
    }
    throw new Exception("No Queue transfer exclusive support found");
  }

}
