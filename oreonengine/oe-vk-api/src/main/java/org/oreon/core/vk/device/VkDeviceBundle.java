package org.oreon.core.vk.device;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VkDeviceBundle {

  private PhysicalDevice physicalDevice;
  private LogicalDevice logicalDevice;
}
