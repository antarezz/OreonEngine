package org.oreon.core.vk.device;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceLayerProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFeatures;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.oreon.core.vk.util.VkUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeviceCapabilities {

  public static void checkInstanceExtensionSupport(PointerBuffer ppEnabledExtensionNames) {

    IntBuffer extensionCount = memAllocInt(1);

    int err = vkEnumerateInstanceExtensionProperties("", extensionCount, null);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    VkExtensionProperties.Buffer extensions = VkExtensionProperties.calloc(extensionCount.get(0));

    err = vkEnumerateInstanceExtensionProperties("", extensionCount, extensions);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    List<String> availableExtensions = new ArrayList<>();
    for (VkExtensionProperties extension : extensions) {
      availableExtensions.add(extension.extensionNameString());
    }

    for (int i = 0; i < ppEnabledExtensionNames.limit(); i++) {
      if (!availableExtensions.contains(ppEnabledExtensionNames.getStringUTF8(i))) {
        throw new AssertionError("Extension " + ppEnabledExtensionNames.getStringUTF8(i) + " not supported");
      }
    }

    memFree(extensionCount);
    extensions.free();
  }

  public static List<String> getPhysicalDeviceExtensionNamesSupport(VkPhysicalDevice physicalDevice) {

    IntBuffer extensionCount = memAllocInt(1);

    int err = vkEnumerateDeviceExtensionProperties(physicalDevice, "", extensionCount, null);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    VkExtensionProperties.Buffer extensions = VkExtensionProperties.calloc(extensionCount.get(0));

    err = vkEnumerateDeviceExtensionProperties(physicalDevice, "", extensionCount, extensions);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    List<String> extensionNames = new ArrayList<>();
    for (VkExtensionProperties extension : extensions) {
      extensionNames.add(extension.extensionNameString());
    }

    return extensionNames;
  }

  public static void checkValidationLayerSupport(PointerBuffer ppEnabledLayerNames) {

    IntBuffer layerCount = memAllocInt(1);

    int err = vkEnumerateInstanceLayerProperties(layerCount, null);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    VkLayerProperties.Buffer layers = VkLayerProperties.calloc(layerCount.get(0));

    err = vkEnumerateInstanceLayerProperties(layerCount, layers);
    if (err != VK_SUCCESS) {
      throw new AssertionError(VkUtil.translateVulkanResult(err));
    }

    List<String> availableLayers = new ArrayList<>();
    for (VkLayerProperties layer : layers) {
      availableLayers.add(layer.layerNameString());
    }

    for (int i = 0; i < ppEnabledLayerNames.limit(); i++) {
      if (!availableLayers.contains(ppEnabledLayerNames.getStringUTF8())) {
        throw new AssertionError("Extension " + ppEnabledLayerNames.getStringUTF8() + " not supported");
      }
    }

    ppEnabledLayerNames.flip();

    memFree(layerCount);
    layers.free();
  }

  public static VkPhysicalDeviceProperties checkPhysicalDeviceProperties(VkPhysicalDevice physicalDevice) {

    VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.create();
    vkGetPhysicalDeviceProperties(physicalDevice, properties);

    log.info("Device: " + properties.deviceNameString());

    return properties;
  }

  public static VkPhysicalDeviceFeatures checkPhysicalDeviceFeatures(VkPhysicalDevice physicalDevice) {

    VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.create();
    vkGetPhysicalDeviceFeatures(physicalDevice, features);

    return features;
  }

  public static VkFormatProperties getVkPhysicalDeviceFormatProperties(VkPhysicalDevice physicalDevice,
      int format) {

    VkFormatProperties formatProperties = VkFormatProperties.create();
    vkGetPhysicalDeviceFormatProperties(physicalDevice, format, formatProperties);

    return formatProperties;
  }

  public static boolean getMemoryTypeIndex(VkPhysicalDeviceMemoryProperties memoryProperties,
      int memoryTypeBits,
      int properties,
      IntBuffer memoryTypeIndex) {

    for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
      if ((memoryTypeBits & (1 << i)) != 0 &&
          (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
        memoryTypeIndex.put(0, i);
        return true;
      }
    }

    return false;
  }

}