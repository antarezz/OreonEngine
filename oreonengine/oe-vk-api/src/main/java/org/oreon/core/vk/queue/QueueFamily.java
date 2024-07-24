package org.oreon.core.vk.queue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueFamily {

  private int index;
  private int count;
  private int flags;
  private int presentFlag;

}
