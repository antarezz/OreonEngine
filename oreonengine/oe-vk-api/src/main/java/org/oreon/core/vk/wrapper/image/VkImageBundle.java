package org.oreon.core.vk.wrapper.image;

import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VkImageBundle {

  protected VkImage image;
  protected VkImageView imageView;
  protected VkSampler sampler;

  public VkImageBundle(VkImage image, VkImageView imageView) {
    this.image = image;
    this.imageView = imageView;
  }

  public void destroy() {

    if (sampler != null) {
      sampler.destroy();
    }
    imageView.destroy();
    image.destroy();
  }
}
