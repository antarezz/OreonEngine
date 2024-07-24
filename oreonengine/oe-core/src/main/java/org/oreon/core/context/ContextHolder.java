package org.oreon.core.context;

import java.util.Optional;
import org.oreon.core.platform.Window;
import org.oreon.core.scenegraph.Camera;

public abstract class ContextHolder<C extends Camera, W extends Window, OC extends OreonContext<C, W>> {

  private static final ThreadLocal<OreonContext<?, ?>> CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

  private ContextHolder() {
    throw new IllegalStateException();
  }

  public static OreonContext<?, ?> getContext() {
    return Optional.ofNullable(CONTEXT_THREAD_LOCAL.get())
        .orElseThrow(() -> new IllegalStateException("No OreonContext available"));
  }

  public static void setContext(final OreonContext<?, ?> context) {
    CONTEXT_THREAD_LOCAL.set(context);
  }

  public static void clearContext() {
    CONTEXT_THREAD_LOCAL.remove();
  }
}
