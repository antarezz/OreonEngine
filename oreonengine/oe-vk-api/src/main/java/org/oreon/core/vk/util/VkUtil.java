/**************************************************
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 **************************************************/
package org.oreon.core.vk.util;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.vulkan.EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT;
import static org.lwjgl.vulkan.KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_SURFACE_LOST_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST;
import static org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FEATURE_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FORMAT_NOT_SUPPORTED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INCOMPATIBLE_DRIVER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_MEMORY_MAP_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS;
import static org.lwjgl.vulkan.VK10.VK_EVENT_RESET;
import static org.lwjgl.vulkan.VK10.VK_EVENT_SET;
import static org.lwjgl.vulkan.VK10.VK_INCOMPLETE;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_16_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_2_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_32_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_4_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_64_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_8_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Collection;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.oreon.core.math.Vec3f;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class VkUtil {

  public static void vkCheckResult(int err) {

    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }
  }

  /**
   * Translates a Vulkan {@code VkResult} value to a String describing the result.
   *
   * @param result the {@code VkResult} value
   * @return the result description
   */
  public static String translateVulkanResult(int result) {
    switch (result) {
      // Success codes
      case VK_SUCCESS:
        return "Command successfully completed.";
      case VK_NOT_READY:
        return "A fence or query has not yet completed.";
      case VK_TIMEOUT:
        return "A wait operation has not completed in the specified time.";
      case VK_EVENT_SET:
        return "An event is signaled.";
      case VK_EVENT_RESET:
        return "An event is unsignaled.";
      case VK_INCOMPLETE:
        return "A return array was too small for the result.";
      case VK_SUBOPTIMAL_KHR:
        return "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully.";

      // Error codes
      case VK_ERROR_OUT_OF_HOST_MEMORY:
        return "A host memory allocation has failed.";
      case VK_ERROR_OUT_OF_DEVICE_MEMORY:
        return "A device memory allocation has failed.";
      case VK_ERROR_INITIALIZATION_FAILED:
        return "Initialization of an object could not be completed for implementation-specific reasons.";
      case VK_ERROR_DEVICE_LOST:
        return "The logical or physical device has been lost.";
      case VK_ERROR_MEMORY_MAP_FAILED:
        return "Mapping of a memory object has failed.";
      case VK_ERROR_LAYER_NOT_PRESENT:
        return "A requested layer is not present or could not be loaded.";
      case VK_ERROR_EXTENSION_NOT_PRESENT:
        return "A requested extension is not supported.";
      case VK_ERROR_FEATURE_NOT_PRESENT:
        return "A requested feature is not supported.";
      case VK_ERROR_INCOMPATIBLE_DRIVER:
        return "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons.";
      case VK_ERROR_TOO_MANY_OBJECTS:
        return "Too many objects of the type have already been created.";
      case VK_ERROR_FORMAT_NOT_SUPPORTED:
        return "A requested format is not supported on this device.";
      case VK_ERROR_SURFACE_LOST_KHR:
        return "A surface is no longer available.";
      case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
        return "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API.";
      case VK_ERROR_OUT_OF_DATE_KHR:
        return
            "A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the "
                + "swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue"
                + "presenting to the surface.";
      case VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
        return
            "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an"
                + " image.";
      case VK_ERROR_VALIDATION_FAILED_EXT:
        return "A validation layer found an error.";
      default:
        return String.format("%s [%d]", "Unknown", Integer.valueOf(result));
    }
  }

  public static PointerBuffer getValidationLayerNames(boolean validation, ByteBuffer[] layers) {

    PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
    for (int i = 0; validation && i < layers.length; i++) {
      ppEnabledLayerNames.put(layers[i]);
    }
    ppEnabledLayerNames.flip();

    return ppEnabledLayerNames;
  }

  public static VkClearValue getClearValueColor(Vec3f clearColor) {

    VkClearValue clearValues = VkClearValue.calloc();
    clearValues.color()
        .float32(0, clearColor.getX())
        .float32(1, clearColor.getY())
        .float32(2, clearColor.getZ())
        .float32(3, 1.0f);

    return clearValues;
  }

  public static VkClearColorValue getClearColorValue() {

    VkClearColorValue clearValues = VkClearColorValue.calloc();
    clearValues
        .float32(0, 0.0f)
        .float32(1, 0.0f)
        .float32(2, 0.0f)
        .float32(3, 1.0f);

    return clearValues;
  }

  public static VkClearValue getClearValueDepth() {

    VkClearValue clearValues = VkClearValue.calloc();
    clearValues.depthStencil()
        .depth(1.0f);

    return clearValues;
  }

  public static int getSampleCountBit(int samples) {

    int sampleCountBit = 0;

    switch (samples) {
      case 1:
        sampleCountBit = VK_SAMPLE_COUNT_1_BIT;
        break;
      case 2:
        sampleCountBit = VK_SAMPLE_COUNT_2_BIT;
        break;
      case 4:
        sampleCountBit = VK_SAMPLE_COUNT_4_BIT;
        break;
      case 8:
        sampleCountBit = VK_SAMPLE_COUNT_8_BIT;
        break;
      case 16:
        sampleCountBit = VK_SAMPLE_COUNT_16_BIT;
        break;
      case 32:
        sampleCountBit = VK_SAMPLE_COUNT_32_BIT;
        break;
      case 64:
        sampleCountBit = VK_SAMPLE_COUNT_64_BIT;
        break;
    }

    if (sampleCountBit == 0) {
      log.error("Multisamplecount: " + samples + ". Allowed numbers [1,2,4,8,16,32,64]");
    }

    return sampleCountBit;
  }

  public static long[] createLongArray(List<DescriptorSet> descriptorSets) {

    long[] descriptorSetHandles = new long[descriptorSets.size()];

    for (int i = 0; i < descriptorSets.size(); i++) {

      descriptorSetHandles[i] = descriptorSets.get(i).getHandle();
    }

    return descriptorSetHandles;
  }

  public static long[] createLongArray(DescriptorSet descriptorSet) {

    long[] descriptorSetHandles = new long[1];
    descriptorSetHandles[0] = descriptorSet.getHandle();

    return descriptorSetHandles;
  }

  public static LongBuffer createLongBuffer(List<DescriptorSetLayout> descriptorSetLayouts) {

    if (descriptorSetLayouts.size() == 0) {
      log.error("createLongBuffer: descriptorSetLayouts empty");
    }

    LongBuffer descriptorSetLayoutsBuffer = memAllocLong(descriptorSetLayouts.size());

    for (DescriptorSetLayout layout : descriptorSetLayouts) {

      descriptorSetLayoutsBuffer.put(layout.getHandle());
    }
    descriptorSetLayoutsBuffer.flip();

    return descriptorSetLayoutsBuffer;
  }

  public static PointerBuffer createPointerBuffer(List<CommandBuffer> commandBuffers) {

    if (commandBuffers.size() == 0) {
      log.error("createPointerBuffer: commandBuffers empty");
    }

    PointerBuffer cmdBuffersPointer = memAllocPointer(commandBuffers.size());

    for (CommandBuffer cmdBuffer : commandBuffers) {
      cmdBuffersPointer.put(cmdBuffer.getHandlePointer());
    }

    cmdBuffersPointer.flip();

    return cmdBuffersPointer;
  }

  public static PointerBuffer createPointerBuffer(Collection<CommandBuffer> commandBuffers) {

    if (commandBuffers.size() == 0) {
      log.info("createPointerBuffer: commandBuffers empty");
      return null;
    }

    PointerBuffer cmdBuffersPointer = memAllocPointer(commandBuffers.size());

    for (CommandBuffer cmdBuffer : commandBuffers) {
      cmdBuffersPointer.put(cmdBuffer.getHandlePointer());
    }

    cmdBuffersPointer.flip();

    return cmdBuffersPointer;
  }

}
