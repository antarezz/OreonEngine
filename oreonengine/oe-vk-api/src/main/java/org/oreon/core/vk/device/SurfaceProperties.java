package org.oreon.core.vk.device;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

@Getter
public class SurfaceProperties {

  private VkSurfaceCapabilitiesKHR surfaceCapabilities;
  private VkSurfaceFormatKHR.Buffer surfaceFormats;
  private IntBuffer presentModes;

  public SurfaceProperties(VkPhysicalDevice physicalDevice, long surface) {

    surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
    int err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfaceCapabilities);
    if (err != VK_SUCCESS) {
      throw new AssertionError(
          "Failed to get physical device surface capabilities: " + VkUtil.translateVulkanResult(err));
    }

    IntBuffer pFormatCount = memAllocInt(1);
    err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, null);
    int formatCount = pFormatCount.get(0);
    if (err != VK_SUCCESS) {
      throw new AssertionError(
          "Failed to query number of physical device surface formats: " + VkUtil.translateVulkanResult(err));
    }

    surfaceFormats = VkSurfaceFormatKHR.calloc(formatCount);
    err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, surfaceFormats);
    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to query physical device surface formats: " + VkUtil.translateVulkanResult(err));
    }

    IntBuffer pPresentModeCount = memAllocInt(1);
    err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null);
    int presentModeCount = pPresentModeCount.get(0);
    if (err != VK_SUCCESS) {
      throw new AssertionError(
          "Failed to get number of physical device surface presentation modes: " + VkUtil.translateVulkanResult(err));
    }

    presentModes = memAllocInt(presentModeCount);
    err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, presentModes);
    if (err != VK_SUCCESS) {
      throw new AssertionError(
          "Failed to get physical device surface presentation modes: " + VkUtil.translateVulkanResult(err));
    }

    memFree(pPresentModeCount);
    memFree(pFormatCount);
  }

  public void checkVkSurfaceFormatKHRSupport(int format, int colorSpace) {

    if (surfaceFormats.get(0).format() == VK_FORMAT_UNDEFINED) {
      // surface has no format restrictions
      return;
    }

    for (int i = 0; i < surfaceFormats.limit(); i++) {

      if (surfaceFormats.get(i).format() == format
          && surfaceFormats.get(i).colorSpace() == colorSpace) {
        return;
      }
    }
    throw new AssertionError("Desired format and colorspace not supported");
  }

  public boolean checkPresentationModeSupport(int presentMode) {

    for (int i = 0; i < presentModes.limit(); i++) {

      if (presentModes.get(i) == presentMode) {
        return true;
      }
    }
    return false;
  }

  public int getMinImageCount4TripleBuffering() {

    int minImageCount = surfaceCapabilities.minImageCount() + 1;
    if ((surfaceCapabilities.maxImageCount() > 0) && (minImageCount > surfaceCapabilities.maxImageCount())) {
      minImageCount = surfaceCapabilities.maxImageCount();
    }

    return minImageCount;
  }

}
