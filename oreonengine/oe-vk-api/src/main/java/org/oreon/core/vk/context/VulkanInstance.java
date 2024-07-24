package org.oreon.core.vk.context;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugReport.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkCreateDebugReportCallbackEXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT;
import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugReportCallbackCreateInfoEXT;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.oreon.core.vk.util.VkUtil;

import lombok.Getter;

public class VulkanInstance {

  @Getter
  private VkInstance handle;
  private long debugCallbackHandle;

  public VulkanInstance(PointerBuffer ppEnabledLayerNames) {

    PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
    if (requiredExtensions == null) {
      throw new AssertionError("Failed to find list of required Vulkan extensions");
    }

    ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);

    // +1 due to VK_EXT_DEBUG_REPORT_EXTENSION
    PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
    ppEnabledExtensionNames.put(requiredExtensions);
    ppEnabledExtensionNames.put(VK_EXT_DEBUG_REPORT_EXTENSION);
    ppEnabledExtensionNames.flip();

    VkApplicationInfo appInfo = VkApplicationInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
        .pApplicationName(memUTF8("Vulkan Demo"))
        .pEngineName(memUTF8("OREON ENGINE"))
        .apiVersion(VK_MAKE_VERSION(1, 1, 77));

    VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
        .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
        .pNext(0)
        .pApplicationInfo(appInfo)
        .ppEnabledExtensionNames(ppEnabledExtensionNames)
        .ppEnabledLayerNames(ppEnabledLayerNames);
    PointerBuffer pInstance = memAllocPointer(1);
    int err = vkCreateInstance(pCreateInfo, null, pInstance);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create VkInstance: " + VkUtil.translateVulkanResult(err));
    }

    handle = new VkInstance(pInstance.get(0), pCreateInfo);

    pCreateInfo.free();
    memFree(pInstance);
    memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
    memFree(ppEnabledExtensionNames);
    memFree(appInfo.pApplicationName());
    memFree(appInfo.pEngineName());
    appInfo.free();

    VkDebugReportCallbackEXT debugCallback = new VkDebugReportCallbackEXT() {
      public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix,
          long pMessage, long pUserData) {
        System.err.println("ERROR OCCURED: " + VkDebugReportCallbackEXT.getString(pMessage));
        return 0;
      }
    };

    debugCallbackHandle = setupDebugging(handle, VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT,
        debugCallback);
  }

  private long setupDebugging(VkInstance instance, int flags, VkDebugReportCallbackEXT callback) {

    VkDebugReportCallbackCreateInfoEXT debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
        .sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
        .pNext(0)
        .pfnCallback(callback)
        .pUserData(0)
        .flags(flags);

    LongBuffer pCallback = memAllocLong(1);
    int err = vkCreateDebugReportCallbackEXT(instance, debugCreateInfo, null, pCallback);
    long callbackHandle = pCallback.get(0);

    if (err != VK_SUCCESS) {
      throw new AssertionError("Failed to create VkInstance: " + VkUtil.translateVulkanResult(err));
    }

    memFree(pCallback);
    debugCreateInfo.free();

    return callbackHandle;
  }

  public void destroy() {

    vkDestroyDebugReportCallbackEXT(handle, debugCallbackHandle, null);
    vkDestroyInstance(handle, null);
  }
}
