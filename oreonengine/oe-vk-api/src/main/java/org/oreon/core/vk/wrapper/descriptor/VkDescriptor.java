package org.oreon.core.vk.wrapper.descriptor;

import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VkDescriptor {

  protected DescriptorSet descriptorSet;
  protected DescriptorSetLayout descriptorSetLayout;

  public void destroy() {

    descriptorSet.destroy();
    descriptorSetLayout.destroy();
  }
}
