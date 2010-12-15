package com.errorapp_notifier.app;

/**
 * ErrorappNotifier
 *
 */
public class ErrorappNotifier  extends RuntimeException 
{
    public ErrorappNotifier(Throwable cause)
    {
      super(cause);
    }
}
