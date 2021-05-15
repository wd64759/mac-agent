package com.cte4.mac.agent.bundle;

public class BundleProcessException extends Exception {
    public BundleProcessException() {
        super();
    }
    public BundleProcessException(String name) {
        super(name);
    }

    public BundleProcessException(String name, Throwable t) {
        super(name, t);
    }

    public BundleProcessException(Throwable t) {
        super(t);
    }
    
}
